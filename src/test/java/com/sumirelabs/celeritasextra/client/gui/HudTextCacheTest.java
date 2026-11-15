/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import java.util.Arrays;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HudTextCacheTest {

    @Test
    void informationLinesUseCorrectDaySessionAndClockFormats() {
        cache.beginFrame(0, "en_us", Locale.US);
        assertTrue(cache.worldDay(23999).endsWith("[0]"));
        String day = cache.worldDay(24000);
        assertTrue(day.endsWith("[1]"));
        assertSame(day, cache.worldDay(47999));
        assertTrue(cache.session(3661).endsWith("[1, 1, 1]"));
        assertTrue(cache.entityCount(7, false).endsWith("[7]"));
        assertTrue(cache.entityCount(3, true).endsWith("[3]"));
        var midnight = java.time.LocalDateTime.of(2026, 11, 16, 0, 5);
        assertEquals("00:05", HudTextCache.formatLocalTime(midnight, false, Locale.US));
        assertEquals("12:05 AM", HudTextCache.formatLocalTime(midnight, true, Locale.US));
        assertEquals("01:05 PM", HudTextCache.formatLocalTime(midnight.plusHours(13), true, Locale.US));
    }

    @Test
    void biomeRefreshesOnWorldChangesAndAtTheRefreshBoundary() {
        var firstWorld = new Object();
        cache.beginFrame(0, "en_us", Locale.US);
        assertTrue(cache.needsBiome(firstWorld));
        cache.biome("Plains");
        assertFalse(cache.needsBiome(firstWorld));
        cache.beginFrame(499_999_999, "en_us", Locale.US);
        assertEquals("Plains", cache.biome());
        cache.beginFrame(500_000_000, "en_us", Locale.US);
        assertTrue(cache.needsBiome(firstWorld));
        cache.biome("Forest");
        assertTrue(cache.needsBiome(new Object()));
    }

    private final HudTextCache cache = new HudTextCache((key, args) -> key + Arrays.toString(args));

    @Test
    void movingCoordinatesAndSignedZeroUpdateImmediately() {
        cache.beginFrame(0, "en_us", Locale.US);
        String original = cache.coordinates(1.25, 64, 0.0);
        assertSame(original, cache.coordinates(1.25, 64, 0.0));
        assertNotEquals(original, cache.coordinates(1.26, 64, 0.0));
        assertTrue(cache.coordinates(1.26, 64, -0.0).endsWith("-0.00]"));
    }

    @Test
    void extendedFpsToggleAndNewValuesAreVisibleImmediately() {
        cache.beginFrame(0, "en_us", Locale.US);
        String simple = cache.fps(120, 100, 60, 30, false);
        assertSame(simple, cache.fps(120, 90, 50, 20, false));
        String extended = cache.fps(120, 90, 50, 20, true);
        assertTrue(extended.contains("[90, 50, 20]"));
        assertNotEquals(extended, cache.fps(120, 90, 49, 20, true));
    }

    @Test
    void languageAndLocaleChangesInvalidatePreviouslyHiddenLines() {
        cache.beginFrame(0, "en_us", Locale.US);
        String old = cache.coordinates(1.25, 64, 0);
        String warning = cache.lightWarning();
        cache.beginFrame(1, "ja_jp", Locale.US);
        assertNotSame(warning, cache.lightWarning());
        cache.beginFrame(2, "ja_jp", Locale.GERMANY);
        assertNotEquals(old, cache.coordinates(1.25, 64, 0));
        assertTrue(cache.coordinates(1.25, 64, 0).contains("1,25"));
    }

    @Test
    void refreshBoundaryWorksWithNegativeClockOrigin() {
        cache.beginFrame(-1_000_000_000L, "en_us", Locale.US);
        String old = cache.lightWarning();
        cache.beginFrame(-500_000_001L, "en_us", Locale.US);
        assertSame(old, cache.lightWarning());
        cache.beginFrame(-500_000_000L, "en_us", Locale.US);
        assertNotSame(old, cache.lightWarning());
    }

}
