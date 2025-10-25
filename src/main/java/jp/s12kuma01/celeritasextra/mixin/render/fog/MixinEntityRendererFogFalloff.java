package jp.s12kuma01.celeritasextra.mixin.render.fog;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityRenderer.class)
public class MixinEntityRendererFogFalloff {

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
