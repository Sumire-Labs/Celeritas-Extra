package jp.s12kuma01.celeritasextra.mixin.particle;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls rain-splash particles on {@link RenderGlobal}.
 * {@code RenderGlobal} is the 1.12.2 equivalent of WorldRenderer from 1.20.1.
 * <p>
 * Cancels {@code addRainParticles} when particles are disabled globally or the rain-splash toggle is
 * off, suppressing the small water splashes that rain spawns on the ground.
 */
@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    /**
     * Control rain splash particles
     */
    @Inject(method = "addRainParticles", at = @At("HEAD"), cancellable = true)
    private void addRainParticles(CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().particleSettings.particles ||
                !CeleritasExtraClientMod.options().particleSettings.rainSplash) {
            ci.cancel();
        }
    }
}
