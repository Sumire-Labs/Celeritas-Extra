package jp.s12kuma01.celeritasextra.client.gui;

import jp.s12kuma01.celeritasextra.client.particle.ParticleClassRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ExtraOptionsPersistenceTest {
    @TempDir Path directory;
    private Field minecraftHome;
    private Object previousHome;

    @BeforeEach void initializeForgeConfigContext() throws Exception {
        // Forge's real config parser expects the launcher to have supplied a game directory.
        minecraftHome = FMLInjectionData.class.getDeclaredField("minecraftHome");
        minecraftHome.setAccessible(true);
        previousHome = minecraftHome.get(null);
        minecraftHome.set(null, directory.toFile());
    }

    @AfterEach void restoreForgeConfigContext() throws Exception {
        minecraftHome.set(null, previousHome);
        ParticleClassRegistry.getInstance().loadDisabledClasses(new String[0]);
        ParticleClassRegistry.getInstance().loadSpawnPercentages(new String[0]);
        ParticleClassRegistry.getInstance().markClean();
    }

    @Test void combinedSunMoonSettingMigratesWithoutLosingIndependentChoices() throws Exception {
        Path file = directory.resolve("extra.cfg");
        Files.writeString(file, "detail {\n B:sunMoon=false\n}\n");
        var options = CeleritasExtraGameOptions.load(file.toFile());
        assertFalse(options.detailSettings.sun);
        assertFalse(options.detailSettings.moon);
        options.detailSettings.sun = true;
        options.writeChanges();
        var reloaded = CeleritasExtraGameOptions.load(file.toFile());
        assertTrue(reloaded.detailSettings.sun);
        assertFalse(reloaded.detailSettings.moon);
    }

    @Test void dimensionFogFallsBackToGlobalAndOverridesPersist() {
        var settings = new CeleritasExtraGameOptions.RenderSettings();
        settings.fogDistance = 12;
        settings.fogStart = 80;
        var nether = settings.dimensionFog(-1);
        nether.distance = 4;
        nether.start = 40;
        assertEquals(12, settings.resolveFog(-1).distance());
        nether.override = true;
        assertEquals(4, settings.resolveFog(-1).distance());
        assertEquals(40, settings.resolveFog(-1).start());
        assertEquals(12, settings.resolveFog(0).distance());
        nether.fog = false;
        assertFalse(settings.resolveFog(-1).enabled());
        nether.override = false;
        settings.fogDistance = 20;
        assertEquals(20, settings.resolveFog(-1).distance());
    }

    @Test void newSettingsAndRatesSurviveSaveReload() {
        Path file = directory.resolve("extra.cfg");
        var options = CeleritasExtraGameOptions.load(file.toFile());
        options.renderSettings.entityRenderDistance = 48;
        options.renderSettings.tileEntityRenderDistance = 32;
        options.renderSettings.entityDistanceExemptions = new String[]{"mod.visual.*"};
        options.extraSettings.inactiveFpsLimit = 30;
        options.extraSettings.minimizedFpsLimit = 5;
        var fog = options.renderSettings.dimensionFog(72);
        fog.override = true;
        fog.distance = 6;
        fog.start = 50;
        ParticleClassRegistry.getInstance().setSpawnPercentage("example.Smoke", 25);
        options.writeChanges();
        var reloaded = CeleritasExtraGameOptions.load(file.toFile());
        assertEquals(48, reloaded.renderSettings.entityRenderDistance);
        assertEquals(32, reloaded.renderSettings.tileEntityRenderDistance);
        assertArrayEquals(new String[]{"mod.visual.*"}, reloaded.renderSettings.entityDistanceExemptions);
        assertEquals(30, reloaded.extraSettings.inactiveFpsLimit);
        assertEquals(5, reloaded.extraSettings.minimizedFpsLimit);
        assertEquals(6, reloaded.renderSettings.resolveFog(72).distance());
        assertEquals(50, reloaded.renderSettings.resolveFog(72).start());
        assertEquals(25, ParticleClassRegistry.getInstance().getSpawnPercentage("example.Smoke"));
        ParticleClassRegistry.getInstance().loadSpawnPercentages(new String[0]);
    }
}
