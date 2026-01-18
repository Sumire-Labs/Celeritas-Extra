package jp.s12kuma01.celeritasextra.mixin.light_updates;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Controls light updates for performance optimization.
 * When disabled, light will not be recalculated which can improve performance
 * but may cause visual artifacts.
 */
@Mixin(World.class)
public class MixinWorld {

    @Inject(method = "checkLightFor", at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$onCheckLightFor(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        World self = (World) (Object) this;
        // Only apply on client side
        if (self.isRemote && !CeleritasExtraClientMod.options().renderSettings.lightUpdates) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "checkLight", at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$onCheckLight(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        World self = (World) (Object) this;
        // Only apply on client side
        if (self.isRemote && !CeleritasExtraClientMod.options().renderSettings.lightUpdates) {
            cir.setReturnValue(false);
        }
    }
}
