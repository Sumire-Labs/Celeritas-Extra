package com.sumirelabs.celeritasextra.client.gui;

import net.minecraftforge.common.ForgeEarlyConfig;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ScreenModePersistenceTest {
    @TempDir Path directory;

    @Test void borderlessAndExclusivePreferencesSurviveConfigReload() throws Exception {
        var homeField = FMLInjectionData.class.getDeclaredField("minecraftHome");
        var configsField = ConfigManager.class.getDeclaredField("CLASS_TO_CONFIG");
        homeField.setAccessible(true);
        configsField.setAccessible(true);
        Object previousHome = homeField.get(null);
        boolean previousBorderless = ForgeEarlyConfig.WINDOW_BORDERLESS_REPLACES_FULLSCREEN;
        @SuppressWarnings("unchecked")
        var configs = (Map<Class<?>, Configuration>) configsField.get(null);
        var previousConfig = configs.get(ForgeEarlyConfig.class);
        var file = directory.resolve("forge_early.cfg").toFile();
        try {
            homeField.set(null, directory.toFile());
            configs.put(ForgeEarlyConfig.class, new Configuration(file));
            String category = ForgeEarlyConfig.class.getAnnotation(Config.class).category();
            for (var mode : new CeleritasExtraGameOptions.ScreenMode[]{
                    CeleritasExtraGameOptions.ScreenMode.BORDERLESS,
                    CeleritasExtraGameOptions.ScreenMode.FULLSCREEN,
                    CeleritasExtraGameOptions.ScreenMode.BORDERLESS,
                    CeleritasExtraGameOptions.ScreenMode.WINDOWED}) {
                CeleritasExtraGameOptions.ScreenMode.saveBorderlessPreference(mode);
                var reloaded = new Configuration(file);
                reloaded.load();
                assertEquals(mode == CeleritasExtraGameOptions.ScreenMode.BORDERLESS,
                        reloaded.get(category, "WINDOW_BORDERLESS_REPLACES_FULLSCREEN", false).getBoolean(),
                        "Cleanroom's on-disk preference after selecting " + mode);
            }
        } finally {
            ForgeEarlyConfig.WINDOW_BORDERLESS_REPLACES_FULLSCREEN = previousBorderless;
            homeField.set(null, previousHome);
            if (previousConfig == null) configs.remove(ForgeEarlyConfig.class);
            else configs.put(ForgeEarlyConfig.class, previousConfig);
        }
    }
}
