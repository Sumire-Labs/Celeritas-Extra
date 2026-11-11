import com.diffplug.gradle.spotless.SpotlessExtension
import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.json.JsonOutput
import net.minecraftforge.renamer.gradle.RenameJar
import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

// Development scaffold: Sumire-Labs/CleanroomDevEnv 590d46c.

plugins {
    id("com.sumirelabs.felt") version "0.3.0"
    id("com.cleanroommc.versioning") version "3.4.0"
    `java-library`
    `maven-publish`
    id("com.cleanroommc.cleanroomgradle")
    id("com.cleanroommc.tokenenvoy") version "3.0.0"
    id("com.gradleup.shadow") version "9.6.1" apply false
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.4.1"
    id("com.diffplug.spotless") version "8.11.0" apply false
    id("me.modmuss50.mod-publish-plugin") version "2.2.1" apply false
}

fun propertyString(name: String): String = providers.gradleProperty(name).get()
fun propertyFlag(name: String): Boolean = propertyString(name).toBoolean()

val modId = propertyString("mod_id")
val modName = propertyString("mod_name")
val modPackage = propertyString("root_package")
val modVersion = version.toString()
val assetMoverVersion = providers.gradleProperty("asset_mover_version").get()
val mixinEnabled = propertyFlag("enable_mixin")
val shadowEnabled = propertyFlag("enable_shadow")
val spotlessEnabled = propertyFlag("enable_spotless")
val testingEnabled = propertyFlag("enable_junit_testing")
val configuredTransformers = providers.gradleProperty("access_transformers").orElse("").get()
    .split(Regex("\\s+")).filter(String::isNotBlank)
val atEnabled = providers.gradleProperty("use_access_transformer").map(String::toBoolean)
    .orElse(configuredTransformers.isNotEmpty()).get()
val accessTransformerNames = if (atEnabled) configuredTransformers.ifEmpty { listOf("${modId}_at.cfg") } else emptyList()
val configuredCoremod = providers.gradleProperty("coremod_plugin_class").orElse("").get()
val coremodEnabled = providers.gradleProperty("enable_coremod").map(String::toBoolean)
    .orElse(configuredCoremod.isNotBlank()).get()
val coremodClass = if (coremodEnabled) configuredCoremod.ifBlank { "$modPackage.core.LoadingPlugin" } else ""

group = modPackage
base.archivesName.set(modId)
require(modId.matches(Regex("[a-z][a-z0-9_-]*"))) {
    "mod_id must start with a lowercase letter and contain only lowercase letters, numbers, underscores or hyphens."
}

if (shadowEnabled) apply(plugin = "com.gradleup.shadow")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    if (propertyFlag("generate_sources_jar")) withSourcesJar()
    if (propertyFlag("generate_javadocs_jar")) withJavadocJar()
}

val contain = configurations.create("contain")
val containedFiles = contain.elements
configurations.implementation { extendsFrom(contain) }
if (shadowEnabled) {
    val shade = configurations.create("shade")
    configurations.implementation { extendsFrom(shade) }
}

dependencies {
    implementation(cleanroom.userdev(propertyString("cleanroom_version")) {
        accessTransformers.from(accessTransformerNames.map { "src/main/resources/META-INF/$it" })
    })
    if (propertyFlag("use_asset_mover")) {
        implementation("com.cleanroommc:assetmover:$assetMoverVersion")
    }
    if (testingEnabled) {
        testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }
}
if (mixinEnabled) {
    // Match the compile-only APIs to the processors selected from this Loader.
    configurations.compileOnly.get().dependencies.addAllLater(providers.provider {
        configurations.annotationProcessor.get().allDependencies.filter {
            it.group == "com.cleanroommc" && it.name in setOf("cleanmix", "mixinextras-common")
        }.map { (it.copy() as ModuleDependency).apply { isTransitive = false } }
    })
}

sourceSets.main {
    val packagePath = modPackage.replace('.', '/')
    val enabledTransformers = accessTransformerNames
    if (!mixinEnabled) java.exclude("$packagePath/mixin/**")
    if (coremodClass.isBlank()) java.exclude("$packagePath/core/**")
    resources.exclude {
        it.path.startsWith("META-INF/") && it.name.endsWith("_at.cfg") && it.name !in enabledTransformers
    }
    if (!mixinEnabled) resources.exclude("*.mixin.json")
}
if (!testingEnabled) sourceSets.test { java.setSrcDirs(emptyList<String>()) }

tokenEnvoy {
    sourceSet("main") {
        set("MOD_ID", modId)
        set("MOD_NAME", modName)
        set("MOD_VERSION", modVersion)
        set("MOD_UPDATE_JSON", propertyString("mod_update_json"))
        set("MOD_PACKAGE", modPackage)
        set("CLIENT_PROXY", "$modPackage.proxy.ClientProxy")
        set("SERVER_PROXY", "$modPackage.proxy.CommonProxy")
        set("MIXIN_REFMAP", providers.gradleProperty("cg.userdev.refmap"))
        set("MOD_ID_JSON", JsonOutput.toJson(modId))
        set("MOD_NAME_JSON", JsonOutput.toJson(modName))
        set("MOD_VERSION_JSON", JsonOutput.toJson(modVersion))
        set("MOD_DESCRIPTION_JSON", JsonOutput.toJson(propertyString("mod_description")))
        set("MOD_AUTHORS_JSON", JsonOutput.toJson(propertyString("mod_authors").split(',').map(String::trim).filter(String::isNotBlank)))
        set("MOD_CREDITS_JSON", JsonOutput.toJson(propertyString("mod_credits")))
        set("MOD_URL_JSON", JsonOutput.toJson(propertyString("mod_url")))
        set("MOD_UPDATE_JSON_JSON", JsonOutput.toJson(propertyString("mod_update_json")))
        set("MOD_LOGO_JSON", JsonOutput.toJson(propertyString("mod_logo_path")))
        set("MOD_ISSUE_TRACKER_JSON", JsonOutput.toJson(propertyString("mod_issue_tracker")))
        set("PACK_DESCRIPTION_JSON", JsonOutput.toJson("$modName Resources"))
        classes { include("**/Reference.class", "**/CeleritasExtraMod.class") }
        resources { include("mcmod.info", "pack.mcmeta", "*.mixin.json") }
    }
}

tasks.processResources {
    filteringCharset = "UTF-8"
    rename("modid.mixin.json", "$modId.mixin.json")
}
tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }
tasks.withType<Jar>().configureEach {
    from("LICENSE.md") { into("META-INF") }
    if (name == "jar" || name == "shadowJar") {
        val embeddedFiles = containedFiles
        doFirst {
            val files = embeddedFiles.get()
            if (files.isNotEmpty()) {
                (this as Jar).manifest.attributes(
                    "ContainedDeps" to files.joinToString(" ") { it.asFile.name },
                    "NonModDeps" to "true"
                )
            }
        }
    }
}
tasks.test {
    enabled = testingEnabled
    useJUnitPlatform()
    testLogging.showStandardStreams = propertyFlag("show_testing_output")
}

apply(from = "gradle/scripts/dependencies.gradle.kts")
apply(from = "gradle/scripts/mixin.gradle.kts")
apply(from = "gradle/scripts/runs.gradle.kts")
if (spotlessEnabled) {
    apply(plugin = "com.diffplug.spotless")
    configure<SpotlessExtension> {
        encoding("UTF-8")
        lineEndings = com.diffplug.spotless.LineEnding.UNIX
        java {
            target("src/*/java/**/*.java")
            toggleOffOn()
            importOrderFile("gradle/spotless/spotless.importorder")
            removeUnusedImports()
            eclipse().configFile("gradle/spotless/spotless.eclipseformat.xml")
            trimTrailingWhitespace()
            endWithNewline()
        }
        format("misc") {
            target("*.gradle", "*.gradle.kts", "*.properties", ".gitignore", ".gitattributes",
                "gradle/scripts/*.gradle", "gradle/scripts/*.gradle.kts", ".github/workflows/*.yml")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}

idea {
    module {
        inheritOutputDirs = true
        isDownloadSources = true
    }
    project {
        settings {
            runConfigurations {
                create("1. Build", Gradle::class.java) { taskNames = listOf("build") }
                create("2. Run Client", Gradle::class.java) { taskNames = listOf("runClient") }
                create("3. Run Server", Gradle::class.java) { taskNames = listOf("runServer") }
                if (spotlessEnabled) {
                    create("4. Apply Spotless", Gradle::class.java) { taskNames = listOf("spotlessApply") }
                }
            }
        }
    }
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.FAIL
    from(contain) { into("/") }
    manifest {
        attributes("ModType" to "CRL")
        if (mixinEnabled) attributes("MixinConfigs" to "$modId.mixin.json")
        if (accessTransformerNames.isNotEmpty()) attributes("FMLAT" to accessTransformerNames.joinToString(" "))
        if (coremodClass.isNotBlank()) {
            attributes("FMLCorePlugin" to coremodClass)
            if (propertyFlag("coremod_includes_mod")) {
                attributes("FMLCorePluginContainsFMLMod" to "true", "ForceLoadAsMod" to "true")
            }
        }
    }
}
if (shadowEnabled) {
    val shadowJar = tasks.named<ShadowJar>("shadowJar") {
        dependsOn(tasks.jar)
        configurations = listOf(project.configurations.getByName("shade"))
        archiveClassifier.set("shadow-mcp")
        destinationDirectory.set(layout.buildDirectory.dir("intermediates/shadow"))
        from(contain) { into("/") }
    }
    tasks.named<RenameJar>("reobfJar") {
        from(shadowJar)
        archiveClassifier.set("")
    }
}

val modDescription = propertyString("mod_description")
val modUrl = propertyString("mod_url")
val mavenName = propertyString("maven_name")
val mavenUrl = propertyString("maven_url")
val curseforgeProjectId = propertyString("curseforge_project_id")
val modrinthProjectId = propertyString("modrinth_project_id")
val remotePublishing = propertyFlag("publish_to_maven")
val localPublishing = propertyFlag("publish_to_local_maven")
val reobfJar = tasks.named<RenameJar>("reobfJar")

if (remotePublishing || localPublishing) {
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                groupId = modPackage
                artifactId = modId
                artifact(reobfJar) { classifier = "srg" }
                pom {
                    name.set(modName)
                    description.set(modDescription)
                    if (modUrl.isNotBlank()) url.set(modUrl)
                }
            }
        }
        if (remotePublishing && !localPublishing) {
            require(mavenUrl.isNotBlank()) { "Set maven_url before enabling publish_to_maven." }
            repositories {
                maven {
                    name = mavenName
                    url = uri(mavenUrl)
                    credentials {
                        username = providers.environmentVariable("MAVEN_USERNAME")
                            .orElse(providers.gradleProperty("${mavenName}Username")).orNull
                        password = providers.environmentVariable("MAVEN_PASSWORD")
                            .orElse(providers.gradleProperty("${mavenName}Password")).orNull
                    }
                }
            }
        }
    }
}

apply(plugin = "me.modmuss50.mod-publish-plugin")
configure<ModPublishExtension> {
    file.set(reobfJar.flatMap { it.output })
    version.set(modVersion)
    displayName.set("$modName $modVersion")
    type.set(versioning.stage.map { stage ->
        when (stage.name) {
            "ALPHA" -> ReleaseType.ALPHA
            "BETA", "RC" -> ReleaseType.BETA
            else -> ReleaseType.STABLE
        }
    })
    modLoaders.add("forge")
    maxRetries.set(5)
    val notes = providers.environmentVariable("CHANGELOG_FILE").orElse("RELEASE_NOTES.md")
    changelog.set(providers.fileContents(layout.projectDirectory.file(notes)).asText.orElse(""))
    if (curseforgeProjectId.isNotBlank()) {
        curseforge {
            projectId.set(curseforgeProjectId)
            accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
            minecraftVersions.add("1.12.2")
            client.set(true)
            server.set(false)
        }
    }
    if (modrinthProjectId.isNotBlank()) {
        modrinth {
            projectId.set(modrinthProjectId)
            accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
            minecraftVersions.add("1.12.2")
        }
    }
}
apply(from = "gradle/scripts/verification.gradle.kts")
apply(from = "gradle/scripts/extra.gradle.kts")

val releaseType = versioning.stage.map { stage ->
    stage.name.lowercase().let { if (it == "rc") "beta" else it }
}
tasks.register("printReleaseType") {
    inputs.property("releaseType", releaseType)
    doLast { println(inputs.properties.getValue("releaseType")) }
}
val publishMaven = remotePublishing && !localPublishing
val publishCurseforge = curseforgeProjectId.isNotBlank()
val publishModrinth = modrinthProjectId.isNotBlank()
tasks.register("printReleaseInfo") {
    inputs.property("info", releaseType.map { type ->
        JsonOutput.toJson(mapOf(
            "version" to modVersion, "type" to type, "maven" to publishMaven,
            "curseforge" to publishCurseforge, "modrinth" to publishModrinth
        ))
    })
    doLast { println(inputs.properties.getValue("info")) }
}

// Check maintained sources; generated adapters inherit the header from their inputs.
felt { sources.setFrom(fileTree("src") { include("**/*.java") }) }
