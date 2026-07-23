package jp.s12kuma01.celeritasextra.mixin.biome_colors;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.world.biome.BiomeColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Disables biome-specific tinting on {@link BiomeColorHelper}.
 * Port of embeddium-extra's biome_colors.MixinBiomeColors.
 * <p>
 * When biome colors are turned off, forces grass, water, and foliage lookups to return fixed default
 * colors instead of the biome-blended values, giving every biome a uniform tint.
 * <p>
 * In 1.20.1 this targets the BiomeColors class; in 1.12.2 it is {@link BiomeColorHelper}.
 */
@Mixin(BiomeColorHelper.class)
public class MixinBiomeColorHelper {

    @Unique
    private static final int DEFAULT_GRASS_COLOR = 0x91BD59;
    @Unique
    private static final int DEFAULT_WATER_COLOR = 0x3F76E4;
    @Unique
    private static final int DEFAULT_FOLIAGE_COLOR = 0x59AE30;

    /**
     * Override grass color with default value when biome colors are disabled
     */
    @Inject(
            method = "getGrassColorAtPos",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void overrideGrassColor(CallbackInfoReturnable<Integer> cir) {
        if (!CeleritasExtraClientMod.options().detailSettings.biomeColors) {
            cir.setReturnValue(DEFAULT_GRASS_COLOR);
        }
    }

    /**
     * Override water color with default value when biome colors are disabled
     */
    @Inject(
            method = "getWaterColorAtPos",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void overrideWaterColor(CallbackInfoReturnable<Integer> cir) {
        if (!CeleritasExtraClientMod.options().detailSettings.biomeColors) {
            cir.setReturnValue(DEFAULT_WATER_COLOR);
        }
    }

    /**
     * Override foliage color with default value when biome colors are disabled
     */
    @Inject(
            method = "getFoliageColorAtPos",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void overrideFoliageColor(CallbackInfoReturnable<Integer> cir) {
        if (!CeleritasExtraClientMod.options().detailSettings.biomeColors) {
            cir.setReturnValue(DEFAULT_FOLIAGE_COLOR);
        }
    }
}
