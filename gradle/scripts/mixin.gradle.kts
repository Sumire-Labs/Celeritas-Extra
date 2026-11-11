import com.cleanroommc.gradle.api.task.userdev.ExtractUserdevFile
import com.cleanroommc.gradle.env.UserDevTasks
import net.minecraftforge.renamer.gradle.RenameJar
import net.minecraftforge.srgutils.IMappingFile

val nativeRefmap = providers.gradleProperty("cg.userdev.refmap").get()
if (providers.gradleProperty("enable_mixin").get().toBoolean()) {
    require(nativeRefmap.isNotBlank()) { "Set cg.userdev.refmap when enable_mixin=true." }
    val extraMappings = layout.buildDirectory.file("generated/mixin-mappings/mixin.tsrg")
    val mergedMappings = layout.buildDirectory.file("generated/mixin-mappings/merged.tsrg")
    val extractedMapping = tasks.named<ExtractUserdevFile>("extractUserdevMcpToSrg")
    val baseMappings = extractedMapping.flatMap { it.output }
    val compileJava = tasks.named<JavaCompile>("compileJava") {
        outputs.file(extraMappings)
        options.compilerArgs.add("-AoutTsrgFile=${extraMappings.get().asFile}")
        doFirst { extraMappings.get().asFile.parentFile.mkdirs() }
    }
    val mergeMappings = tasks.register("mergeMixinMappings") {
        group = "build setup"
        description = "Merges Minecraft and Mixin member mappings for the reobfuscated jar."
        dependsOn(compileJava, extractedMapping)
        inputs.file(baseMappings)
        inputs.file(extraMappings)
        outputs.file(mergedMappings)
        doLast {
            val base = IMappingFile.load(baseMappings.get().asFile)
            val extra = IMappingFile.load(extraMappings.get().asFile)
            base.merge(extra).write(mergedMappings.get().asFile.toPath(), IMappingFile.Format.TSRG2, false)
        }
    }
    tasks.named<RenameJar>("reobfJar") {
        dependsOn(mergeMappings)
        map.setFrom(mergedMappings)
    }
} else {
    tasks.named<JavaCompile>("compileJava") {
        options.compilerArgumentProviders.removeAll { it is UserDevTasks.RefmapArguments }
    }
    configurations.named("annotationProcessor") {
        withDependencies {
            removeIf { it.group == "com.cleanroommc" && it.name in setOf("cleanmix", "mixinextras-common") }
        }
    }
    // Switching variants can leave generated refmaps from the enabled build.
    tasks.withType<Jar>().configureEach { exclude("*.refmap.json", nativeRefmap) }
}
