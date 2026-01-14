package jp.s12kuma01.celeritasextra.mixin.toasts;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Master toggle for all toasts
 * Intercepts the add method to prevent toasts from being added when disabled
 */
@Mixin(GuiToast.class)
public class MixinGuiToast {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    public <T extends IToast> void add(T toast, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().extraSettings.toasts) {
            ci.cancel();
        }
    }
}
