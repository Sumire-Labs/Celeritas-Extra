package jp.s12kuma01.celeritasextra.mixin.render.weather;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls weather rendering (rain/snow)
 * EntityRenderer in 1.12.2 contains the weather rendering code
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    /**
     * Control rain and snow rendering
     * Method signature for 1.12.2: renderRainSnow(float partialTicks)
     */
    @Inject(
        method = "renderRainSnow",
        at = @At("HEAD"),
        cancellable = true
    )
    private void renderRainSnow(float partialTicks, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().detailSettings.rainSnow) {
            ci.cancel();
        }
    }
}
