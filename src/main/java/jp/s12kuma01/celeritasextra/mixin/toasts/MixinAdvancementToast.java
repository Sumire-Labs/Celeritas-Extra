package jp.s12kuma01.celeritasextra.mixin.toasts;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.gui.toasts.AdvancementToast;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Control advancement/achievement toast visibility
 * Returns HIDE visibility when disabled to remove the toast immediately
 */
@Mixin(AdvancementToast.class)
public class MixinAdvancementToast {

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    public void draw(GuiToast toastGui, long delta, CallbackInfoReturnable<IToast.Visibility> cir) {
        if (!CeleritasExtraClientMod.options().extraSettings.advancementToast) {
            cir.setReturnValue(IToast.Visibility.HIDE);
        }
    }
}
