import groovy.json.JsonSlurper
import java.lang.classfile.AnnotationValue
import java.lang.classfile.Attributes
import java.lang.classfile.ClassFile
import java.util.jar.JarFile
import net.minecraftforge.renamer.gradle.RenameJar

val runtimeJar = tasks.named<RenameJar>("reobfJar").flatMap { it.output }
val modId = providers.gradleProperty("mod_id").get()
val modName = providers.gradleProperty("mod_name").get()
val modPackage = providers.gradleProperty("root_package").get()
val modVersion = version.toString()

val verifyReleaseJar = tasks.register("verifyReleaseJar") {
    group = "verification"
    inputs.file(runtimeJar)
    inputs.property("modId", modId)
    inputs.property("modName", modName)
    inputs.property("modPackage", modPackage)
    inputs.property("modVersion", modVersion)
    doLast {
        val id = inputs.properties.getValue("modId") as String
        val name = inputs.properties.getValue("modName") as String
        val pkg = (inputs.properties.getValue("modPackage") as String).replace('.', '/')
        val version = inputs.properties.getValue("modVersion") as String
        JarFile(inputs.files.singleFile).use { jar ->
            fun text(path: String): String {
                val entry = jar.getJarEntry(path) ?: throw GradleException("Release artifact is missing $path")
                return jar.getInputStream(entry).use { it.readBytes().toString(Charsets.UTF_8) }
            }
            fun json(path: String): Map<*, *> = JsonSlurper().parseText(text(path)) as Map<*, *>
            fun readClass(path: String) = ClassFile.of().parse(jar.getInputStream(
                jar.getJarEntry(path) ?: throw GradleException("Missing class: $path")
            ).use { it.readBytes() })
            val metadata = (JsonSlurper().parseText(text("mcmod.info")) as List<*>).single() as Map<*, *>
            if (metadata["version"] != version || metadata["modid"] != id)
                throw GradleException("Release version or Mod ID does not match metadata")
            val pack = json("pack.mcmeta")["pack"] as Map<*, *>
            if (pack["pack_format"] != 3 || pack["description"] != "$name Resources")
                throw GradleException("Resource pack metadata is incorrect")
            val reference = readClass("$pkg/Reference.class")
            mapOf("MOD_ID" to id, "MOD_NAME" to name, "VERSION" to version).forEach { (fieldName, expected) ->
                val field = reference.fields().firstOrNull { it.fieldName().stringValue() == fieldName }
                val constant = field?.findAttribute(Attributes.constantValue())?.orElse(null)
                if (constant?.constant()?.constantValue()?.toString() != expected)
                    throw GradleException("Unresolved or incorrect build constant: Reference.$fieldName")
            }
            val mod = readClass("$pkg/CeleritasExtraMod.class")
            val annotation = mod.findAttribute(Attributes.runtimeVisibleAnnotations()).orElseThrow().annotations()
                .first { it.className().stringValue() == "Lnet/minecraftforge/fml/common/Mod;" }
            val values = annotation.elements().filter { it.name().stringValue() in setOf("modid", "name", "version", "dependencies") }
                .associate { it.name().stringValue() to (it.value() as AnnotationValue.OfString).stringValue() }
            if (values["modid"] != id || values["name"] != name || values["version"] != version)
                throw GradleException("TokenEnvoy constants were not inlined into @Mod")
            if (values["dependencies"]?.contains("required-after:cleanroom@[0.7.14-alpha,0.8)") != true)
                throw GradleException("Release must require the supported Cleanroom 0.7.x API")
            val dependencies = metadata["dependencies"] as List<*>
            if (listOf("celeritas", "nothirium", "renderlib").any { it in dependencies })
                throw GradleException("Metadata must allow any supported renderer")
            val config = json("$id.mixin.json")
            val refmap = json(config["refmap"] as String)["mappings"] as Map<*, *>
            val selectors = mapOf(
                "animation/MixinTextureAtlasSprite" to listOf("updateAnimation()V"),
                "render/entity/MixinForgeHooksClient" to listOf(
                    "Lnet/minecraft/client/renderer/RenderItem;renderQuads(Lnet/minecraft/client/renderer/BufferBuilder;Ljava/util/List;ILnet/minecraft/item/ItemStack;)V",
                    "Lnet/minecraft/client/renderer/Tessellator;draw()V"),
                "background_fps/MixinMinecraft" to listOf("getLimitFramerate"),
                "options_search/MixinOptionsScreen" to listOf("initGui", "drawScreen", "mouseClicked"),
                "render/entity/MixinRenderItemFrame" to listOf("renderItem(Lnet/minecraft/entity/item/EntityItemFrame;)V"),
                "profiler/MixinRenderManager" to listOf("renderEntity"),
                "render/weather/MixinEntityRendererWeather" to listOf("renderWorldPass", "renderRainSnow",
                    "Lnet/minecraft/client/renderer/culling/ICamera;setPosition(DDD)V",
                    "Lnet/minecraft/util/math/BlockPos;getY()I"),
                "profiler/MixinTileEntityRendererDispatcher" to listOf("render(Lnet/minecraft/tileentity/TileEntity;DDDFIF)V"),
                "render/block_entity/MixinTileEntitySignRenderer" to listOf("render(Lnet/minecraft/tileentity/TileEntitySign;DDDFIF)V"),
                "particle/MixinParticleManager" to listOf("spawnEffectParticle"),
                "item_cache/MixinRenderItemVertexCache" to listOf(
                    "renderModel(Lnet/minecraft/client/renderer/block/model/IBakedModel;ILnet/minecraft/item/ItemStack;)V", "renderQuads")
            )
            selectors.forEach { (mixin, methods) ->
                val mappings = refmap["com/sumirelabs/celeritasextra/mixin/$mixin"] as? Map<*, *>
                methods.forEach { selector ->
                    if ((mappings?.get(selector) as? String)?.contains(";func_") != true)
                        throw GradleException("Missing production selector mapping: $mixin $selector")
                }
            }
            fun requireEntry(path: String) {
                if (jar.getJarEntry(path) == null) throw GradleException("Missing entry: $path")
            }
            (config["client"] as List<*>).forEach {
                requireEntry(("${config["package"]}.$it").replace('.', '/') + ".class")
            }
            requireEntry((config["plugin"] as String).replace('.', '/') + ".class")
            listOf("en_us", "ja_jp", "ru_ru", "zh_cn").forEach { text("assets/$id/lang/$it.lang") }
            requireEntry("META-INF/LICENSE.md")
            if (jar.manifest.mainAttributes.getValue("MixinConfigs") != "$id.mixin.json")
                throw GradleException("Release manifest does not register the Mixin config")
            jar.entries().asSequence().forEach { entry ->
                if (entry.name.contains("/devruntime/") || entry.name.startsWith("com/cleanroommc/stripforgereq/") ||
                    entry.name == "mixins.striplatestforgerequirements.json")
                    throw GradleException("Development-only helper bundled in release: ${entry.name}")
                if (entry.name.endsWith(".class")) {
                    val bytes = jar.getInputStream(entry).use { it.readBytes() }.toString(Charsets.ISO_8859_1)
                    if (bytes.contains("org/lwjgl/glfw/"))
                        throw GradleException("Removed GLFW backend referenced by ${entry.name}")
                    if (entry.name.matches(Regex("(?:net/minecraft/|net/minecraftforge/|org/taumc/|org/embeddedt/|dhj/embeddedt/|com/dhj/|meldexun/|net/irisshaders/|net/coderbot/).*")))
                        throw GradleException("Renderer or game dependency bundled: ${entry.name}")
                }
            }
        }
        logger.lifecycle("Verified release artifact: ${inputs.files.singleFile.name}")
    }
}
tasks.named("build") { dependsOn(verifyReleaseJar) }
