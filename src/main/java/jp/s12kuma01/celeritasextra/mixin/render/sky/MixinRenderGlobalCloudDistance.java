package jp.s12kuma01.celeritasextra.mixin.render.sky;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Controls cloud render distance
 * In 1.12.2, clouds are rendered in a loop with a fixed range
 * We modify the constant that determines this range
 */
@Mixin(RenderGlobal.class)
public class MixinRenderGlobalCloudDistance {

    /**
     * Modify the cloud render distance constant
     * In 1.12.2, clouds typically render 12 chunks in each direction
     * We scale this value based on the cloudDistance setting
     */
    @ModifyConstant(
        method = "renderClouds",
        constant = @Constant(intValue = 12)
    )
    private int modifyCloudRenderRange(int original) {
        int cloudDistance = CeleritasExtraClientMod.options().renderSettings.cloudDistance;
        return original * cloudDistance / 100;
    }
}
