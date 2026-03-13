package jp.s12kuma01.celeritasextra.mixin.render.fog;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraGameOptions;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fog toggle control
 * Disables OpenGL fog after setupFog completes so that all other
 * GL state (fog color, mode, etc.) is still configured correctly
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRendererFog {

    @Inject(
            method = "setupFog",
            at = @At("RETURN")
    )
    private void disableFogAfterSetup(int startCoords, float partialTicks, CallbackInfo ci) {
        CeleritasExtraGameOptions options = CeleritasExtraClientMod.options();
        if (!options.renderSettings.fog || options.renderSettings.fogType == CeleritasExtraGameOptions.FogType.OFF) {
            GlStateManager.disableFog();
        }
    }
}
