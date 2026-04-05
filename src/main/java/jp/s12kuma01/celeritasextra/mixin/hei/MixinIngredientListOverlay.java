package jp.s12kuma01.celeritasextra.mixin.hei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hides HEI's item grid overlay until the user types in the search bar.
 * The search bar and config button remain visible at all times.
 * Only applied when HEI is present (controlled by CeleritasExtraMixinConfigPlugin).
 */
@Mixin(targets = "mezz.jei.gui.overlay.IngredientListOverlay")
public class MixinIngredientListOverlay {

    @Shadow
    private GuiTextField searchField;

    @WrapOperation(
            method = "drawScreen",
            at = @At(value = "INVOKE",
                    target = "Lmezz/jei/gui/overlay/IngredientGridWithNavigation;draw(Lnet/minecraft/client/Minecraft;IIF)V"),
            remap = false
    )
    private void celeritasExtra$wrapContentsDraw(Object contents,
                                                  Minecraft minecraft, int mouseX, int mouseY, float partialTicks,
                                                  Operation<Void> original) {
        if (!CeleritasExtraClientMod.options().extraSettings.hideHeiUntilSearch
                || !searchField.getText().isEmpty()) {
            original.call(contents, minecraft, mouseX, mouseY, partialTicks);
        }
    }
}
