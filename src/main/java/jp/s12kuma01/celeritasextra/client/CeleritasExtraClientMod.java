package jp.s12kuma01.celeritasextra.client;

import jp.s12kuma01.celeritasextra.CeleritasExtraMod;
import jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraGameOptions;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;

/**
 * Client-side handler for Celeritas Extra
 */
public class CeleritasExtraClientMod {

    private static CeleritasExtraGameOptions CONFIG;

    public static CeleritasExtraGameOptions options() {
        if (CONFIG == null) {
            CONFIG = loadConfig();
        }
        return CONFIG;
    }

    private static CeleritasExtraGameOptions loadConfig() {
        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        File configFile = new File(configDir, "celeritas-extra-options.json");
        return CeleritasExtraGameOptions.load(configFile);
    }

    public static void onClientInit() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            CeleritasExtraMod.LOGGER.info("Initializing Celeritas Extra client...");
            // Load configuration
            options();
        }
    }
}
