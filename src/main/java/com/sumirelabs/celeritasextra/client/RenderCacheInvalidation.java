/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public final class RenderCacheInvalidation {

    private RenderCacheInvalidation() {}

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        // Both caches are owned by the render thread, including the LOD cache's access-order map.
        Minecraft.getMinecraft().addScheduledTask(() -> {
            ItemFrameLodState.clearCache();
            StaticItemVertexCache.INSTANCE.clear();
        });
    }

}
