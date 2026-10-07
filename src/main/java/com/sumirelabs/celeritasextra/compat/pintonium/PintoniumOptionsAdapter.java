package com.sumirelabs.celeritasextra.compat.pintonium;

import com.sumirelabs.celeritasextra.client.gui.Translations;
import com.sumirelabs.celeritasextra.compat.nothirium.VanillaExtraOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.util.Map;
import java.util.WeakHashMap;

/** Adds Extra settings using Forge events, without linking Pintonium's private GUI API. */
public final class PintoniumOptionsAdapter {
    private final Map<GuiScreen, GuiButton> buttons = new WeakHashMap<>();
    private PintoniumOptionsAdapter() { }

    public static void register() { MinecraftForge.EVENT_BUS.register(new PintoniumOptionsAdapter()); }

    static boolean isVideoScreen(Class<?> type) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (current.getName().equals("org.taumc.celeritas.impl.gui.CeleritasVideoOptionsScreen")) return true;
        }
        return false;
    }

    @SubscribeEvent
    public void initialize(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!isVideoScreen(event.getGui().getClass())) return;
        buttons.put(event.getGui(), new GuiButton(-1, 8, 8, Math.min(150, event.getGui().width - 16), 20,
                Translations.format("celeritasextra.gui.extra_settings")));
    }

    @SubscribeEvent
    public void draw(GuiScreenEvent.DrawScreenEvent.Post event) {
        GuiButton button = buttons.get(event.getGui());
        if (button != null) button.drawButton(Minecraft.getMinecraft(), event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
    }

    @SubscribeEvent
    public void click(GuiScreenEvent.MouseInputEvent.Pre event) {
        GuiButton button = buttons.get(event.getGui());
        if (button == null || Mouse.getEventButton() != 0 || !Mouse.getEventButtonState()) return;
        var mc = Minecraft.getMinecraft();
        var gui = event.getGui();
        int x = Mouse.getEventX() * gui.width / mc.displayWidth;
        int y = gui.height - Mouse.getEventY() * gui.height / mc.displayHeight - 1;
        if (!button.mousePressed(mc, x, y)) return;
        // Pintonium draws and handles its own widgets instead of GuiScreen.buttonList.
        event.setCanceled(true);
        button.playPressSound(mc.getSoundHandler());
        mc.gameSettings.saveOptions();
        mc.displayGuiScreen(new VanillaExtraOptionsScreen(gui));
    }
}
