package jp.s12kuma01.celeritasextra.client.gui;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.ClientTickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders FPS and coordinates overlay with configurable position and text contrast
 */
@Mod.EventBusSubscriber(Side.CLIENT)
@SideOnly(Side.CLIENT)
public class CeleritasExtraHud {

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI) {
            return; // Don't render when F3 debug screen is open or GUI is hidden
        }

        List<String> lines = new ArrayList<>();
        CeleritasExtraGameOptions.ExtraSettings settings = CeleritasExtraClientMod.options().extraSettings;

        // FPS display
        if (settings.showFps) {
            int fps = Minecraft.getDebugFPS();
            String fpsText = I18n.format("celeritasextra.overlay.fps", fps);

            if (settings.showFPSExtended) {
                String extendedText = I18n.format("celeritasextra.overlay.fps_extended",
                        ClientTickHandler.getHighestFps(),
                        ClientTickHandler.getAverageFps(),
                        ClientTickHandler.getLowestFps());
                fpsText = fpsText + " " + extendedText;
            }

            lines.add(fpsText);
        }

        // Coordinates display
        if (settings.showCoords && !mc.gameSettings.reducedDebugInfo) {
            EntityPlayer player = mc.player;
            if (player != null) {
                double x = player.posX;
                double y = player.posY;
                double z = player.posZ;
                lines.add(I18n.format("celeritasextra.overlay.coordinates",
                        String.format("%.2f", x),
                        String.format("%.2f", y),
                        String.format("%.2f", z)));
            }
        }

        // Light updates disabled warning
        if (!CeleritasExtraClientMod.options().renderSettings.lightUpdates) {
            lines.add(I18n.format("celeritasextra.overlay.light_updates"));
        }

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

        for (String line : lines) {
            int textWidth = fontRenderer.getStringWidth(line);
            int x = isRight ? screenWidth - textWidth - 2 : 2;

            drawString(fontRenderer, line, x, y, textContrast);

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
    private static void drawString(FontRenderer fontRenderer, String text, int x, int y, CeleritasExtraGameOptions.TextContrast textContrast) {
        int textColor = 0xFFFFFF;

        switch (textContrast) {
            case BACKGROUND:
                // Draw semi-transparent background
                int textWidth = fontRenderer.getStringWidth(text);
                Gui.drawRect(x - 1, y - 1, x + textWidth + 1, y + fontRenderer.FONT_HEIGHT + 1, 0x90505050);
                fontRenderer.drawString(text, x, y, textColor);
                break;
            case SHADOW:
                // Draw with shadow (default Minecraft style)
                fontRenderer.drawStringWithShadow(text, x, y, textColor);
                break;
            case NONE:
            default:
                // Draw without any contrast enhancement
                fontRenderer.drawString(text, x, y, textColor);
                break;
        }
    }
}
