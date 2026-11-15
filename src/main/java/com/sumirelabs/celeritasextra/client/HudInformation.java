/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client;

import com.sumirelabs.celeritasextra.compat.ShaderPassCompat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

/** Tracks time spent in the current world session and unique entity render submissions. */
@Mod.EventBusSubscriber(Side.CLIENT)
public final class HudInformation {

    private static final SessionClock CLOCK = new SessionClock();
    private static final IntOpenHashSet RENDERED = new IntOpenHashSet();
    private static boolean recording;

    private HudInformation() {}

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getMinecraft();
            CLOCK.update(System.nanoTime(), mc.world != null, mc.isGamePaused());
        }
    }

    @SubscribeEvent
    public static void beginFrame(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            recording = CeleritasExtraClientMod.options().extraSettings.showRenderedEntities
                && BackgroundFrameLimiter.shouldRender() && mc.world != null
                && !mc.gameSettings.hideGUI && !mc.gameSettings.showDebugInfo;
            RENDERED.clear();
        }
    }

    public static void recordRendered(Entity entity) {
        if (recording && !ShaderPassCompat.isShadowPass())
            RENDERED.add(entity.getEntityId());
    }

    public static int renderedEntities() {
        return RENDERED.size();
    }

    public static long sessionSeconds() {
        return CLOCK.seconds();
    }

    public static final class SessionClock {

        private long elapsed, last;
        private boolean active, paused;

        public void update(long now, boolean inWorld, boolean pause) {
            if (!inWorld) {
                elapsed = 0;
                active = false;
                return;
            }
            if (active && !paused)
                elapsed += Math.max(0, now - last);
            last = now;
            active = true;
            paused = pause;
        }

        public long seconds() {
            return elapsed / 1_000_000_000L;
        }

    }

}
