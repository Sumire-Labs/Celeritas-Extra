import com.cleanroommc.gradle.api.ext.CleanroomExtension

buildscript {
    dependencies {
        classpath(files(rootProject.buildscript.configurations.getByName("classpath")))
    }
}

apply(from = "gradle/scripts/dev-runtime.gradle.kts")
apply(from = "gradle/scripts/release-verification.gradle.kts")

val cleanroom = extensions.getByType<CleanroomExtension>()
dependencies {
    // Despite its dev suffix, the committed renderer uses SRG Minecraft names.
    add("compileOnly", cleanroom.deobf(fileTree("libs") { include("*.jar") }))
}

val sources = extensions.getByType<SourceSetContainer>()
sources.named("test") {
    compileClasspath += sources.getByName("main").compileClasspath
    runtimeClasspath += sources.getByName("main").compileClasspath
}

// Do not retain empty package directories from removed integrations in the source archive.
tasks.withType<Jar>().matching { it.name == "sourcesJar" }.configureEach {
    includeEmptyDirs = false
}
