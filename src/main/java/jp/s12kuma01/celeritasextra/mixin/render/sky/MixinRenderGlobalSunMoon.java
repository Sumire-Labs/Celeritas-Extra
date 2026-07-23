package jp.s12kuma01.celeritasextra.mixin.render.sky;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Toggles rendering of the sun and moon discs.
 * <p>
 * Wraps the two internal {@code renderSky(BufferBuilder, float, float, float, boolean)} helper
 * invocations inside {@code RenderGlobal.renderSky(FI)V} — ordinal 0 draws the sun, ordinal 1 the
 * moon — and skips the wrapped call when the "sunMoon" detail setting is disabled, so both discs
 * vanish while the rest of the sky box is left intact.
 */
@Mixin(RenderGlobal.class)
public class MixinRenderGlobalSunMoon {

    /**
     * Control sun rendering by wrapping the bindTexture call for sun texture
     * In 1.12.2, the sun texture path is "textures/environment/sun.png"
     */
    @WrapOperation(
            method = "renderSky(FI)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderGlobal;renderSky(Lnet/minecraft/client/renderer/BufferBuilder;FFZ)V",
                    ordinal = 0
            )
    )
    private void wrapRenderSun(RenderGlobal instance, BufferBuilder buffer, float r, float g, float b, Operation<Void> original) {
        if (CeleritasExtraClientMod.options().detailSettings.sunMoon) {
            original.call(instance, buffer, r, g, b);
        }
    }

    /**
     * Control moon rendering by wrapping the bindTexture call for moon texture
     * In 1.12.2, the moon texture path is "textures/environment/moon_phases.png"
     */
    @WrapOperation(
            method = "renderSky(FI)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderGlobal;renderSky(Lnet/minecraft/client/renderer/BufferBuilder;FFZ)V",
                    ordinal = 1
            )
    )
    private void wrapRenderMoon(RenderGlobal instance, BufferBuilder buffer, float r, float g, float b, Operation<Void> original) {
        if (CeleritasExtraClientMod.options().detailSettings.sunMoon) {
            original.call(instance, buffer, r, g, b);
        }
    }
}
