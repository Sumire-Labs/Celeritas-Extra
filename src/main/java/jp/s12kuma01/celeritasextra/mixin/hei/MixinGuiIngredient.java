package jp.s12kuma01.celeritasextra.mixin.hei;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.settings.GameSettings;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Extends the "Advanced Item Tooltips" option to HEI/JEI ingredient tooltips shown for the slots
 * in recipe screens.
 * <p>
 * These tooltips are drawn by {@code GuiIngredient#drawTooltip}, which reads
 * {@code GameSettings.advancedItemTooltips} directly (JEI never calls {@code GuiScreen#getItemToolTip}),
 * so it needs its own redirect in addition to the vanilla {@code MixinGuiScreen} hook.
 * <p>
 * Only applied when HEI/JEI is present (CeleritasExtraMixinConfigPlugin gates the {@code .hei.} package).
 */
@Mixin(targets = "mezz.jei.gui.ingredients.GuiIngredient")
public class MixinGuiIngredient {

    @Redirect(
            method = "drawTooltip",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/settings/GameSettings;advancedItemTooltips:Z",
                    opcode = Opcodes.GETFIELD,
                    remap = true),
            remap = false
    )
    private boolean celeritasExtra$forceAdvancedTooltips(GameSettings settings) {
        return settings.advancedItemTooltips
                || CeleritasExtraClientMod.options().extraSettings.advancedItemTooltips;
    }
}
