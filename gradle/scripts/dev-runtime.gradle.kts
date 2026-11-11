import javax.inject.Inject
import org.gradle.api.file.ArchiveOperations

abstract class StripArchiveServices {
    @get:Inject abstract val archives: ArchiveOperations
}

// Keep the legacy Forge-requirement helper functional without targeting an already-loaded class.
val sources = extensions.getByType<SourceSetContainer>()
val originalStripHelper = configurations.create("originalStripHelper") {
    isCanBeConsumed = false
    isTransitive = false
}
dependencies {
    add(originalStripHelper.name, "com.cleanroommc:strip-latest-forge-requirements:1.0:dev")
}
val devRuntime = sources.create("devRuntime") {
    compileClasspath += sources.getByName("main").compileClasspath
}
tasks.named<JavaCompile>("compileDevRuntimeJava") { options.compilerArgs.add("-proc:none") }

val archives = objects.newInstance(StripArchiveServices::class.java).archives
val compatibilityJar = tasks.register<Jar>("stripForgeRequirementsCompatibilityJar") {
    val archiveOperations = archives
    val originalFiles = originalStripHelper.elements
    group = "build setup"
    description = "Builds the development-only Forge requirement helper with late discovery hooks."
    archiveFileName.set("strip-latest-forge-requirements-1.0-compat.jar")
    destinationDirectory.set(layout.buildDirectory.dir("dev-runtime"))
    from(devRuntime.output)
    from(originalFiles.map { files -> files.map { archiveOperations.zipTree(it.asFile) } }) {
        exclude("META-INF/MANIFEST.MF", "mixins.striplatestforgerequirements.json",
            "com/cleanroommc/stripforgereq/mixin/**")
    }
    manifest { attributes("FMLCorePlugin" to "com.cleanroommc.stripforgereq.StripLatestForgeRequirements") }
}
configurations.named("runtimeClasspath") {
    exclude(group = "com.cleanroommc", module = "strip-latest-forge-requirements")
}
dependencies {
    add("runtimeOnly", files(compatibilityJar.flatMap { it.archiveFile }).builtBy(compatibilityJar))
}
sources.named("test") {
    compileClasspath += devRuntime.output
    runtimeClasspath += devRuntime.output
}
