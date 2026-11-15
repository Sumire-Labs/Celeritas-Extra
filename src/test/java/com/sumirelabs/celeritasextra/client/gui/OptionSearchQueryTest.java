/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OptionSearchQueryTest {

    @Test
    void ranksNamesAboveDescriptionsAndTypos() {
        var query = new OptionSearchQuery("cloud");
        assertTrue(query.score("Cloud", "") > query.score("Cloud Height", ""));
        assertTrue(query.score("Cloud Height", "") > query.score("Sky", "Cloud controls"));
        assertTrue(query.score("Sky", "Cloud controls") > query.score("Cluod", ""));
        assertTrue(new OptionSearchQuery("cluod").matches("Cloud height"));
        assertTrue(new OptionSearchQuery("rendring").matches("Rendering distance"));
        assertFalse(new OptionSearchQuery("fg").matches("Fog"));
        assertFalse(new OptionSearchQuery("月光").matches("日光"));
        assertFalse(new OptionSearchQuery("cloud missing").matches("Cloud height"));
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
