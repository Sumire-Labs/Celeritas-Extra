package jp.s12kuma01.celeritasextra.mixin.render.sky;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Toggles the vanilla sky box on and off.
 * <p>
 * Cancels {@code renderSky(FI)V} at its head when the "sky" detail setting is disabled, hiding the
 * sky dome, horizon, and void planes. {@code RenderGlobal} is the 1.12.2 equivalent of the 1.20.1
 * {@code WorldRenderer}.
 * <p>
 * Sun/moon, stars, and clouds are gated independently by {@link MixinRenderGlobalSunMoon},
 * {@link MixinRenderGlobalStars}, and {@link MixinRenderGlobalClouds}.
 */
@Mixin(RenderGlobal.class)
public class MixinRenderGlobalSky {

    /**
     * Control sky rendering
     * In 1.12.2, sky rendering is in renderSky method
     */
    @Inject(
            method = "renderSky(FI)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderSky(float partialTicks, int pass, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().detailSettings.sky) {
            ci.cancel();
        }
    }
}
