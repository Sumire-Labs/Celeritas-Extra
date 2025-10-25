package jp.s12kuma01.celeritasextra.client.gui;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders FPS and coordinates overlay
 * In 1.12.2, we use RenderGameOverlayEvent.Text
 */
@Mod.EventBusSubscriber(Side.CLIENT)
@SideOnly(Side.CLIENT)
public class CeleritasExtraHud {

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (mc.gameSettings.showDebugInfo) {
            return; // Don't render when F3 debug screen is open
        }

        List<String> lines = new ArrayList<>();

        // FPS display
        if (CeleritasExtraClientMod.options().extraSettings.showFps) {
            int fps = Minecraft.getDebugFPS();
            lines.add(String.format("%d FPS", fps));
        }

        // Coordinates display
        if (CeleritasExtraClientMod.options().extraSettings.showCoords && !mc.gameSettings.reducedDebugInfo) {
            EntityPlayer player = mc.player;
            if (player != null) {
                double x = player.posX;
                double y = player.posY;
                double z = player.posZ;
                lines.add(String.format("X: %.2f, Y: %.2f, Z: %.2f", x, y, z));
            }
        }

        if (lines.isEmpty()) {
            return;
        }

        FontRenderer fontRenderer = mc.fontRenderer;
        ScaledResolution resolution = event.getResolution();

        int y = 2; // Start from top

        for (String line : lines) {
            int x = 2; // Left side

            // Draw shadow background for better visibility
            fontRenderer.drawStringWithShadow(line, x, y, 0xFFFFFF);

            y += fontRenderer.FONT_HEIGHT + 2;
        }
    }
}
