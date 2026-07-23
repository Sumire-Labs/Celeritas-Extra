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
 * Controls world light recalculation on {@link World} for performance.
 * <p>
 * On the client, short-circuits {@code checkLight} and {@code checkLightFor} when the light-updates
 * toggle is off, skipping lighting recomputation. This can improve performance in exchange for
 * possible lighting artifacts until light is recalculated. Server-side worlds are unaffected.
 */
@Mixin(World.class)
public class MixinWorld {

    @Inject(method = "checkLightFor", at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$onCheckLightFor(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        World self = (World) (Object) this;
        if (self.isRemote && !CeleritasExtraClientMod.options().renderSettings.lightUpdates) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "checkLight", at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$onCheckLight(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        World self = (World) (Object) this;
        if (self.isRemote && !CeleritasExtraClientMod.options().renderSettings.lightUpdates) {
            cir.setReturnValue(false);
        }
    }
}
