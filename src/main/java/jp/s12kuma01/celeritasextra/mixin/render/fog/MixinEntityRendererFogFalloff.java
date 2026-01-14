package jp.s12kuma01.celeritasextra.mixin.render.fog;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraGameOptions;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Fog falloff control - adjusts fog start and end distances
 * Port of embeddium-extra's fog_falloff.MixinBackgroundRenderer
 *
 * In 1.20.1: RenderSystem.setShaderFogStart/End()
 * In 1.12.2: GlStateManager.setFogStart/End()
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRendererFogFalloff {

    /**
     * Modify fog start distance based on user setting
     * fogStart = 100 means default (no change)
     * fogStart < 100 means fog starts closer (more foggy)
     * fogStart > 100 means fog starts farther (less foggy)
     */
    @ModifyArg(
        method = "setupFog",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GlStateManager;setFogStart(F)V"
        ),
        index = 0
    )
    private float modifyFogStart(float original) {
        // If fog type is OFF, push fog start very far
        if (CeleritasExtraClientMod.options().renderSettings.fogType == CeleritasExtraGameOptions.FogType.OFF) {
            return Float.MAX_VALUE;
        }

        int fogDistance = CeleritasExtraClientMod.options().renderSettings.fogDistance;
        float fogStartPercent = (float) CeleritasExtraClientMod.options().renderSettings.fogStart / 100.0f;

        // If fogDistance is set (> 0), calculate fog start based on chunks
        if (fogDistance > 0) {
            return fogDistance * 16.0f * fogStartPercent;
        }

        // Default: use original with fogStart multiplier
        return original * fogStartPercent;
    }

    /**
     * Modify fog end distance based on user setting
     * fogDistance = 0 means use default (render distance)
     * fogDistance > 0 means use specified chunk distance
     */
    @ModifyArg(
        method = "setupFog",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GlStateManager;setFogEnd(F)V"
        ),
        index = 0
    )
    private float modifyFogEnd(float original) {
        // If fog type is OFF, push fog end very far
        if (CeleritasExtraClientMod.options().renderSettings.fogType == CeleritasExtraGameOptions.FogType.OFF) {
            return Float.MAX_VALUE;
        }

        int fogDistance = CeleritasExtraClientMod.options().renderSettings.fogDistance;

        // If fogDistance is set (> 0), calculate fog end based on chunks
        if (fogDistance > 0) {
            return (fogDistance + 1) * 16.0f;
        }

        // Default: use original value
        return original;
    }
}
