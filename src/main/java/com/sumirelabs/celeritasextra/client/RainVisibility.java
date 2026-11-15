/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client;

import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.math.AxisAlignedBB;

/** Tests the precipitation column before vanilla generates its rain/snow vertices. */
public final class RainVisibility {

    private RainVisibility() {}

    public static boolean visible(ICamera camera, int x, int z, int precipitationY, int worldHeight,
        int viewY, int radius) {
        if (camera == null)
            return true;
        // Above the build limit, 1.12 still draws precipitation around the camera.
        double top = Math.max(precipitationY, Math.max(worldHeight, (double) viewY + radius));
        return camera.isBoundingBoxInFrustum(new AxisAlignedBB(x, precipitationY, z, x + 1.0, top, z + 1.0));
    }

    public static int culledHeight(int precipitationY, int viewY, int radius) {
        // Vanilla clamps both ends to this height, then skips the zero-height column.
        return Math.max(precipitationY, viewY + radius);
    }

}
