package com.sumirelabs.celeritasextra.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VisibilityRulesTest {

    @Test
    void horizontalFacesRespectFrontBackAndNearPlane() {
        double[][] normals = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (double[] normal : normals) {
            assertFalse(
                VisibilityRules.behindHorizontalFace(normal[0] * 5, normal[1] * 5, 0, 0, normal[0], normal[1], 0.5));
            assertTrue(
                VisibilityRules.behindHorizontalFace(-normal[0] * 5, -normal[1] * 5, 0, 0, normal[0], normal[1], 0.5));
            assertFalse(VisibilityRules.behindHorizontalFace(-normal[0] * 0.4, -normal[1] * 0.4, 0, 0, normal[0],
                normal[1], 0.5));
        }
    }

    @Test
    void distancesKeepBoundaryAndZeroDisablesTheLimit() {
        assertFalse(VisibilityRules.beyondDistance(1_000_000, 0));
        assertFalse(VisibilityRules.beyondDistance(32 * 32, 32));
        assertTrue(VisibilityRules.beyondDistance(32 * 32 + 0.01, 32));
    }

    @Test
    void exclusionsAllowExactClassesAndPackagePrefixes() {
        String[] exclusions = {"example.Beacon", "mod.visual.*"};
        assertTrue(VisibilityRules.exempt("example.Beacon", exclusions));
        assertTrue(VisibilityRules.exempt("mod.visual.effects.Beam", exclusions));
        assertFalse(VisibilityRules.exempt("mod.visualizer.Beam", exclusions));
    }

    @Test
    void cachedClassesFollowReplacedRulesAndInvalidatedInPlaceEdits() {
        var cache = new VisibilityRules.ExemptionCache();
        String[] rules = {"java.lang.*"};
        assertTrue(cache.matches(String.class, rules));
        assertFalse(cache.matches(getClass(), rules));
        rules = new String[]{getClass().getName()};
        assertFalse(cache.matches(String.class, rules));
        assertTrue(cache.matches(getClass(), rules));
        rules[0] = "java.lang.String";
        cache.invalidate();
        assertTrue(cache.matches(String.class, rules));
        assertFalse(cache.matches(getClass(), rules));
    }

}
