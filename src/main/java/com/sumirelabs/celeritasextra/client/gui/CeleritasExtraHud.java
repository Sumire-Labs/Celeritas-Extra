package com.sumirelabs.celeritasextra.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod;
import com.sumirelabs.celeritasextra.client.FrameCounter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Renders the optional FPS, coordinate, and JVM heap overlay onto the in-game HUD.
 * <p>
 * Subscribed to the client Forge event bus, it draws the lines enabled in
 * {@link CeleritasExtraGameOptions.ExtraSettings} (FPS with average/1%/0.1% lows, player
 * coordinates, JVM heap usage, and a light-updates-disabled warning) in the configured screen corner and with the
 * chosen text contrast. Nothing is drawn while the F3 debug screen is showing or the GUI is hidden.
 */
@Mod.EventBusSubscriber(Side.CLIENT)
@SideOnly(Side.CLIENT)
public class CeleritasExtraHud {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final int HUD_TEXT_COLOR = 0xFFFFFF;
    private static final int HUD_BACKGROUND_COLOR = 0x90505050;
    private static final List<String> lines = new ArrayList<>(4);
    private static final HudTextCache TEXT = new HudTextCache();

    /**
     * Draws the enabled overlay lines each time the HUD text layer renders.
     * <p>
     * Collects the FPS, coordinate, memory, and warning lines permitted by the current settings, then lays
     * them out from the configured {@link CeleritasExtraGameOptions.OverlayCorner}, stacking away from
     * that corner. Returns early when the debug screen or GUI is hidden, or when no line is enabled.
     *
     * @param event the HUD text render event supplying the current screen resolution
     */
    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI) {
            return;
        }

        CeleritasExtraGameOptions.ExtraSettings settings = CeleritasExtraClientMod.options().extraSettings;
        boolean warnLightUpdates = !CeleritasExtraClientMod.options().renderSettings.lightUpdates;
        if (!settings.showFps && !settings.showCoords && !settings.showMemory && !warnLightUpdates)
            return;

        TEXT.beginFrame(System.nanoTime(), mc.gameSettings.language, Locale.getDefault());
        lines.clear();

        if (settings.showFps) {
            lines.add(TEXT.fps(FrameCounter.getSmoothFps(), FrameCounter.getAverageFps(),
                FrameCounter.getOnePercentLowFps(), FrameCounter.getPointOnePercentLowFps(), settings.showFPSExtended));
        }
        if (settings.showCoords && (settings.ignoreReducedDebugInfo || !mc.isReducedDebug())) {
            EntityPlayer player = mc.player;
            if (player != null)
                lines.add(TEXT.coordinates(player.posX, player.posY, player.posZ));
        }
        if (settings.showMemory)
            lines.add(TEXT.memory());
        if (warnLightUpdates)
            lines.add(TEXT.lightWarning());

        if (lines.isEmpty()) {
            return;
        }

        FontRenderer fontRenderer = mc.fontRenderer;
        ScaledResolution resolution = event.getResolution();
        CeleritasExtraGameOptions.OverlayCorner corner = settings.overlayCorner;
        CeleritasExtraGameOptions.TextContrast textContrast = settings.textContrast;

        int screenWidth = resolution.getScaledWidth();
        int screenHeight = resolution.getScaledHeight();
        int lineHeight = fontRenderer.FONT_HEIGHT + 2;

        // Calculate starting Y position based on corner
        boolean isBottom = corner == CeleritasExtraGameOptions.OverlayCorner.BOTTOM_LEFT ||
            corner == CeleritasExtraGameOptions.OverlayCorner.BOTTOM_RIGHT;
        boolean isRight = corner == CeleritasExtraGameOptions.OverlayCorner.TOP_RIGHT ||
            corner == CeleritasExtraGameOptions.OverlayCorner.BOTTOM_RIGHT;

        int y = isBottom ? screenHeight - fontRenderer.FONT_HEIGHT - 2 : 2;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int textWidth = fontRenderer.getStringWidth(line);
            int x = isRight ? screenWidth - textWidth - 2 : 2;

            drawString(fontRenderer, line, x, y, textWidth, textContrast);

            // Move to next line (up or down depending on corner)
            if (isBottom) {
                y -= lineHeight;
            } else {
                y += lineHeight;
            }
        }
    }

    /**
     * Draws a string with the specified text contrast mode
     */
    private static void drawString(FontRenderer fontRenderer, String text, int x, int y, int textWidth,
        CeleritasExtraGameOptions.TextContrast textContrast) {
        int textColor = HUD_TEXT_COLOR;

        switch (textContrast) {
            case BACKGROUND -> {
                Gui.drawRect(x - 1, y - 1, x + textWidth + 1, y + fontRenderer.FONT_HEIGHT + 1, HUD_BACKGROUND_COLOR);
                fontRenderer.drawString(text, x, y, textColor);
            }
            case SHADOW -> fontRenderer.drawStringWithShadow(text, x, y, textColor);
            default -> fontRenderer.drawString(text, x, y, textColor);
        }
    }

}
