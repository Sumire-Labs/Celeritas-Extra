package jp.s12kuma01.celeritasextra.mixin.render.sky;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls cloud rendering and height
 * RenderGlobal is the 1.12.2 equivalent of WorldRenderer from 1.20.1
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

    /**
     * Modify cloud height
     * In 1.12.2, the y parameter is passed to renderClouds and used for cloud positioning
     */
    @ModifyVariable(
        method = "renderClouds",
        at = @At("HEAD"),
        ordinal = 1,
        argsOnly = true
    )
    private double modifyCloudHeight(double y) {
        return CeleritasExtraClientMod.options().renderSettings.cloudHeight;
    }
}
