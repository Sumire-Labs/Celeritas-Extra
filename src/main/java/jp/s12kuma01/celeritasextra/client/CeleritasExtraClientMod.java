package jp.s12kuma01.celeritasextra.client;

import jp.s12kuma01.celeritasextra.CeleritasExtraMod;
import jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraGameOptions;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;

/**
 * Client-side entry point that owns Celeritas Extra's configuration.
 * <p>
 * Lazily loads and caches the {@link CeleritasExtraGameOptions} backing the mod's
 * options from {@code config/celeritas-extra.cfg}, creating the {@code config} directory
 * on first use. Invoked from {@link jp.s12kuma01.celeritasextra.CeleritasExtraMod} during
 * initialization, on the client side only.
 */
public class CeleritasExtraClientMod {

    private static CeleritasExtraGameOptions CONFIG;

    /**
     * Returns the mod's client options, loading and caching them on first access.
     *
     * @return the shared {@link CeleritasExtraGameOptions} instance
     */
    public static CeleritasExtraGameOptions options() {
        if (CONFIG == null) {
            CONFIG = loadConfig();
        }
        return CONFIG;
    }

    /**
     * Loads the options from {@code config/celeritas-extra.cfg}, creating the
     * {@code config} directory first if it does not yet exist.
     *
     * @return the freshly loaded {@link CeleritasExtraGameOptions}
     */
    private static CeleritasExtraGameOptions loadConfig() {
        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        File configFile = new File(configDir, "celeritas-extra.cfg");
        return CeleritasExtraGameOptions.load(configFile);
    }

    /**
     * Initializes the client by eagerly loading the config; called during mod init.
     */
    public static void onClientInit() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            CeleritasExtraMod.LOGGER.info("Initializing Celeritas Extra client...");
            options();
        }
    }
}
