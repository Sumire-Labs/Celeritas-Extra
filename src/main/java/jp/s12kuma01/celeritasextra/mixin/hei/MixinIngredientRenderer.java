package jp.s12kuma01.celeritasextra.mixin.hei;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.settings.GameSettings;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Extends the "Advanced Item Tooltips" option to the HEI/JEI ingredient list overlay (the item
 * grid on the side).
 * <p>
 * JEI builds its own tooltips and never calls {@code GuiScreen#getItemToolTip}, so the vanilla
 * {@code MixinGuiScreen} hook does not reach it. The item-grid tooltip is assembled in
 * {@code IngredientRenderer#getIngredientTooltipSafe}, which reads
 * {@code GameSettings.advancedItemTooltips} directly to choose the tooltip flag — we redirect that
 * read so the option forces advanced tooltips (id + durability) here too.
 * <p>
 * Only applied when HEI/JEI is present (CeleritasExtraMixinConfigPlugin gates the {@code .hei.} package).
 */
@Mixin(targets = "mezz.jei.render.IngredientRenderer")
public class MixinIngredientRenderer {

    @Redirect(
            method = "getIngredientTooltipSafe",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/settings/GameSettings;advancedItemTooltips:Z",
                    opcode = Opcodes.GETFIELD,
                    remap = true),
            remap = false
    )
    private static boolean celeritasExtra$forceAdvancedTooltips(GameSettings settings) {
        return settings.advancedItemTooltips
                || CeleritasExtraClientMod.options().extraSettings.advancedItemTooltips;
    }
}
