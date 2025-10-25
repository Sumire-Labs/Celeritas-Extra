package jp.s12kuma01.celeritasextra.mixin.biome_colors;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.world.biome.BiomeColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Biome color control - disable biome-specific colors
 * Port of embeddium-extra's biome_colors.MixinBiomeColors
 *
 * In 1.20.1: BiomeColors class
 * In 1.12.2: BiomeColorHelper class
 *
 * Default colors:
 * - Grass: 9551193 (0x91BD59)
 * - Water: 4159204 (0x3F76E4)
 * - Foliage: 5877296 (0x59AE30)
 */
@Mixin(BiomeColorHelper.class)
public class MixinBiomeColorHelper {

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
            cir.setReturnValue(9551193);
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
            cir.setReturnValue(4159204);
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
            cir.setReturnValue(5877296);
        }
    }
}
