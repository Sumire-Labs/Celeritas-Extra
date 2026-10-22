package com.sumirelabs.celeritasextra.client.gui;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.*;

class HudTextCacheTest {
    private final HudTextCache cache = new HudTextCache((key, args) -> key + Arrays.toString(args));

    @Test void movingCoordinatesAndSignedZeroUpdateImmediately() {
        cache.beginFrame(0, "en_us", Locale.US);
        String original = cache.coordinates(1.25, 64, 0.0);
        assertSame(original, cache.coordinates(1.25, 64, 0.0));
        assertNotEquals(original, cache.coordinates(1.26, 64, 0.0));
        assertTrue(cache.coordinates(1.26, 64, -0.0).endsWith("-0.00]"));
    }

    @Test void extendedFpsToggleAndNewValuesAreVisibleImmediately() {
        cache.beginFrame(0, "en_us", Locale.US);
        String simple = cache.fps(120, 100, 60, 30, false);
        assertSame(simple, cache.fps(120, 90, 50, 20, false));
        String extended = cache.fps(120, 90, 50, 20, true);
        assertTrue(extended.contains("[90, 50, 20]"));
        assertNotEquals(extended, cache.fps(120, 90, 49, 20, true));
    }

    @Test void languageAndLocaleChangesInvalidatePreviouslyHiddenLines() {
        cache.beginFrame(0, "en_us", Locale.US);
        String old = cache.coordinates(1.25, 64, 0);
        String warning = cache.lightWarning();
        cache.beginFrame(1, "ja_jp", Locale.US);
        assertNotSame(warning, cache.lightWarning());
        cache.beginFrame(2, "ja_jp", Locale.GERMANY);
        assertNotEquals(old, cache.coordinates(1.25, 64, 0));
        assertTrue(cache.coordinates(1.25, 64, 0).contains("1,25"));
    }

    @Test void refreshBoundaryWorksWithNegativeClockOrigin() {
        cache.beginFrame(-1_000_000_000L, "en_us", Locale.US);
        String old = cache.lightWarning();
        cache.beginFrame(-500_000_001L, "en_us", Locale.US);
        assertSame(old, cache.lightWarning());
        cache.beginFrame(-500_000_000L, "en_us", Locale.US);
        assertNotSame(old, cache.lightWarning());
    }
}
