package jp.s12kuma01.celeritasextra.client.gui;

import java.util.ArrayList;
import java.util.List;

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

/**
 * Renders HUD overlays for FPS and coordinates display.
 */
@Mod.EventBusSubscriber(Side.CLIENT)
@SideOnly(Side.CLIENT)
public class CeleritasExtraHud {

    private static final Minecraft mc = Minecraft.getMinecraft();

    /**
     * Renders custom overlay text (FPS, coordinates) if debug screen is not shown.
     */
    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        // Don't render if F3 debug screen is active
        if (mc.gameSettings.showDebugInfo) {
            return;
        }

        List<String> lines = new ArrayList<>();

        if (CeleritasExtraClientMod.options().extraSettings.showFps) {
            int fps = Minecraft.getDebugFPS();
            lines.add(String.format("%d FPS", fps));
        }

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

        int y = 2;

        for (String line : lines) {
            int x = 2;

            fontRenderer.drawStringWithShadow(line, x, y, 0xFFFFFF);

            y += fontRenderer.FONT_HEIGHT + 2;
        }
    }
}
