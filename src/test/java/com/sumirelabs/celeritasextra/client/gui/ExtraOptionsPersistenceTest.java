/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod;
import com.sumirelabs.celeritasextra.client.particle.ParticleClassRegistry;

import net.minecraftforge.fml.relauncher.FMLInjectionData;

import static org.junit.jupiter.api.Assertions.*;

class ExtraOptionsPersistenceTest {

    @Test
    void informationFlagsAndPerLineColorsSurviveReload() {
        var file = directory.resolve("hud.cfg").toFile();
        var options = CeleritasExtraGameOptions.load(file);
        var extra = options.extraSettings;
        extra.showLocalTime = extra.use12HourClock = extra.showWorldDay = extra.showSessionTime = true;
        extra.showEntityCount = extra.showRenderedEntities = extra.showBiome = true;
        extra.showMemoryExtended = false;
        extra.setHudColor(CeleritasExtraGameOptions.HudLine.BIOME, 0, 17);
        extra.setHudColor(CeleritasExtraGameOptions.HudLine.BIOME, 1, -10);
        extra.setHudColor(CeleritasExtraGameOptions.HudLine.BIOME, 2, 300);
        options.detailSettings.rainSnowCulling = false;
        options.writeChanges();
        var reloaded = CeleritasExtraGameOptions.load(file);
        assertTrue(reloaded.extraSettings.showLocalTime && reloaded.extraSettings.use12HourClock
            && reloaded.extraSettings.showWorldDay && reloaded.extraSettings.showSessionTime
            && reloaded.extraSettings.showEntityCount && reloaded.extraSettings.showRenderedEntities
            && reloaded.extraSettings.showBiome);
        assertFalse(reloaded.extraSettings.showMemoryExtended);
        assertFalse(reloaded.detailSettings.rainSnowCulling);
        assertEquals(0x1100FF, reloaded.extraSettings.hudColor(CeleritasExtraGameOptions.HudLine.BIOME));
        assertEquals(0xFFFFFF, reloaded.extraSettings.hudColor(CeleritasExtraGameOptions.HudLine.FPS));
        assertEquals("255", reloaded.forgeConfiguration().getCategory("hud_color_biome").get("blue").getMaxValue());
    }

    @Test
    void savingRetainsSliderBoundsAndThreeFaceSettings() {
        var file = directory.resolve("slider-ranges.cfg").toFile();
        var options = CeleritasExtraGameOptions.load(file);
        options.renderSettings.itemFrameThreeFaceCulling = true;
        options.renderSettings.itemFrameThreeFaceDistance = 12;
        options.renderSettings.dimensionFog(-1);
        options.writeChanges();
        var config = options.forgeConfiguration();
        var scale = config.getCategory("render").get("cloudScale");
        assertEquals("1", scale.getMinValue());
        assertEquals("16", scale.getMaxValue());
        assertEquals(4, scale.getInt());
        assertEquals("-16", config.getCategory("render").get("cloudHeight").getMinValue());
        assertEquals("200", config.getCategory("dimension_fog_-1").get("start").getMaxValue());
        var reloaded = CeleritasExtraGameOptions.load(file);
        assertTrue(reloaded.renderSettings.itemFrameThreeFaceCulling);
        assertEquals(12, reloaded.renderSettings.itemFrameThreeFaceDistance);
    }

    @Test
    void earlyOptionsUseLaunchDirectoryAndLaterCanonicalDirectoryIsHonored() throws Exception {
        var configField = CeleritasExtraClientMod.class.getDeclaredField("CONFIG");
        var directoryField = CeleritasExtraClientMod.class.getDeclaredField("configDirectory");
        configField.setAccessible(true);
        directoryField.setAccessible(true);
        Object previousConfig = configField.get(null);
        Object previousDirectory = directoryField.get(null);
        try {
            configField.set(null, null);
            directoryField.set(null, null);
            var earlyDirectory = Files.createDirectories(directory.resolve("config"));
            Files.writeString(earlyDirectory.resolve("celeritas-extra.cfg"), "extra {\n I:inactiveFpsLimit=37\n}\n");
            var early = CeleritasExtraClientMod.options();
            assertEquals(37, early.extraSettings.inactiveFpsLimit);
            assertEquals(earlyDirectory.toFile(), directoryField.get(null));
            CeleritasExtraClientMod.setConfigDirectory(earlyDirectory.toFile());
            assertSame(early, CeleritasExtraClientMod.options(),
                "Same-directory preInit must retain the existing config");
            var canonical = Files.createDirectories(directory.resolve("canonical-config"));
            Files.writeString(canonical.resolve("celeritas-extra.cfg"), "extra {\n I:inactiveFpsLimit=91\n}\n");
            CeleritasExtraClientMod.setConfigDirectory(canonical.toFile());
            assertEquals(91, CeleritasExtraClientMod.options().extraSettings.inactiveFpsLimit);
        } finally {
            configField.set(null, previousConfig);
            directoryField.set(null, previousDirectory);
        }
    }

    @TempDir Path directory;
    private Field minecraftHome;
    private Object previousHome;

    @BeforeEach
    void initializeForgeConfigContext() throws Exception {
        // Forge's real config parser expects the launcher to have supplied a game directory.
        minecraftHome = FMLInjectionData.class.getDeclaredField("minecraftHome");
        minecraftHome.setAccessible(true);
        previousHome = minecraftHome.get(null);
        minecraftHome.set(null, directory.toFile());
    }

    @AfterEach
    void restoreForgeConfigContext() throws Exception {
        minecraftHome.set(null, previousHome);
        ParticleClassRegistry.getInstance().loadDisabledClasses(new String[0]);
        ParticleClassRegistry.getInstance().loadSpawnPercentages(new String[0]);
        ParticleClassRegistry.getInstance().markClean();
    }

    @Test
    void obsoleteSunMoonIsIgnoredAndIndependentChoicesPersist() throws Exception {
        Path file = directory.resolve("extra.cfg");
        Files.writeString(file, "detail {\n B:sunMoon=false\n}\n");
        var options = CeleritasExtraGameOptions.load(file.toFile());
        assertTrue(options.detailSettings.sun);
        assertTrue(options.detailSettings.moon);
        assertFalse(options.forgeConfiguration().hasKey("detail", "sunMoon"));
        options.detailSettings.moon = false;
        options.writeChanges();
        var reloaded = CeleritasExtraGameOptions.load(file.toFile());
        assertTrue(reloaded.detailSettings.sun);
        assertFalse(reloaded.detailSettings.moon);
    }

    @Test
    void dimensionFogFallsBackToGlobalAndOverridesPersist() {
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

    @Test
    void newSettingsAndRatesSurviveSaveReload() {
        Path file = directory.resolve("extra.cfg");
        var options = CeleritasExtraGameOptions.load(file.toFile());
        options.renderSettings.entityRenderDistance = 48;
        options.renderSettings.tileEntityRenderDistance = 32;
        options.renderSettings.entityDistanceExemptions = new String[]{"mod.visual.*"};
        options.extraSettings.inactiveFpsLimit = 30;
        assertEquals(0, options.extraSettings.menuFpsLimit);
        options.extraSettings.menuFpsLimit = 60;
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
        assertEquals(60, reloaded.extraSettings.menuFpsLimit);
        assertEquals(5, reloaded.extraSettings.minimizedFpsLimit);
        assertEquals(6, reloaded.renderSettings.resolveFog(72).distance());
        assertEquals(50, reloaded.renderSettings.resolveFog(72).start());
        assertEquals(25, ParticleClassRegistry.getInstance().getSpawnPercentage("example.Smoke"));
        ParticleClassRegistry.getInstance().loadSpawnPercentages(new String[0]);
    }

}
