package com.sumire.celeritasextra.mixin.render.sky;

import com.sumire.celeritasextra.client.CeleritasExtraClientMod;
import com.sumire.celeritasextra.client.CloudPassState;
import com.sumire.celeritasextra.client.gui.CeleritasExtraGameOptions.CloudTranslucency;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Controls cloud translucency behavior.
 * Ported from Angelica's NotFine MixinEntityRenderer (clouds package).
 * <p>
 * In vanilla, clouds become translucent when the player is above y=128.
 * This mixin replaces the hardcoded 128.0D constant to allow:
 * - DEFAULT: use the current dimension's effective cloud height as the threshold
 * - ALWAYS: always translucent (negative infinity)
 * - NEVER: never translucent (positive infinity)
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRendererCloudTranslucency {

    @ModifyConstant(
            method = "renderWorldPass",
            constant = @Constant(doubleValue = 128.0D)
    )
    private double celeritasExtra$modifyCloudTranslucencyCheck(double original) {
        var options = CeleritasExtraClientMod.options();
        CloudTranslucency mode = options.renderSettings.cloudTranslucency;
        if (mode == CloudTranslucency.ALWAYS) {
            return Double.NEGATIVE_INFINITY;
        }
        if (mode == CloudTranslucency.NEVER) {
            return Double.POSITIVE_INFINITY;
        }
        return CloudPassState.cloudHeight((float) original);
    }
}
