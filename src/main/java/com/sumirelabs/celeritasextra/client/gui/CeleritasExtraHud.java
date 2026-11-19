/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod;
import com.sumirelabs.celeritasextra.client.FrameCounter;
import com.sumirelabs.celeritasextra.client.HudInformation;
import com.sumirelabs.celeritasextra.client.ServerTickRateEstimator;
import com.sumirelabs.celeritasextra.client.gui.CeleritasExtraGameOptions.HudLine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Renders configurable performance, time, world, and entity information on the in-game HUD.
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
    private static final List<String> lines = new ArrayList<>(12);
    private static final List<HudLine> lineKinds = new ArrayList<>(12);
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
        if (!settings.showFps && !settings.showCoords && !settings.showMemory && !warnLightUpdates
            && !settings.showLocalTime && !settings.showWorldDay && !settings.showSessionTime
            && !settings.showEntityCount && !settings.showRenderedEntities && !settings.showBiome && !settings.showTps)
            return;

        TEXT.beginFrame(System.nanoTime(), mc.gameSettings.language, Locale.getDefault());
        lines.clear();
        lineKinds.clear();

        if (settings.showFps) {
            add(TEXT.fps(FrameCounter.getSmoothFps(), FrameCounter.getAverageFps(),
                FrameCounter.getOnePercentLowFps(), FrameCounter.getPointOnePercentLowFps(), settings.showFPSExtended),
                HudLine.FPS);
        }
        if (settings.showCoords && (settings.ignoreReducedDebugInfo || !mc.isReducedDebug())) {
            EntityPlayer player = mc.player;
            if (player != null)
                add(TEXT.coordinates(player.posX, player.posY, player.posZ), HudLine.COORDINATES);
        }
        if (settings.showMemory) {
            add(TEXT.memoryPercent(), HudLine.MEMORY);
            if (settings.showMemoryExtended)
                add(TEXT.memoryAmount(), HudLine.MEMORY);
        }
        if (settings.showLocalTime)
            add(TEXT.localTime(settings.use12HourClock), HudLine.LOCAL_TIME);
        if (mc.world != null) {
            if (settings.showTps) {
                var connection = mc.getConnection();
                if (mc.isGamePaused() && connection != null)
                    ServerTickRateEstimator.INSTANCE.reset(connection.getNetworkManager());
                double tps = ServerTickRateEstimator.INSTANCE.estimate(
                    connection == null ? null : connection.getNetworkManager(), System.nanoTime());
                add(TEXT.tps(tps, mc.isGamePaused()), HudLine.TPS);
            }
            if (settings.showWorldDay)
                add(TEXT.worldDay(mc.world.getTotalWorldTime()), HudLine.WORLD_DAY);
            if (settings.showSessionTime)
                add(TEXT.session(HudInformation.sessionSeconds()), HudLine.SESSION_TIME);
            if (settings.showEntityCount)
                add(TEXT.entityCount(mc.world.loadedEntityList.size(), false), HudLine.ENTITY_COUNT);
            if (settings.showRenderedEntities)
                add(TEXT.entityCount(HudInformation.renderedEntities(), true), HudLine.RENDERED_ENTITIES);
            if (settings.showBiome && mc.player != null) {
                if (TEXT.needsBiome(mc.world)) {
                    var biome = mc.world.getBiome(new BlockPos(mc.player));
                    var id = biome.getRegistryName();
                    String name = id == null ? biome.getBiomeName() : id.toString();
                    if (id != null && id.getNamespace().equals("minecraft")) {
                        String key = "biome.minecraft." + id.getPath();
                        name = I18n.hasKey(key) ? I18n.format(key) : biome.getBiomeName();
                    }
                    TEXT.biome(name);
                }
                add(TEXT.biome(), HudLine.BIOME);
            }
        }
        if (warnLightUpdates)
            add(TEXT.lightWarning(), null);

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

            var kind = lineKinds.get(i);
            drawString(fontRenderer, line, x, y, textWidth, textContrast,
                kind == null ? HUD_TEXT_COLOR : settings.hudColor(kind));

            // Move to next line (up or down depending on corner)
            if (isBottom) {
                y -= lineHeight;
            } else {
                y += lineHeight;
            }
        }
    }

    private static void add(String text, HudLine kind) {
        lines.add(text);
        lineKinds.add(kind);
    }

    /**
     * Draws a string with the specified text contrast mode
     */
    private static void drawString(FontRenderer fontRenderer, String text, int x, int y, int textWidth,
        CeleritasExtraGameOptions.TextContrast textContrast, int textColor) {

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
