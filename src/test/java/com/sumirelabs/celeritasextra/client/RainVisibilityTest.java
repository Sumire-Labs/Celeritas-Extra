/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client;

import org.junit.jupiter.api.Test;

import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.math.AxisAlignedBB;

import static org.junit.jupiter.api.Assertions.*;

class RainVisibilityTest {

    static class Camera implements ICamera {

        AxisAlignedBB box;
        boolean visible;
        public boolean isBoundingBoxInFrustum(AxisAlignedBB box) {
            this.box = box;
            return visible;
        }
        public void setPosition(double x, double y, double z) {}

    }

    @Test
    void checksTheFullColumnAndFallsBackWithoutAView() {
        var camera = new Camera();
        assertTrue(RainVisibility.visible(null, 1, 2, 64, 256, 70, 10));
        assertFalse(RainVisibility.visible(camera, -3, 7, 64, 256, 70, 10));
        assertEquals(new AxisAlignedBB(-3, 64, 7, -2, 256, 8), camera.box);
        camera.visible = true;
        assertTrue(RainVisibility.visible(camera, -3, 7, 64, 256, 70, 10));
    }

    @Test
    void cameraAboveBuildHeightIsStillCovered() {
        var camera = new Camera();
        RainVisibility.visible(camera, 1, 2, 64, 256, 400, 10);
        assertEquals(410, camera.box.maxY);
    }

    @Test
    void rejectedColumnsBecomeZeroHeightBeforeVertexGeneration() {
        for (int precipitation : new int[]{0, 64, 90, 256})
            for (int radius : new int[]{5, 10}) {
                int height = RainVisibility.culledHeight(precipitation, 70, radius);
                assertEquals(Math.max(70 - radius, height), Math.max(70 + radius, height));
            }
    }

}
