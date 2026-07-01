package jp.s12kuma01.celeritasextra.mixin.hei;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.input.IClickedIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hides HEI's item grid overlay until the user types in the search bar.
 * <p>
 * The search bar and config button stay visible and interactive at all times, but while the
 * grid is hidden it is made fully <em>inert</em>: it does not draw, show tooltips, respond to
 * clicks/scroll/keyboard, or expose any ingredient. This prevents the reported issues where the
 * "hidden" list still rendered and where clicking the now-empty area opened the recipe viewer
 * (JEI's InputHandler asks the overlay for {@code getIngredientUnderMouse}, so that must return
 * null while hidden).
 * <p>
 * Note: we deliberately do NOT force {@code isListDisplayed()} to false — that field also gates
 * the search bar/config button, so disabling it would make the feature impossible to turn off.
 * Only applied when HEI/JEI is present (controlled by CeleritasExtraMixinConfigPlugin).
 */
@Mixin(targets = "mezz.jei.gui.overlay.IngredientListOverlay")
public class MixinIngredientListOverlay {

    private static final int HINT_TEXT_COLOR = 0x80FFFFFF;

    @Unique
    private static boolean celeritasExtra$isHidden() {
        return CeleritasExtraClientMod.options().extraSettings.hideHeiUntilSearch
                && Config.getFilterText().isEmpty();
    }

    // ------------------------------------------------------------------
    // Rendering: hide the grid + its tooltips, show a search hint instead
    // ------------------------------------------------------------------

    @WrapOperation(
            method = "drawScreen",
            at = @At(value = "INVOKE",
                    target = "Lmezz/jei/gui/overlay/IngredientGridWithNavigation;draw(Lnet/minecraft/client/Minecraft;IIF)V"),
            remap = false
    )
    private void celeritasExtra$wrapContentsDraw(IngredientGridWithNavigation contents,
                                                 Minecraft minecraft, int mouseX, int mouseY, float partialTicks,
                                                 Operation<Void> original) {
        if (!celeritasExtra$isHidden()) {
            original.call(contents, minecraft, mouseX, mouseY, partialTicks);
        } else {
            var area = contents.getArea();
            var hint = I18n.format("celeritasextra.overlay.hei_hidden");
            int textWidth = minecraft.fontRenderer.getStringWidth(hint);
            int x = area.x + (area.width - textWidth) / 2;
            int y = area.y + area.height / 2;
            minecraft.fontRenderer.drawStringWithShadow(hint, x, y, HINT_TEXT_COLOR);
        }
    }

    @WrapOperation(
            method = "drawTooltips",
            at = @At(value = "INVOKE",
                    target = "Lmezz/jei/gui/overlay/IngredientGridWithNavigation;drawTooltips(Lnet/minecraft/client/Minecraft;II)V"),
            remap = false
    )
    private void celeritasExtra$wrapContentsDrawTooltips(IngredientGridWithNavigation contents,
                                                         Minecraft minecraft, int mouseX, int mouseY,
                                                         Operation<Void> original) {
        if (!celeritasExtra$isHidden()) {
            original.call(contents, minecraft, mouseX, mouseY);
        }
    }

    // ------------------------------------------------------------------
    // Input: make the hidden grid inert (search bar/config button untouched)
    // ------------------------------------------------------------------

    /** The recipe-focus source JEI's InputHandler queries on click / recipe key — null hides it. */
    @Inject(
            method = "getIngredientUnderMouse(II)Lmezz/jei/input/IClickedIngredient;",
            at = @At("HEAD"), cancellable = true, remap = false
    )
    private void celeritasExtra$noClickedIngredient(int mouseX, int mouseY, CallbackInfoReturnable<IClickedIngredient<?>> cir) {
        if (celeritasExtra$isHidden()) {
            cir.setReturnValue(null);
        }
    }

    /** Public API used by other mods to read the ingredient under the mouse. */
    @Inject(
            method = "getIngredientUnderMouse()Ljava/lang/Object;",
            at = @At("HEAD"), cancellable = true, remap = false
    )
    private void celeritasExtra$noIngredientObject(CallbackInfoReturnable<Object> cir) {
        if (celeritasExtra$isHidden()) {
            cir.setReturnValue(null);
        }
    }

    /** Ghost-drag source + focus element — null while hidden. */
    @Inject(method = "getElementUnderMouse", at = @At("HEAD"), cancellable = true, remap = false)
    private void celeritasExtra$noElementUnderMouse(CallbackInfoReturnable<IIngredientListElement> cir) {
        if (celeritasExtra$isHidden()) {
            cir.setReturnValue(null);
        }
    }

    /** Don't report any visible ingredients while hidden. */
    @Inject(method = "getVisibleIngredients", at = @At("HEAD"), cancellable = true, remap = false)
    private void celeritasExtra$noVisibleIngredients(CallbackInfoReturnable<ImmutableList<Object>> cir) {
        if (celeritasExtra$isHidden()) {
            cir.setReturnValue(ImmutableList.of());
        }
    }

    @Inject(method = "canSetFocusWithMouse", at = @At("HEAD"), cancellable = true, remap = false)
    private void celeritasExtra$noFocusWithMouse(CallbackInfoReturnable<Boolean> cir) {
        if (celeritasExtra$isHidden()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "handleMouseScrolled", at = @At("HEAD"), cancellable = true, remap = false)
    private void celeritasExtra$noScroll(int mouseX, int mouseY, int scrollDelta, CallbackInfoReturnable<Boolean> cir) {
        if (celeritasExtra$isHidden()) {
            cir.setReturnValue(false);
        }
    }

    /** Swallow grid clicks (e.g. invisible navigation buttons) while hidden; search/config still run. */
    @WrapOperation(
            method = "handleMouseClicked",
            at = @At(value = "INVOKE",
                    target = "Lmezz/jei/gui/overlay/IngredientGridWithNavigation;handleMouseClicked(III)Z"),
            remap = false
    )
    private boolean celeritasExtra$wrapContentsClick(IngredientGridWithNavigation contents,
                                                     int mouseX, int mouseY, int mouseButton,
                                                     Operation<Boolean> original) {
        if (celeritasExtra$isHidden()) {
            return false;
        }
        return original.call(contents, mouseX, mouseY, mouseButton);
    }

    /** Swallow grid keyboard navigation while hidden; typing in the (focused) search bar still works. */
    @WrapOperation(
            method = "onKeyPressed",
            at = @At(value = "INVOKE",
                    target = "Lmezz/jei/gui/overlay/IngredientGridWithNavigation;onKeyPressed(CI)Z"),
            remap = false
    )
    private boolean celeritasExtra$wrapContentsKey(IngredientGridWithNavigation contents,
                                                   char typedChar, int keyCode,
                                                   Operation<Boolean> original) {
        if (celeritasExtra$isHidden()) {
            return false;
        }
        return original.call(contents, typedChar, keyCode);
    }
}
