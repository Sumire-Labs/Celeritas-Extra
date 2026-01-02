package jp.s12kuma01.celeritasextra;

import net.minecraftforge.common.MinecraftForge;
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
    public static final String VERSION = "0.1.13";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @Mod.Instance
    public static CeleritasExtraMod INSTANCE;

    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        if (Loader.isModLoaded("celeritas")) {
            try {
                Class.forName("org.taumc.celeritas.api.OptionGUIConstructionEvent");
                // Register option GUI construction listener
                OptionGUIConstructionEvent.BUS.addListener(jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraOptionsListener::onCeleritasOptionsConstruct);
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

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (net.minecraftforge.fml.common.FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod.onClientInit();
        }
        LOGGER.info("Celeritas Extra initialized");
    }
}
