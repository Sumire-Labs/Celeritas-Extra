package jp.s12kuma01.celeritasextra.mixin.render.sky;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraGameOptions.CloudTranslucency;
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
 * - DEFAULT: use configured cloud height as the threshold
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
        return switch (options.renderSettings.cloudTranslucency) {
            case DEFAULT -> options.renderSettings.cloudHeight;
            case ALWAYS -> Double.NEGATIVE_INFINITY;
            case NEVER -> Double.POSITIVE_INFINITY;
        };
    }
}
