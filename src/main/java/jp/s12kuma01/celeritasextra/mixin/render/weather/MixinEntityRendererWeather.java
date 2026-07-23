package jp.s12kuma01.celeritasextra.mixin.render.weather;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Toggles rain and snow rendering according to the detail config.
 * <p>
 * In 1.12.2 the weather draw code lives in {@link EntityRenderer#renderRainSnow}. When
 * precipitation rendering is disabled, this mixin cancels that call at {@code HEAD} so no rain
 * or snow particles are drawn.
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRendererWeather {

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
