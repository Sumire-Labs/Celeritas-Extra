package com.sumirelabs.celeritasextra.mixin.particle;

import com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses the rain-splash particle pass when particles or rain splashes are disabled.
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRendererRainSplash {

    @Inject(method = "addRainParticles()V", at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$addRainParticles(CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().particleSettings.particles
                || !CeleritasExtraClientMod.options().particleSettings.rainSplash) {
            ci.cancel();
        }
    }
}
