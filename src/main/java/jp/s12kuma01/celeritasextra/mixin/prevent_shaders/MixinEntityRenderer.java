package jp.s12kuma01.celeritasextra.mixin.prevent_shaders;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(
        method = "loadShader",
        at = @At("HEAD"),
        cancellable = true
    )
    private void preventLoadShader(ResourceLocation resourceLocationIn, CallbackInfo ci) {
        if (CeleritasExtraClientMod.options().renderSettings.preventShaders) {
            ci.cancel();
        }
    }

    @Inject(
        method = "switchUseShader",
        at = @At("HEAD"),
        cancellable = true
    )
    private void preventSwitchUseShader(CallbackInfo ci) {
        if (CeleritasExtraClientMod.options().renderSettings.preventShaders) {
            ci.cancel();
        }
    }
}
