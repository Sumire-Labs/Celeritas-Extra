package jp.s12kuma01.celeritasextra.mixin.render.sky;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobalSky {

    @Inject(
        method = "renderSky(FI)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void renderSky(float partialTicks, int pass, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().detailSettings.sky) {
            ci.cancel();
        }
    }
}
