package jp.s12kuma01.celeritasextra.client;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Appends the source mod's display name to item tooltips — the "Mod Name Tooltip" behaviour,
 * modelled on mezz's ModNameTooltip. Shown in place of the raw registry id: the friendly mod
 * name (e.g. {@code Minecraft}, {@code Just Enough Items}) is resolved from the item's owning mod.
 * <p>
 * Runs off Forge's {@link ItemTooltipEvent}, which fires inside {@code ItemStack#getTooltip} and so
 * also reaches HEI/JEI ingredient tooltips (they gather lines through the same call) — no JEI-specific
 * hook is needed. At {@link EventPriority#LOW} so the mod name lands at the very bottom, after other
 * mods have added their own lines.
 */
@Mod.EventBusSubscriber(Side.CLIENT)
@SideOnly(Side.CLIENT)
public final class ModNameTooltipHandler {

    /** Blue + italic — the conventional Mod Name Tooltip styling. */
    private static final String MOD_NAME_FORMAT = TextFormatting.BLUE.toString() + TextFormatting.ITALIC;

    private ModNameTooltipHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!CeleritasExtraClientMod.options().extraSettings.modNameTooltip) {
            return;
        }
        String modName = getModName(event.getItemStack());
        if (modName == null || modName.isEmpty()) {
            return;
        }
        List<String> toolTip = event.getToolTip();
        if (!isModNameAlreadyPresent(toolTip, modName)) {
            toolTip.add(MOD_NAME_FORMAT + modName);
        }
    }

    /** Resolve the owning mod's display name, or {@code null} when it can't be attributed. */
    @Nullable
    private static String getModName(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return null;
        }
        Item item = itemStack.getItem();
        // getCreatorModId honours items that reassign their creator (e.g. tools spawned by another
        // mod); it falls back to the registry-name namespace otherwise.
        String modId = item.getCreatorModId(itemStack);
        if (modId == null) {
            return null;
        }
        Map<String, ModContainer> indexedModList = Loader.instance().getIndexedModList();
        ModContainer modContainer = indexedModList.get(modId);
        return modContainer != null ? modContainer.getName() : null;
    }

    /**
     * True when the last tooltip line already is the mod name, so we don't duplicate it. HEI/JEI
     * reaches item tooltips through the same {@link ItemTooltipEvent} and appends the mod name in
     * its own overlay; this guard is what keeps the two additions from stacking.
     */
    private static boolean isModNameAlreadyPresent(List<String> toolTip, String modName) {
        if (toolTip.size() > 1) {
            String last = TextFormatting.getTextWithoutFormattingCodes(toolTip.get(toolTip.size() - 1));
            return modName.equals(last);
        }
        return false;
    }
}
