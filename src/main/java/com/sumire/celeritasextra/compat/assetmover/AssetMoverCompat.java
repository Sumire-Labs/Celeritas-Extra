package com.sumire.celeritasextra.compat.assetmover;

import com.cleanroommc.assetmover.AssetMoverAPI;

import java.util.Collections;

/**
 * Optional AssetMover bridge. Keep every direct AssetMover reference in this class so a normal
 * Celeritas Extra installation does not try to resolve AssetMover classes when the mod is absent.
 */
public final class AssetMoverCompat {

    private static final String MODERN_MINECRAFT_VERSION = "1.21.6";
    private static final String SOURCE_CLOUD_TEXTURE =
            "assets/minecraft/textures/environment/clouds.png";
    private static final String TARGET_CLOUD_TEXTURE =
            "assets/celeritasextra/textures/environment/clouds_1_21_6.png";

    private AssetMoverCompat() {
    }

    /**
     * Requests the updated cloud pattern from Minecraft 1.21.6. AssetMover requires this call no
     * later than {@code FMLConstructionEvent}; the main mod therefore invokes it from construction
     * only after Forge has confirmed that the optional mod is loaded.
     */
    public static void registerModernCloudTexture() {
        AssetMoverAPI.fromMinecraft(MODERN_MINECRAFT_VERSION, Collections.singletonMap(
                SOURCE_CLOUD_TEXTURE, TARGET_CLOUD_TEXTURE));
    }
}
