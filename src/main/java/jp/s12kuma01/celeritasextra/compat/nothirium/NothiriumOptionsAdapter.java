package jp.s12kuma01.celeritasextra.compat.nothirium;

import jp.s12kuma01.celeritasextra.client.gui.Translations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Adds an entry to the vanilla video screen without linking either Sodium-derived GUI. */
public final class NothiriumOptionsAdapter {
    private NothiriumOptionsAdapter() { }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new NothiriumOptionsAdapter());
    }

    @SubscribeEvent
    public void addButton(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.getGui() instanceof GuiVideoSettings)) return;
        var buttons = event.getButtonList();
        // Use a distinct button type so other mods' numeric IDs cannot trigger our action.
        if (buttons.stream().anyMatch(ExtraButton.class::isInstance)) return;
        int width = event.getGui().width;
        int height = event.getGui().height;
        int x = width / 2 - 100;
        int y = height - 27;
        // Prefer the bottom margin; move upwards if another mod already occupies it.
        while (y >= 5 && overlaps(buttons, x, y, 200, 20)) y -= 24;
        if (y < 5) {
            // The standard Done row can be split even on small GUI scales.
            var done = buttons.stream().filter(b -> b.id == 200).findFirst().orElse(null);
            if (done == null) return;
            x = done.x + done.width / 2 + 2;
            y = done.y;
            done.width = Math.max(1, done.width / 2 - 2);
            buttons.add(new ExtraButton(x, y, done.width));
        } else {
            buttons.add(new ExtraButton(x, y, 200));
        }
    }

    private static boolean overlaps(java.util.List<GuiButton> buttons, int x, int y, int width, int height) {
        return buttons.stream().anyMatch(b -> b.visible && x < b.x + b.width && x + width > b.x
                && y < b.y + b.height && y + height > b.y);
    }

    @SubscribeEvent
    public void openSettings(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (!(event.getGui() instanceof GuiVideoSettings) || !(event.getButton() instanceof ExtraButton)) return;
        event.setCanceled(true);
        Minecraft.getMinecraft().gameSettings.saveOptions();
        Minecraft.getMinecraft().displayGuiScreen(new VanillaExtraOptionsScreen(event.getGui()));
    }

    private static final class ExtraButton extends GuiButton {
        private ExtraButton(int x, int y, int width) {
            super(-1, x, y, width, 20, Translations.format("celeritasextra.gui.extra_settings"));
        }
    }
}
