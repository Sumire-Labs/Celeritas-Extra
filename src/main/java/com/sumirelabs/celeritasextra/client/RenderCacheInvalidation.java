package com.sumirelabs.celeritasextra.client;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public final class RenderCacheInvalidation {
    private RenderCacheInvalidation() { }

    @SubscribeEvent public static void onModelBake(ModelBakeEvent event) {
        ItemFrameLodState.clearCache();
    }
}
