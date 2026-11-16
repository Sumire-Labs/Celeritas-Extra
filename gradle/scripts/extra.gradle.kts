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
// Pintonium retains the older GUI API under the same package names as Celeritas.
// Compile its adapter separately so neither renderer's classes shadow the other API.
val pintonium = sources.create("pintonium")
configurations.named(pintonium.implementationConfigurationName) {
    extendsFrom(configurations.getByName("implementation"))
}
dependencies {
    add(pintonium.compileOnlyConfigurationName,
        cleanroom.deobf("curse.maven:pintonium-1549315:9051771"))
}
pintonium.compileClasspath += sources.getByName("main").output
sources.named("main") { runtimeClasspath += pintonium.output }
tasks.named<Jar>("jar") { from(pintonium.output) }
tasks.withType<Jar>().matching { it.name == "sourcesJar" }.configureEach {
    from(pintonium.allSource)
}

val pintoniumTest = sources.create("pintoniumTest")
if (!providers.gradleProperty("enable_junit_testing").get().toBoolean()) {
    pintoniumTest.java.setSrcDirs(emptyList<String>())
}
configurations.named(pintoniumTest.implementationConfigurationName) {
    extendsFrom(configurations.getByName("testImplementation"),
        configurations.getByName(pintonium.compileOnlyConfigurationName))
}
configurations.named(pintoniumTest.runtimeOnlyConfigurationName) {
    extendsFrom(configurations.getByName("testRuntimeOnly"))
}
pintoniumTest.compileClasspath += sources.getByName("main").output + pintonium.output
pintoniumTest.runtimeClasspath += sources.getByName("main").output + pintonium.output
val testPintonium = tasks.register<Test>("testPintonium") {
    description = "Tests native integration against Pintonium's independent GUI API."
    group = "verification"
    enabled = providers.gradleProperty("enable_junit_testing").get().toBoolean()
    testClassesDirs = pintoniumTest.output.classesDirs
    classpath = pintoniumTest.runtimeClasspath
    useJUnitPlatform()
}
tasks.named("check") { dependsOn(testPintonium) }

sources.named("test") {
    compileClasspath += sources.getByName("main").compileClasspath
    runtimeClasspath += sources.getByName("main").compileClasspath
}

// Do not retain empty package directories from removed integrations in the source archive.
tasks.withType<Jar>().matching { it.name == "sourcesJar" }.configureEach {
    includeEmptyDirs = false
}
