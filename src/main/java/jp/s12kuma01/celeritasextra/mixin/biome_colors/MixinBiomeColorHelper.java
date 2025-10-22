package jp.s12kuma01.celeritasextra.mixin.biome_colors;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.world.biome.BiomeColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColorHelper.class)
public class MixinBiomeColorHelper {

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
