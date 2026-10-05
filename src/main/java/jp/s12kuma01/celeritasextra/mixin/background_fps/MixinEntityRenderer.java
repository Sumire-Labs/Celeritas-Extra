package jp.s12kuma01.celeritasextra.mixin.background_fps;

import jp.s12kuma01.celeritasextra.client.BackgroundFrameLimiter;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Inject(method = "updateCameraAndRender", at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$skipBackgroundDraw(float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (!BackgroundFrameLimiter.shouldRender()) ci.cancel();
    }
}
