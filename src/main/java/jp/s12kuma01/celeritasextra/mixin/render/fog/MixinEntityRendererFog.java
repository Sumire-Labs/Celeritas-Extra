package jp.s12kuma01.celeritasextra.mixin.render.fog;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Simple fog control
 * In 1.12.2, fog is controlled in EntityRenderer's setupFog methods
 * This is a simplified version - full fog control would be more complex
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRendererFog {

    /**
     * Control fog rendering (simplified version)
     * This cancels the fog setup, effectively disabling fog
     */
    @Inject(
        method = "setupFog",
        at = @At("HEAD"),
        cancellable = true
    )
    private void setupFog(int startCoords, float partialTicks, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().renderSettings.fog) {
            ci.cancel();
        }
    }
}
