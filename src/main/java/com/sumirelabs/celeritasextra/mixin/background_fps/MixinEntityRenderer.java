package com.sumirelabs.celeritasextra.mixin.background_fps;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sumirelabs.celeritasextra.client.BackgroundFrameLimiter;

import net.minecraft.client.renderer.EntityRenderer;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(method = "updateCameraAndRender", at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$skipBackgroundDraw(float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (!BackgroundFrameLimiter.shouldRender())
            ci.cancel();
    }

}
