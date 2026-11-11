import com.cleanroommc.gradle.api.ext.CleanroomExtension
import java.net.URI
import java.security.MessageDigest

val cleanroom = extensions.getByType<CleanroomExtension>()
val sources = extensions.getByType<SourceSetContainer>()
val actiniumVersion = "alpha-0.0.12"
val actiniumApi = layout.projectDirectory.file(".gradle/compat/Actinium-$actiniumVersion.jar")
val downloadActiniumApi = tasks.register("downloadActiniumApi") {
    val target = actiniumApi.asFile
    val url = "https://github.com/Q-Engineering-Source/Actinium/releases/download/$actiniumVersion/Actinium-$actiniumVersion.jar"
    inputs.property("url", url)
    outputs.file(target)
    doLast {
        target.parentFile.mkdirs()
        URI(url).toURL().openStream().use { input -> target.outputStream().use(input::copyTo) }
        val digest = MessageDigest.getInstance("SHA-256").digest(target.readBytes())
            .joinToString("") { "%02x".format(it) }
        if (digest != "94f79a0b67a7ec8ad3edf18c36b87d1402e98c9d38c49a65187a2d1959776c43") {
            target.delete()
            throw GradleException("Actinium API artifact checksum mismatch")
        }
    }
}
dependencies {
    add("compileOnly", cleanroom.deobf(files(actiniumApi).builtBy(downloadActiniumApi)))
}

// Generate relocated adapters from the same option definitions and configuration bindings.
val generatedAdapters = layout.buildDirectory.dir("generated/sources/actinium/java")
val generateActiniumAdapters = tasks.register("generateActiniumAdapters") {
    val names = listOf("CeleritasExtraGameOptionPages", "CeleritasExtraOptionPages",
        "CeleritasExtraOptionsStorage", "CeleritasExtraOptionsListener")
    val sourceDir = layout.projectDirectory.dir("src/main/java/com/sumirelabs/celeritasextra/client/gui").asFile
    val output = generatedAdapters.get().dir("com/sumirelabs/celeritasextra/compat/actinium/gui").asFile
    inputs.files(names.map { sourceDir.resolve("$it.java") })
    outputs.dir(generatedAdapters)
    mustRunAfter("applyLicenseHeader")
    doLast {
        output.mkdirs()
        names.forEach { name ->
            var source = sourceDir.resolve("$name.java").readText(Charsets.UTF_8)
                .replace("package com.sumirelabs.celeritasextra.client.gui;",
                    "package com.sumirelabs.celeritasextra.compat.actinium.gui;\n\n" +
                        "import com.sumirelabs.celeritasextra.client.gui.CeleritasExtraGameOptions;\n" +
                        "import com.sumirelabs.celeritasextra.client.gui.Translations;\n" +
                        "import com.sumirelabs.celeritasextra.client.gui.WindowModeSupport;")
                .replace("org.taumc.celeritas.api", "dhj.embeddedt.embeddium.api")
                .replace("org.embeddedt.embeddium.impl", "dhj.embeddedt.embeddium.impl")
            if (name == "CeleritasExtraGameOptionPages") {
                source = source.replace("BooleanSupplier cloudsOn = () -> cloudsOption.getValue();",
                    "BooleanSupplier cloudsOn = () -> Minecraft.getMinecraft().gameSettings.clouds != 0;")
                    .replace(".setBinding(setter, getter)",
                        ".setDefaultValue(getter.apply(new CeleritasExtraGameOptions()))\n                .setBinding(setter, getter)")
                    .replace("OptionImpl.createBuilder(int.class, celeritasExtraOpts)",
                        "OptionImpl.createBuilder(int.class, celeritasExtraOpts).setDefaultValue(100)")
                    .replace("OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)",
                        "OptionImpl.createBuilder(boolean.class, celeritasExtraOpts).setDefaultValue(false)")
                mapOf("CloudTranslucency" to "renderSettings.cloudTranslucency",
                    "OverlayCorner" to "extraSettings.overlayCorner", "TextContrast" to "extraSettings.textContrast")
                    .forEach { (type, field) ->
                        source = source.replace("OptionImpl.createBuilder(CeleritasExtraGameOptions.$type.class, celeritasExtraOpts)",
                            "OptionImpl.createBuilder(CeleritasExtraGameOptions.$type.class, celeritasExtraOpts)" +
                                ".setDefaultValue(new CeleritasExtraGameOptions().$field)")
                    }
            }
            if (name == "CeleritasExtraOptionsListener") {
                source = source.replace("OptionImpl.createBuilder(CeleritasExtraGameOptions.VerticalSyncOption.class, celeritasExtraOpts)",
                    "OptionImpl.createBuilder(CeleritasExtraGameOptions.VerticalSyncOption.class, celeritasExtraOpts)" +
                        ".setDefaultValue(CeleritasExtraGameOptions.VerticalSyncOption.OFF)")
            }
            output.resolve("$name.java").writeText(source, Charsets.UTF_8)
        }
    }
}
sources.named("main") { java.srcDir(generatedAdapters) }
tasks.named("compileJava") { dependsOn(generateActiniumAdapters) }
tasks.matching { it.name == "sourcesJar" }.configureEach { dependsOn(generateActiniumAdapters) }
