package jp.s12kuma01.celeritasextra.mixin.render.sky;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Toggles vanilla cloud rendering on and off.
 * <p>
 * Cancels {@code renderClouds} at its head when the "clouds" render setting is disabled, so no
 * cloud geometry is drawn. {@code RenderGlobal} is the 1.12.2 equivalent of the 1.20.1
 * {@code WorldRenderer}.
 * <p>
 * Related cloud tuning lives elsewhere: cloud height in {@link MixinWorldProvider}, and cloud
 * distance/scale in {@link MixinCloudRenderer}.
 */
@Mixin(RenderGlobal.class)
public class MixinRenderGlobalClouds {

    /**
     * Control cloud rendering
     */
    @Inject(
            method = "renderClouds",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderClouds(float partialTicks, int pass, double x, double y, double z, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().renderSettings.clouds) {
            ci.cancel();
        }
    }
}
