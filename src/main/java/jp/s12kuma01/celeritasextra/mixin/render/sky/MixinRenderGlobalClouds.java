package jp.s12kuma01.celeritasextra.mixin.render.sky;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls cloud rendering
 * RenderGlobal is the 1.12.2 equivalent of WorldRenderer from 1.20.1
 * Cloud height is controlled via MixinWorldProvider
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
