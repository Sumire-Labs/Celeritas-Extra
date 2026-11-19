/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/** Render-thread text cache. Refreshes translations and memory at least twice per second. */
public final class HudTextCache {

    private static final long REFRESH_NS = 500_000_000L;
    private final Formatter formatter;
    private long nextRefresh;
    private String language;
    private Locale locale;
    private String fps, coordinates, memory, lightWarning;
    private int smooth, average, onePercent, pointOnePercent;
    private boolean extended;
    private double x, y, z;
    private String clock, worldDay, session, entities, rendered, biome, memoryPercent, memoryAmount;
    private long dayValue, sessionValue, heapUsed = -1, heapMax;
    private int entityValue, renderedValue;
    private boolean twelveHour;
    private String tps;
    private long tpsValue;
    private WeakReference<Object> biomeWorld = new WeakReference<>(null);

    public HudTextCache() {
        this(Translations::format);
    }

    HudTextCache(Formatter formatter) {
        this.formatter = formatter;
    }

    public void beginFrame(long now, String language, Locale locale) {
        if (this.locale == null || now - nextRefresh >= 0
            || !Objects.equals(this.language, language) || !locale.equals(this.locale)) {
            this.language = language;
            this.locale = locale;
            nextRefresh = now + REFRESH_NS;
            fps = coordinates = memory = lightWarning = null;
            clock = worldDay = session = entities = rendered = biome = memoryPercent = memoryAmount = null;
            tps = null;
            heapUsed = -1;
        }
    }

    public String fps(int smooth, int average, int onePercent, int pointOnePercent, boolean extended) {
        if (fps == null || this.smooth != smooth || this.extended != extended
            || extended && (this.average != average || this.onePercent != onePercent
                || this.pointOnePercent != pointOnePercent)) {
            this.smooth = smooth;
            this.average = average;
            this.onePercent = onePercent;
            this.pointOnePercent = pointOnePercent;
            this.extended = extended;
            fps = formatter.format("sodium-extra.overlay.fps", smooth);
            if (extended)
                fps += " "
                    + formatter.format("sodium-extra.overlay.fps_extended", average, onePercent, pointOnePercent);
        }
        return fps;
    }

    public String coordinates(double x, double y, double z) {
        if (coordinates == null || Double.compare(this.x, x) != 0
            || Double.compare(this.y, y) != 0 || Double.compare(this.z, z) != 0) {
            this.x = x;
            this.y = y;
            this.z = z;
            coordinates = formatter.format("sodium-extra.overlay.coordinates",
                String.format(locale, "%.2f", x), String.format(locale, "%.2f", y), String.format(locale, "%.2f", z));
        }
        return coordinates;
    }

    public String memory() {
        if (memory == null) {
            sampleMemory();
            memory = formatter.format("celeritasextra.overlay.memory", heapUsed * 100L / heapMax,
                heapUsed / 1_048_576L, heapMax / 1_048_576L);
        }
        return memory;
    }

    private void sampleMemory() {
        if (heapUsed < 0) {
            var runtime = Runtime.getRuntime();
            heapUsed = runtime.totalMemory() - runtime.freeMemory();
            heapMax = runtime.maxMemory();
        }
    }

    public String memoryPercent() {
        if (memoryPercent == null) {
            sampleMemory();
            memoryPercent = formatter.format("celeritasextra.overlay.memory_percent", heapUsed * 100L / heapMax);
        }
        return memoryPercent;
    }

    public String memoryAmount() {
        if (memoryAmount == null) {
            sampleMemory();
            memoryAmount = formatter.format("celeritasextra.overlay.memory_amount", heapUsed / 1_048_576L,
                heapMax / 1_048_576L);
        }
        return memoryAmount;
    }

    public String localTime(boolean twelveHour) {
        if (clock == null || this.twelveHour != twelveHour) {
            this.twelveHour = twelveHour;
            clock = formatLocalTime(LocalDateTime.now(), twelveHour, locale);
        }
        return clock;
    }

    static String formatLocalTime(LocalDateTime time, boolean twelveHour, Locale locale) {
        return time.format(DateTimeFormatter.ofPattern(twelveHour ? "hh:mm a" : "HH:mm", locale));
    }

    public String worldDay(long totalWorldTime) {
        long day = totalWorldTime / 24000;
        if (worldDay == null || dayValue != day) {
            dayValue = day;
            worldDay = formatter.format("celeritasextra.overlay.world_day", day);
        }
        return worldDay;
    }

    public String session(long seconds) {
        if (session == null || sessionValue != seconds) {
            sessionValue = seconds;
            session = formatter.format("celeritasextra.overlay.session_time", seconds / 3600, seconds / 60 % 60,
                seconds % 60);
        }
        return session;
    }

    public String entityCount(int count, boolean renderedCount) {
        if (renderedCount) {
            if (rendered == null || renderedValue != count) {
                renderedValue = count;
                rendered = formatter.format("celeritasextra.overlay.rendered_entities", count);
            }
            return rendered;
        }
        if (entities == null || entityValue != count) {
            entityValue = count;
            entities = formatter.format("celeritasextra.overlay.entity_count", count);
        }
        return entities;
    }

    public String tps(double estimate, boolean paused) {
        long value = paused
            ? -2
            : Double.isFinite(estimate) ? Math.round(Math.max(0, Math.min(20, estimate)) * 100) : -1;
        if (tps == null || tpsValue != value) {
            tpsValue = value;
            tps = value == -2
                ? formatter.format("celeritasextra.overlay.tps_paused")
                : formatter.format("celeritasextra.overlay.tps",
                    value == -1 ? "--" : String.format(Locale.ROOT, "%.2f", value / 100.0));
        }
        return tps;
    }

    public boolean needsBiome(Object world) {
        if (biomeWorld.get() != world) {
            biomeWorld = new WeakReference<>(world);
            biome = null;
        }
        return biome == null;
    }

    public void biome(String text) {
        biome = text;
    }
    public String biome() {
        return biome;
    }

    public String lightWarning() {
        if (lightWarning == null)
            lightWarning = formatter.format("sodium-extra.overlay.light_updates");
        return lightWarning;
    }

    @FunctionalInterface
    interface Formatter {

        String format(String key, Object... arguments);

    }

}
