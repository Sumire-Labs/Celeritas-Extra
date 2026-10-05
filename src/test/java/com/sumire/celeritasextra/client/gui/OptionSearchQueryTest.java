package com.sumire.celeritasextra.client.gui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OptionSearchQueryTest {
    @Test void blankQueriesRestoreAllOptions() {
        assertTrue(new OptionSearchQuery("　 \t").isEmpty());
        assertTrue(new OptionSearchQuery("").matches("anything"));
    }
    @Test void matchesJapaneseFullWidthLatinAndCaseInsensitively() {
        assertTrue(new OptionSearchQuery("ｆｐｓ　上限").matches("FPS 上限\n非アクティブ時の設定"));
        assertTrue(new OptionSearchQuery("fog").matches("FOG distance"));
        assertTrue(new OptionSearchQuery("描画距離").matches("エンティティの描画距離"));
    }
    @Test void allTermsMustMatchAcrossNameAndTooltip() {
        var query = new OptionSearchQuery("map behind");
        assertTrue(query.matches("Cull Map Back Faces\nSkips maps viewed from behind"));
        assertFalse(query.matches("Map rendering distance"));
        assertFalse(query.matches("A sign viewed from behind"));
    }
    @Test void formattingCodesDoNotBreakMatching() {
        assertTrue(new OptionSearchQuery("fps").matches("§aF§bP§cS"));
        assertFalse(new OptionSearchQuery("not found").matches(null));
    }
}
