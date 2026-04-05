package jp.s12kuma01.celeritasextra.mixin.hei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import mezz.jei.config.Config;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hides HEI's item grid overlay until the user types in the search bar.
 * The search bar and config button remain visible at all times.
 * Only applied when HEI is present (controlled by CeleritasExtraMixinConfigPlugin).
 */
@Mixin(targets = "mezz.jei.gui.overlay.IngredientListOverlay")
public class MixinIngredientListOverlay {

    private static final int HINT_TEXT_COLOR = 0x80FFFFFF;

    @WrapOperation(
            method = "drawScreen",
            at = @At(value = "INVOKE",
                    target = "Lmezz/jei/gui/overlay/IngredientGridWithNavigation;draw(Lnet/minecraft/client/Minecraft;IIF)V"),
            remap = false
    )
    private void celeritasExtra$wrapContentsDraw(IngredientGridWithNavigation contents,
                                                  Minecraft minecraft, int mouseX, int mouseY, float partialTicks,
                                                  Operation<Void> original) {
        if (!CeleritasExtraClientMod.options().extraSettings.hideHeiUntilSearch
                || !Config.getFilterText().isEmpty()) {
            original.call(contents, minecraft, mouseX, mouseY, partialTicks);
        } else {
            var resolution = new ScaledResolution(minecraft);
            int screenWidth = resolution.getScaledWidth();
            var hint = I18n.format("celeritasextra.overlay.hei_hidden");
            int textWidth = minecraft.fontRenderer.getStringWidth(hint);
            int x = screenWidth - textWidth - 10;
            int y = resolution.getScaledHeight() / 2;
            minecraft.fontRenderer.drawStringWithShadow(hint, x, y, HINT_TEXT_COLOR);
        }
    }
}
