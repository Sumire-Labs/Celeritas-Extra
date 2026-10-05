package com.sumire.celeritasextra.mixin.render.sky;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sumire.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * Toggles rendering of the sun and moon discs.
 * <p>
 * Wraps the two {@link Tessellator#draw()} calls bounded by the sun and moon texture fields. When
 * disabled, the corresponding buffers are finished and reset without uploading them, leaving the
 * rest of the sky pass and its GL-state cleanup intact.
 */
@Mixin(RenderGlobal.class)
public class MixinRenderGlobalSunMoon {

    @WrapOperation(
            method = "renderSky(FI)V",
            slice = @Slice(
                    from = @At(value = "FIELD",
                            target = "Lnet/minecraft/client/renderer/RenderGlobal;SUN_TEXTURES:Lnet/minecraft/util/ResourceLocation;"),
                    to = @At(value = "FIELD",
                            target = "Lnet/minecraft/client/renderer/RenderGlobal;MOON_PHASES_TEXTURES:Lnet/minecraft/util/ResourceLocation;")
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/Tessellator;draw()V"
            )
    )
    private void celeritasExtra$drawSun(Tessellator tessellator, Operation<Void> original) {
        this.celeritasExtra$drawOrDiscard(tessellator, original, CeleritasExtraClientMod.options().detailSettings.sun);
    }

    @WrapOperation(
            method = "renderSky(FI)V",
            slice = @Slice(
                    from = @At(value = "FIELD",
                            target = "Lnet/minecraft/client/renderer/RenderGlobal;MOON_PHASES_TEXTURES:Lnet/minecraft/util/ResourceLocation;"),
                    to = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/multiplayer/WorldClient;getStarBrightness(F)F")
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/Tessellator;draw()V"
            )
    )
    private void celeritasExtra$drawMoon(Tessellator tessellator, Operation<Void> original) {
        this.celeritasExtra$drawOrDiscard(tessellator, original, CeleritasExtraClientMod.options().detailSettings.moon);
    }

    @Unique
    private void celeritasExtra$drawOrDiscard(Tessellator tessellator, Operation<Void> original, boolean enabled) {
        if (enabled) {
            original.call(tessellator);
        } else {
            tessellator.getBuffer().finishDrawing();
            tessellator.getBuffer().reset();
        }
    }
}
