package jp.s12kuma01.celeritasextra.mixin.render.sky;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Controls sun and moon rendering
 * In 1.12.2, sun and moon are rendered using texture binding in renderSky
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
