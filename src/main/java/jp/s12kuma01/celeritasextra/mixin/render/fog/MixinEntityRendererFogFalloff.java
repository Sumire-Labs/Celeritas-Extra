package jp.s12kuma01.celeritasextra.mixin.render.fog;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Fog falloff control - adjusts fog start distance
 * Port of embeddium-extra's fog_falloff.MixinBackgroundRenderer
 *
 * In 1.20.1: RenderSystem.setShaderFogStart()
 * In 1.12.2: GlStateManager.setFogStart()
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
        float fogStart = (float) CeleritasExtraClientMod.options().renderSettings.fogStart / 100.0f;
        return original * fogStart;
    }
}
