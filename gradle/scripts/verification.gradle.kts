import groovy.json.JsonSlurper
import java.io.DataInputStream
import java.util.jar.JarFile
import net.minecraftforge.renamer.gradle.RenameJar

val mod_id = providers.gradleProperty("mod_id").get()
val root_package = providers.gradleProperty("root_package").get()
val configuredCoremod = providers.gradleProperty("coremod_plugin_class").orElse("").get()
val coremodEnabled = providers.gradleProperty("enable_coremod").map(String::toBoolean)
    .orElse(configuredCoremod.isNotBlank()).get()
val coremod_plugin_class = if (coremodEnabled) configuredCoremod.ifBlank { "$root_package.core.LoadingPlugin" } else ""
val mixinEnabled = providers.gradleProperty("enable_mixin").get().toBoolean()
val configuredTransformers = providers.gradleProperty("access_transformers").orElse("").get()
    .split(Regex("\\s+")).filter(String::isNotBlank)
val atEnabled = providers.gradleProperty("use_access_transformer").map(String::toBoolean)
    .orElse(configuredTransformers.isNotEmpty()).get()
val accessTransformers = if (atEnabled) configuredTransformers.ifEmpty { listOf("${mod_id}_at.cfg") } else emptyList()
val nativeRefmap = providers.gradleProperty("cg.userdev.refmap").get()
val modVersion = project.version.toString()
val runtimeJar = tasks.named<RenameJar>("reobfJar").flatMap { it.output }
val containedNames = configurations.named("contain").flatMap { it.elements }
    .map { files -> files.map { it.asFile.name }.sorted() }

val verifyModJar = tasks.register("verifyModJar") {
    group = "verification"
    description = "Checks Mod metadata and optional features in the SRG distribution jar."
    inputs.file(runtimeJar)
    inputs.property("modId", mod_id)
    inputs.property("modPackage", root_package)
    inputs.property("modVersion", modVersion)
    inputs.property("mixinEnabled", mixinEnabled)
    inputs.property("coremodClass", coremod_plugin_class)
    inputs.property("accessTransformers", accessTransformers)
    inputs.property("nativeRefmap", nativeRefmap)
    inputs.property("containedDependencies", containedNames)
    doLast {
        val mod_id = inputs.properties.getValue("modId") as String
        val root_package = inputs.properties.getValue("modPackage") as String
        val modVersion = inputs.properties.getValue("modVersion") as String
        val mixinEnabled = inputs.properties.getValue("mixinEnabled") as Boolean
        val coremod_plugin_class = inputs.properties.getValue("coremodClass") as String
        val coremodEnabled = coremod_plugin_class.isNotBlank()
        val accessTransformers = (inputs.properties.getValue("accessTransformers") as List<*>).map { it as String }
        val nativeRefmap = inputs.properties.getValue("nativeRefmap") as String
        val expectedContained = (inputs.properties.getValue("containedDependencies") as List<*>).map { it as String }
        JarFile(inputs.files.singleFile).use { jar ->
            val entries = jar.entries().asSequence().toList()
            if (jar.getJarEntry("META-INF/LICENSE.md") == null) {
                throw GradleException("Missing license notice: META-INF/LICENSE.md")
            }
            fun json(name: String): Any {
                val entry = jar.getJarEntry(name) ?: throw GradleException("Missing jar resource: $name")
                return jar.getInputStream(entry).use { JsonSlurper().parse(it, "UTF-8") }
            }
            val metadata = json("mcmod.info") as List<*>
            val mod = metadata.singleOrNull() as? Map<*, *>
            if (mod?.get("modid") != mod_id || mod["version"] != modVersion) {
                throw GradleException("Distribution metadata does not match the Mod ID and Git version.")
            }
            val attributes = jar.manifest.mainAttributes
            if (attributes.getValue("ModType") != "CRL") {
                throw GradleException("Missing Cleanroom-only ModType manifest attribute.")
            }
            val declaredContained = attributes.getValue("ContainedDeps").orEmpty()
                .split(Regex("\\s+")).filter(String::isNotBlank).sorted()
            if (declaredContained != expectedContained || expectedContained.any { jar.getJarEntry(it) == null } ||
                (expectedContained.isNotEmpty() && attributes.getValue("NonModDeps") != "true")) {
                throw GradleException("Embedded dependencies do not match the ContainedDeps manifest attributes.")
            }
            val at = attributes.getValue("FMLAT")
            if (accessTransformers.isNotEmpty()) {
                if (at != accessTransformers.joinToString(" ") || accessTransformers.any {
                    jar.getJarEntry("META-INF/$it") == null
                }) {
                    throw GradleException("Missing access transformer or FMLAT manifest attribute.")
                }
            } else if (at != null || entries.any { it.name.endsWith("_at.cfg") }) {
                throw GradleException("Access transformer leaked into the disabled variant.")
            }
            val coremod = attributes.getValue("FMLCorePlugin")
            if (coremodEnabled) {
                if (coremod != coremod_plugin_class || jar.getJarEntry(coremod.replace('.', '/') + ".class") == null) {
                    throw GradleException("Missing coremod class or incorrect FMLCorePlugin attribute.")
                }
            } else if (coremod != null || entries.any { it.name.startsWith(root_package.replace('.', '/') + "/core/") }) {
                throw GradleException("Coremod classes or manifest attribute leaked into the disabled variant.")
            }
            val unresolvedToken = Regex("@\\{[^{}\\r\\n]+}")
            entries.filter {
                !it.isDirectory && (it.name.endsWith(".class") || it.name in listOf("mcmod.info", "pack.mcmeta") ||
                    it.name.endsWith(".mixin.json"))
            }.forEach { entry ->
                fun checkText(text: String) {
                    if (unresolvedToken.containsMatchIn(text)) {
                        throw GradleException("Unresolved token in ${entry.name}")
                    }
                }
                if (entry.name.endsWith(".class")) {
                    // Only constant-pool text can contain tokens; numeric values and bytecode are binary.
                    DataInputStream(jar.getInputStream(entry)).use { input ->
                        if (input.readInt() != 0xCAFEBABE.toInt()) {
                            throw GradleException("Invalid class file: ${entry.name}")
                        }
                        input.readInt() // minor_version and major_version
                        val count = input.readUnsignedShort()
                        var index = 1
                        while (index < count) {
                            when (val tag = input.readUnsignedByte()) {
                                1 -> checkText(input.readUTF())
                                3, 4 -> input.readInt()
                                5, 6 -> {
                                    input.readLong()
                                    index++ // Long and double constants occupy two entries.
                                }
                                7, 8, 16, 19, 20 -> input.readUnsignedShort()
                                9, 10, 11, 12, 17, 18 -> input.readInt()
                                15 -> {
                                    input.readUnsignedByte()
                                    input.readUnsignedShort()
                                }
                                else -> throw GradleException("Unknown constant-pool tag $tag in ${entry.name}")
                            }
                            index++
                        }
                    }
                } else {
                    checkText(jar.getInputStream(entry).use { it.readBytes().toString(Charsets.UTF_8) })
                }
            }
            val configs = attributes.getValue("MixinConfigs")
            if (mixinEnabled) {
                if (configs != "$mod_id.mixin.json") throw GradleException("Incorrect MixinConfigs attribute.")
                val config = json(configs) as Map<*, *>
                val refmap = json(config["refmap"] as String) as Map<*, *>
                if ((refmap["mappings"] as? Map<*, *>).isNullOrEmpty()) {
                    throw GradleException("Missing Mixin mappings in the native refmap.")
                }
                listOf("mixins", "client", "server").flatMap { (config[it] as? List<*>).orEmpty() }.forEach { name ->
                    val classFile = "${config["package"]}.$name".replace('.', '/') + ".class"
                    if (jar.getJarEntry(classFile) == null) throw GradleException("Missing Mixin class: $name")
                }
            } else if (configs != null || entries.any {
                it.name == nativeRefmap || it.name.endsWith(".mixin.json") || it.name.endsWith(".refmap.json") ||
                    it.name.startsWith(root_package.replace('.', '/') + "/mixin/")
            }) {
                throw GradleException("Mixin resources leaked into the disabled variant.")
            }
        }
        logger.lifecycle("Verified distributable Mod jar (Mixin {}).", if (mixinEnabled) "ON" else "OFF")
    }
}
tasks.named("check") { dependsOn(verifyModJar) }
