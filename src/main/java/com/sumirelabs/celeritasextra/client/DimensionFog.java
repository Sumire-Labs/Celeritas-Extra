/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client;

import com.sumirelabs.celeritasextra.client.gui.CeleritasExtraGameOptions.ResolvedFog;

import net.minecraft.client.Minecraft;

public final class DimensionFog {

    private DimensionFog() {}

    public static ResolvedFog current() {
        var world = Minecraft.getMinecraft().world;
        var settings = CeleritasExtraClientMod.options().renderSettings;
        return world == null
            ? new ResolvedFog(settings.fog, settings.fogStart, settings.fogDistance)
            : settings.resolveFog(world.provider.getDimension());
    }

}
