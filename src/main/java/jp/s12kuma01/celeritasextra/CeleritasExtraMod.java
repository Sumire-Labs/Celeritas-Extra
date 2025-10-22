package jp.s12kuma01.celeritasextra;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraOptionsListener;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.taumc.celeritas.api.OptionGUIConstructionEvent;

@Mod(modid = CeleritasExtraMod.MOD_ID, name = CeleritasExtraMod.MOD_NAME, version = CeleritasExtraMod.VERSION,
     clientSideOnly = true, acceptableRemoteVersions = "*")
public class CeleritasExtraMod {
    public static final String MOD_ID = "celeritasextra";
    public static final String MOD_NAME = "Celeritas Extra";
    public static final String VERSION = "0.1.0";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @Mod.Instance
    public static CeleritasExtraMod INSTANCE;

    /**
     * Construction phase - registers Celeritas Extra options with Celeritas GUI.
     * Checks for Celeritas 2.4.0+ compatibility by verifying OptionGUIConstructionEvent exists.
     */
    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        if (Loader.isModLoaded("celeritas")) {
            try {
                // Verify Celeritas version is 2.4.0 or newer by checking for API class
                Class.forName("org.taumc.celeritas.api.OptionGUIConstructionEvent");

                // Register our options listener to Celeritas GUI system
                OptionGUIConstructionEvent.BUS.addListener(CeleritasExtraOptionsListener::onCeleritasOptionsConstruct);
                LOGGER.info("Successfully registered Celeritas Extra with Celeritas GUI");
            } catch (Throwable t) {
                if (t instanceof NoClassDefFoundError) {
                    LOGGER.error("Celeritas version is too old, use 2.4.0 or newer");
                } else {
                    LOGGER.error("Unable to check if Celeritas is up-to-date", t);
                }
            }
        } else {
            LOGGER.warn("Celeritas not found! This mod requires Celeritas to function.");
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Celeritas Extra pre-initialization");
    }

    /**
     * Initialization phase - initializes client-side configuration.
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Only initialize on client side (this is a client-only mod)
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            CeleritasExtraClientMod.onClientInit();
        }
        LOGGER.info("Celeritas Extra initialized");
    }
}
