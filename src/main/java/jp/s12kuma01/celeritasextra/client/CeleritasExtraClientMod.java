package jp.s12kuma01.celeritasextra.client;

import java.io.File;

import jp.s12kuma01.celeritasextra.CeleritasExtraMod;
import jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraGameOptions;

import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * Client-side initialization and configuration management for Celeritas Extra.
 */
public class CeleritasExtraClientMod {

    private static CeleritasExtraGameOptions CONFIG;

    /**
     * Gets the mod configuration, loading it lazily if needed.
     *
     * @return The configuration instance
     */
    public static CeleritasExtraGameOptions options() {
        if (CONFIG == null) {
            CONFIG = loadConfig();
        }
        return CONFIG;
    }

    /**
     * Loads configuration from disk.
     * Creates config directory if it doesn't exist.
     *
     * @return Loaded configuration instance
     */
    private static CeleritasExtraGameOptions loadConfig() {
        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        File configFile = new File(configDir, "celeritas-extra-options.json");
        return CeleritasExtraGameOptions.load(configFile);
    }

    /**
     * Called during client initialization.
     * Triggers configuration loading.
     */
    public static void onClientInit() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            CeleritasExtraMod.LOGGER.info("Initializing Celeritas Extra client...");
            options(); // Load config
        }
    }
}
