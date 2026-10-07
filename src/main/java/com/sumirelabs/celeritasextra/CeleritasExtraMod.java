package com.sumirelabs.celeritasextra;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Main mod entry point for Celeritas Extra, a client-only renderer companion.
 * <p>
 * Celeritas Extra layers additional rendering options on top of Celeritas and surfaces
 * them inside the renderer's own options GUI. This class drives the Forge lifecycle: during
 * construction it selects Celeritas, Actinium or Nothirium, then registers the
 * option-GUI construction listeners; during initialization it bootstraps the client
 * configuration.
 * <p>
 * The mod is {@code clientSideOnly} and accepts any remote version, so it can join
 * servers that do not have it installed.
 */
@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION,
        clientSideOnly = true, acceptableRemoteVersions = "*",
        dependencies = "required-after:cleanroom@[0.6.10-alpha,);after:celeritas;after:actinium;after:nothirium@[0.4.9-beta,);"
                + "after:assetmover@[2.5,)")
public class CeleritasExtraMod {

    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    @Mod.Instance
    public static CeleritasExtraMod INSTANCE;

    private File configDirectory;

    /**
     * Wires Celeritas Extra into the installed renderer's options GUI during mod construction.
     * <p>
     * Optional ordering loads either supported renderer first. The adapters are isolated
     * so the absent renderer's API is never resolved during registration.
     *
     * @param event the Forge construction event
     */
    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        boolean actinium = Loader.isModLoaded("actinium");
        boolean celeritas = Loader.isModLoaded("celeritas");
        boolean nothirium = Loader.isModLoaded("nothirium");
        if ((actinium ? 1 : 0) + (celeritas ? 1 : 0) + (nothirium ? 1 : 0) != 1) {
            throw new IllegalStateException("Celeritas Extra requires exactly one renderer: Celeritas, Actinium or Nothirium");
        }
        if (Loader.isModLoaded("assetmover")) {
            try {
                com.sumirelabs.celeritasextra.compat.assetmover.AssetMoverCompat
                        .registerModernCloudTexture();
                LOGGER.info("Requested the Minecraft 1.21.6 cloud texture through AssetMover");
            } catch (RuntimeException | LinkageError throwable) {
                LOGGER.error("AssetMover integration failed; the modern cloud texture was not requested",
                        throwable);
            }
        } else {
            LOGGER.info("AssetMover is not installed; the modern cloud texture will not be downloaded");
        }

        if (actinium) {
            com.sumirelabs.celeritasextra.compat.actinium.ActiniumOptionsAdapter.register();
        } else if (nothirium) {
            com.sumirelabs.celeritasextra.compat.nothirium.NothiriumOptionsAdapter.register();
        } else if (!com.sumirelabs.celeritasextra.compat.CeleritasGuiCompatibility.hasControllerGui()) {
            com.sumirelabs.celeritasextra.compat.pintonium.PintoniumOptionsAdapter.register();
            LOGGER.info("Using standalone Extra settings for the renderer's independent video GUI");
        } else {
            com.sumirelabs.celeritasextra.compat.CeleritasOptionsAdapter.register();
        }
        LOGGER.info("Registered Celeritas Extra with {} GUI", actinium ? "Actinium" : nothirium ? "Nothirium" : "Celeritas");
    }

    /**
     * Captures Forge's canonical configuration directory before client initialization.
     *
     * @param event the Forge pre-initialization event
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        this.configDirectory = event.getModConfigurationDirectory();
        com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod.setConfigDirectory(this.configDirectory);
        LOGGER.info("Celeritas Extra pre-initialization");
    }

    /**
     * Bootstraps the client on the effective client side during Forge initialization.
     * <p>
     * Delegates to {@link com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod#onClientInit(File)}
     * so dedicated-server environments never load client-only classes.
     *
     * @param event the Forge initialization event
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (net.minecraftforge.fml.common.FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod
                    .onClientInit(this.configDirectory);
        }
        LOGGER.info("Celeritas Extra initialized");
    }
}
