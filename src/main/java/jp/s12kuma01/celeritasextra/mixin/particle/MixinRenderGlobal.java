package jp.s12kuma01.celeritasextra.mixin.particle;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;

import net.minecraft.client.renderer.RenderGlobal;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls rain splash particle rendering.
 */
@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    @Inject(method = "addRainParticles", at = @At("HEAD"), cancellable = true)
    private void addRainParticles(CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().particleSettings.particles ||
            !CeleritasExtraClientMod.options().particleSettings.rainSplash) {
            ci.cancel();
        }
    }
}
