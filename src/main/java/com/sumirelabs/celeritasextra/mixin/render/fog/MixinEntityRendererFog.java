package com.sumirelabs.celeritasextra.mixin.render.fog;

import com.sumirelabs.celeritasextra.client.FogState;
import com.sumirelabs.celeritasextra.client.DimensionFog;
import com.sumirelabs.celeritasextra.compat.ShaderPassCompat;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Toggles OpenGL fog off when fog rendering is disabled in the render config.
 * <p>
 * Runs at the {@code RETURN} of {@link EntityRenderer#setupFog}, after vanilla has finished
 * configuring all surrounding fog GL state (color, mode, start/end). Disabling fog only at this
 * point keeps that state correct while still suppressing the visible effect. Gameplay fog such
 * as blindness, water, and lava is left intact via {@link FogState#isGameplayFog()}.
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRendererFog {

    @Inject(
            method = "setupFog",
            at = @At("RETURN")
    )
    private void disableFogAfterSetup(int startCoords, float partialTicks, CallbackInfo ci) {
        if (!ShaderPassCompat.isShadowPass()
                && !DimensionFog.current().enabled() && !FogState.isGameplayFog()) {
            GlStateManager.disableFog();
        }
    }
}
