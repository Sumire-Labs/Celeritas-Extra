package com.sumire.celeritasextra.mixin.render.sky;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sumire.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Controls star visibility and the number of stars generated on {@code RenderGlobal}.
 * <p>
 * Two independent changes:
 * - a {@code WrapOperation} around {@code World.getStarBrightness} that returns {@code 0.0f} when
 * the "stars" detail setting is off, suppressing the star pass without rebuilding its geometry, and
 * - a constant modification in Vanilla's star generator that changes only the iteration count.
 * This preserves both Vanilla's VBO and display-list generation paths.
 */
@Mixin(RenderGlobal.class)
public class MixinRenderGlobalStars {

    /**
     * Control star rendering by wrapping the star brightness calculation.
     * Returns 0.0f when stars are disabled, preventing star rendering.
     */
    @WrapOperation(
            method = "renderSky(FI)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/WorldClient;getStarBrightness(F)F"
            )
    )
    private float celeritasExtra$getStarBrightness(WorldClient world, float partialTicks,
                                                    Operation<Float> original) {
        if (!CeleritasExtraClientMod.options().detailSettings.stars) {
            return 0.0f;
        }
        return original.call(world, partialTicks);
    }

    @ModifyConstant(
            method = "renderStars(Lnet/minecraft/client/renderer/BufferBuilder;)V",
            constant = @Constant(intValue = 1500)
    )
    private int celeritasExtra$starCount(int vanillaCount) {
        return CeleritasExtraClientMod.options().detailSettings.totalStars;
    }
}
