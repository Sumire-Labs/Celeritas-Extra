/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import java.util.Map;

import org.taumc.celeritas.api.options.OptionIdentifier;
import org.taumc.celeritas.api.options.structure.StandardOptions;

/** Defaults verified against 1.12.2 GameSettings and Celeritas's value conversions. */
final class VanillaOptionDefaults {

    private static final Map<OptionIdentifier<?>, Object> VALUES = Map.ofEntries(
        Map.entry(StandardOptions.Option.BRIGHTNESS, 0), Map.entry(StandardOptions.Option.GUI_SCALE, 0),
        Map.entry(StandardOptions.Option.FULLSCREEN, false), Map.entry(StandardOptions.Option.VSYNC, true),
        Map.entry(StandardOptions.Option.MAX_FRAMERATE, 120), Map.entry(StandardOptions.Option.VIEW_BOBBING, true),
        Map.entry(StandardOptions.Option.ATTACK_INDICATOR, 1), Map.entry(StandardOptions.Option.GRAPHICS_MODE, true),
        Map.entry(StandardOptions.Option.CLOUDS, 2), Map.entry(StandardOptions.Option.PARTICLES, 0),
        Map.entry(StandardOptions.Option.SMOOTH_LIGHT, 2), Map.entry(StandardOptions.Option.ENTITY_SHADOWS, true),
        Map.entry(StandardOptions.Option.MIPMAP_LEVEL, 4));

    private VanillaOptionDefaults() {}

    static Object find(OptionIdentifier<?> id, boolean java64Bit) {
        if (id == null)
            return null;
        if (id.matches(StandardOptions.Option.RENDER_DISTANCE))
            return java64Bit ? 12 : 8;
        for (var entry : VALUES.entrySet())
            if (id.matches(entry.getKey()))
                return entry.getValue();
        return null;
    }

}
