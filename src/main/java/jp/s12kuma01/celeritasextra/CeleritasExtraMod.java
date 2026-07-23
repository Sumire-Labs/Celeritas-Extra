package jp.s12kuma01.celeritasextra;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.taumc.celeritas.api.OptionGUIConstructionEvent;
import org.taumc.celeritas.api.OptionGroupConstructionEvent;

/**
 * Main mod entry point for Celeritas Extra, a client-only companion to Celeritas.
 * <p>
 * Celeritas Extra layers additional rendering options on top of Celeritas and surfaces
 * them inside Celeritas' own options GUI. This class drives the Forge lifecycle: during
 * construction it verifies Celeritas is present and new enough, then registers the
 * option-GUI construction listeners; during initialization it bootstraps the client
 * configuration.
 * <p>
 * The mod is {@code clientSideOnly} and accepts any remote version, so it can join
 * servers that do not have it installed.
 */
@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION,
        clientSideOnly = true, acceptableRemoteVersions = "*")
public class CeleritasExtraMod {

    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    @Mod.Instance
    public static CeleritasExtraMod INSTANCE;

    /**
     * Wires Celeritas Extra into Celeritas' options GUI during mod construction.
     * <p>
     * Runs only when Celeritas is loaded. Resolving {@link OptionGUIConstructionEvent}
     * confirms the installed Celeritas exposes the extension API (2.4.0 or newer); a
     * {@link NoClassDefFoundError} instead indicates an outdated Celeritas and is logged
     * as such. When Celeritas is absent the mod logs a warning, as it cannot function
     * without it.
     *
     * @param event the Forge construction event
     */
    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        if (Loader.isModLoaded("celeritas")) {
            try {
                Class.forName("org.taumc.celeritas.api.OptionGUIConstructionEvent");
                // Register option GUI construction listener
                OptionGUIConstructionEvent.BUS.addListener(jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraOptionsListener::onCeleritasOptionsConstruct);
                OptionGroupConstructionEvent.BUS.addListener(jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraOptionsListener::onOptionGroupConstruct);
                LOGGER.info("Successfully registered Celeritas Extra with Celeritas GUI");
            } catch (Throwable t) {
                switch (t) {
                    case NoClassDefFoundError _ -> LOGGER.error("Celeritas version is too old, use 2.4.0 or newer");
                    default -> LOGGER.error("Unable to check if Celeritas is up-to-date", t);
                }
            }
        } else {
            LOGGER.warn("Celeritas not found! This mod requires Celeritas to function.");
        }
    }

    /**
     * Handles Forge pre-initialization; currently only logs progress.
     *
     * @param event the Forge pre-initialization event
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Celeritas Extra pre-initialization");
    }

    /**
     * Bootstraps the client on the effective client side during Forge initialization.
     * <p>
     * Delegates to {@link jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod#onClientInit()}
     * so dedicated-server environments never load client-only classes.
     *
     * @param event the Forge initialization event
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (net.minecraftforge.fml.common.FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod.onClientInit();
        }
        LOGGER.info("Celeritas Extra initialized");
    }
}
