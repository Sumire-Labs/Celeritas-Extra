package com.sumirelabs.celeritasextra.client.gui;

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
            Runtime runtime = Runtime.getRuntime();
            long used = runtime.totalMemory() - runtime.freeMemory();
            long max = runtime.maxMemory();
            memory = formatter.format("celeritasextra.overlay.memory", used * 100L / max,
                used / 1_048_576L, max / 1_048_576L);
        }
        return memory;
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
