package jp.s12kuma01.celeritasextra.mixin.render.sky;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldProvider.class)
public class MixinWorldProvider {

    @Inject(method = "getCloudHeight", at = @At("RETURN"), cancellable = true)
    private void modifyCloudHeight(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue((float) CeleritasExtraClientMod.options().renderSettings.cloudHeight);
    }
}
