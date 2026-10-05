package com.sumire.celeritasextra.compat.nothirium;

import com.sumire.celeritasextra.client.gui.CeleritasExtraGameOptions;
import com.sumire.celeritasextra.client.particle.ParticleClassRegistry;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ForgeExtraConfigTest {
    @TempDir Path directory;

    @Test void forgeEditsApplyToLiveOptionsAndPersistWithoutReloadingStaleDiskValues() throws Exception {
        var home = FMLInjectionData.class.getDeclaredField("minecraftHome");
        home.setAccessible(true);
        Object previous = home.get(null);
        var registry = ParticleClassRegistry.getInstance();
        try {
            home.set(null, directory.toFile());
            var file = directory.resolve("extra.cfg").toFile();
            var options = CeleritasExtraGameOptions.load(file);
            var config = options.forgeConfiguration();
            new net.minecraftforge.common.config.ConfigElement(config.getCategory("render").get("entityRenderDistance")).set(72);
            config.getCategory("detail").get("stars").set(false);
            config.getCategory("extra").get("menuFpsLimit").set(45);
            config.get("dimension_fog_-1", "override", false).set(true);
            config.get("dimension_fog_-1", "distance", 0).set(8);
            config.getCategory("particle_classes").get("spawnPercentages").set(new String[]{"example.Particle|25"});
            assertEquals(0, options.renderSettings.entityRenderDistance, "Pending GUI edits must not change live settings");
            options.applyForgeConfiguration();
            assertEquals(72, options.renderSettings.entityRenderDistance);
            assertFalse(options.detailSettings.stars);
            assertEquals(45, options.extraSettings.menuFpsLimit);
            assertEquals(8, options.renderSettings.resolveFog(-1).distance());
            assertEquals(25, registry.getSpawnPercentage("example.Particle"));
            var reloaded = CeleritasExtraGameOptions.load(file);
            assertEquals(72, reloaded.renderSettings.entityRenderDistance);
            assertFalse(reloaded.detailSettings.stars);
            assertEquals(8, reloaded.renderSettings.resolveFog(-1).distance());
        } finally {
            home.set(null, previous);
            registry.loadSpawnPercentages(new String[0]);
            registry.loadDisabledClasses(new String[0]);
            registry.markClean();
        }
    }

    @Test void enumControlsUseNamesAndPreserveTheExistingOrdinalFileFormat() {
        var property = new net.minecraftforge.common.config.Property("textContrast", "2",
                net.minecraftforge.common.config.Property.Type.INTEGER);
        property.setDefaultValue("2");
        var element = ForgeExtraConfigElements.enumElement(property, CeleritasExtraGameOptions.TextContrast.values(),
                new String[]{"None", "Background", "Shadow"});
        assertEquals("SHADOW", element.get());
        assertEquals("SHADOW", element.getDefault());
        element.set("BACKGROUND");
        assertEquals(1, property.getInt());
        assertEquals("BACKGROUND", element.get());
        assertEquals("SHADOW", element.getDefault(), "Editing must preserve reset-to-default behavior");
    }
}
