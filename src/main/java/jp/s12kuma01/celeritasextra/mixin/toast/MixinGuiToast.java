package jp.s12kuma01.celeritasextra.mixin.toast;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraGameOptions;
import net.minecraft.client.gui.toasts.AdvancementToast;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.RecipeToast;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.gui.toasts.TutorialToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses toast pop-ups (advancement / recipe / tutorial / system) per user config.
 * Inspired by Sodium Extra's toast controls.
 */
@Mixin(GuiToast.class)
public class MixinGuiToast {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$filterToast(IToast toastIn, CallbackInfo ci) {
        CeleritasExtraGameOptions.ExtraSettings settings = CeleritasExtraClientMod.options().extraSettings;

        if (!settings.toasts
                || (toastIn instanceof AdvancementToast && !settings.toastAdvancement)
                || (toastIn instanceof RecipeToast && !settings.toastRecipe)
                || (toastIn instanceof TutorialToast && !settings.toastTutorial)
                || (toastIn instanceof SystemToast && !settings.toastSystem)) {
            ci.cancel();
        }
    }
}
