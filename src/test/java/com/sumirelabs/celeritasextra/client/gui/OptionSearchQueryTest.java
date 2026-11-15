/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OptionSearchQueryTest {

    @Test
    void ranksWithUpstreamIdfAndLimitsResultsToFifteen() {
        var query = new OptionSearchQuery("cloud");
        var results = query.rank(List.of("Cloud", "Cloud Height", "Sky Cloud controls", "Cluod"), Function.identity());
        assertEquals("Cloud", results.getFirst().item());
        assertTrue(results.getFirst().score() > results.getLast().score());
        assertFalse(new OptionSearchQuery("cluod").matches("Cloud height"),
            "RSO's token coverage rejects this transposition");
        assertTrue(new OptionSearchQuery("clod").matches("Cloud height"));
        assertTrue(new OptionSearchQuery("rendring").matches("Rendering distance"));
        assertFalse(new OptionSearchQuery("fg").matches("Fog"));
        assertFalse(new OptionSearchQuery("月光").matches("日光"));
        assertFalse(new OptionSearchQuery("cloud missing").matches("Cloud height"));
        assertEquals(15, query.rank(java.util.stream.IntStream.range(0, 30)
            .mapToObj(i -> "Cloud " + i).toList(), Function.identity()).size());
        assertTrue(new OptionSearchQuery("eclairage").matches("Éclairage"));
    }

    @Test
    void blankQueriesRestoreAllOptions() {
        assertTrue(new OptionSearchQuery("　 \t").isEmpty());
        assertTrue(new OptionSearchQuery("").matches("anything"));
    }
    @Test
    void matchesJapaneseFullWidthLatinAndCaseInsensitively() {
        assertTrue(new OptionSearchQuery("ｆｐｓ　上限").matches("FPS 上限\n非アクティブ時の設定"));
        assertTrue(new OptionSearchQuery("fog").matches("FOG distance"));
        assertTrue(new OptionSearchQuery("描画距離").matches("エンティティの描画距離"));
    }
    @Test
    void allTermsMustMatchAcrossNameAndTooltip() {
        var query = new OptionSearchQuery("map behind");
        assertTrue(query.matches("Cull Map Back Faces\nSkips maps viewed from behind"));
        assertFalse(query.matches("Map rendering distance"));
        assertFalse(query.matches("A sign viewed from behind"));
    }
    @Test
    void formattingCodesDoNotBreakMatching() {
        assertTrue(new OptionSearchQuery("fps").matches("§aF§bP§cS"));
        assertFalse(new OptionSearchQuery("not found").matches(null));
    }

}
