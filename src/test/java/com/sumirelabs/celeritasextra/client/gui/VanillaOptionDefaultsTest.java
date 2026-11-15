/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import org.junit.jupiter.api.Test;
import org.taumc.celeritas.api.options.structure.StandardOptions;

import static org.junit.jupiter.api.Assertions.*;

class VanillaOptionDefaultsTest {

    @Test
    void nativeDefaultsUseTheCorrectCeleritasValueTypesAndArchitecture() {
        assertEquals(12, VanillaOptionDefaults.find(StandardOptions.Option.RENDER_DISTANCE, true));
        assertEquals(8, VanillaOptionDefaults.find(StandardOptions.Option.RENDER_DISTANCE, false));
        assertEquals(0, VanillaOptionDefaults.find(StandardOptions.Option.BRIGHTNESS, true));
        assertEquals(120, VanillaOptionDefaults.find(StandardOptions.Option.MAX_FRAMERATE, true));
        assertEquals(true, VanillaOptionDefaults.find(StandardOptions.Option.VSYNC, true));
        assertEquals(true, VanillaOptionDefaults.find(StandardOptions.Option.GRAPHICS_MODE, true));
        assertEquals(2, VanillaOptionDefaults.find(StandardOptions.Option.CLOUDS, true));
        assertEquals(2, VanillaOptionDefaults.find(StandardOptions.Option.SMOOTH_LIGHT, true));
        assertEquals(4, VanillaOptionDefaults.find(StandardOptions.Option.MIPMAP_LEVEL, true));
        assertNull(VanillaOptionDefaults.find(StandardOptions.Option.SIMULATION_DISTANCE, true));
        assertNull(VanillaOptionDefaults.find(null, true));
    }

}
