package jp.s12kuma01.celeritasextra.mixin.tooltip;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.gui.GuiScreen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Forces advanced item tooltips (item id + durability) on when the option is enabled,
 * without requiring the vanilla F3+H debug toggle. Inspired by Sodium Extra.
 */
@Mixin(GuiScreen.class)
public class MixinGuiScreen {

    @ModifyExpressionValue(
            method = "getItemToolTip",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/settings/GameSettings;advancedItemTooltips:Z",
                    opcode = Opcodes.GETFIELD)
    )
    private boolean celeritasExtra$forceAdvancedTooltips(boolean original) {
        return original || CeleritasExtraClientMod.options().extraSettings.advancedItemTooltips;
    }
}
