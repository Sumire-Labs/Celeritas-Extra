package com.sumirelabs.celeritasextra.client.gui;

import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.gui.framework.InteractionContext;
import org.embeddedt.embeddium.impl.gui.theme.DefaultColors;
import org.embeddedt.embeddium.impl.gui.widgets.FlatButtonWidget;
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class CeleritasSearchBarTest {
    @Test void emptySearchUsesNativeTranslucentStyleAndHidesExtraControls() {
        var drawing = new RecordingContext();
        var bar = new CeleritasSearchBar(new Dim2i(0, 0, 300, 20), () -> {});
        bar.update("", 0, 0, false, false, "Search options...", "149 results");
        bar.render(drawing.context, -1, -1, 0);
        assertEquals(1, drawing.fills.size());
        assertEquals(FlatButtonWidget.Style.defaults().bgDefault, drawing.fills.getFirst()[4]);
        assertEquals(List.of("Search options..."), drawing.labels);
        assertEquals(1, drawing.scissors);
        assertEquals(1, drawing.scissorEnds);
    }

    @Test void focusedSearchUsesCeleritasAccentAndNativeClearButton() {
        var drawing = new RecordingContext();
        var clears = new AtomicInteger();
        var bar = new CeleritasSearchBar(new Dim2i(0, 0, 300, 20), clears::incrementAndGet);
        bar.update("fps", 3, 3, true, true, "Search options...", "2 results");
        bar.render(drawing.context, -1, -1, 0);
        assertTrue(drawing.labels.containsAll(List.of("fps", "2 results", "×")));
        assertTrue(drawing.fills.stream().anyMatch(fill -> fill[4] == DefaultColors.ELEMENT_ACTIVATED));
        assertTrue(bar.clickClear(new InteractionContext(){}, 290, 10, 0));
        assertEquals(1, clears.get());
        bar.update("", 0, 0, true, true, "Search options...", "149 results");
        assertFalse(bar.clickClear(new InteractionContext(){}, 290, 10, 0));
    }

    @Test void longTextKeepsCaretVisibleAndClicksUseTheScrolledCharacterOffset() {
        var drawing = new RecordingContext();
        var bar = new CeleritasSearchBar(new Dim2i(0, 0, 140, 20), () -> {});
        String text = "a".repeat(120);
        bar.update(text, 120, 120, true, true, "Search options...", "0 results");
        bar.render(drawing.context, -1, -1, 0);
        assertTrue(bar.cursorAt(drawing.context, 6) > 90);
        assertEquals(120, bar.cursorAt(drawing.context, 130));
        bar.update(text, 0, 0, true, true, "Search options...", "0 results");
        assertEquals(0, bar.cursorAt(drawing.context, 6));
    }

    @Test void selectionDrawsTheCaretAtTheActiveCursorEndpoint() {
        var drawing = new RecordingContext();
        var bar = new CeleritasSearchBar(new Dim2i(0, 0, 200, 20), () -> {});
        bar.update("abcdef", 1, 4, true, true, "Search options...", "1 result");
        bar.render(drawing.context, -1, -1, 0);

        assertTrue(drawing.fills.stream().anyMatch(fill -> fill[0] == 12 && fill[2] == 13),
                "The caret should follow cursorPosition, not the selection endpoint");
    }

    private static final class RecordingContext {
        final List<int[]> fills = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        int scissors, scissorEnds;
        final DrawContext context = (DrawContext) Proxy.newProxyInstance(DrawContext.class.getClassLoader(),
                new Class<?>[]{DrawContext.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "fill" -> fills.add(new int[]{(int)args[0], (int)args[1], (int)args[2], (int)args[3], (int)args[4]});
                        case "drawString" -> {
                            labels.add(args[0].toString());
                            if (args.length == 5) assertFalse((boolean)args[4], "Use the native unshadowed GUI text style");
                        }
                        case "getStringWidth" -> { return args[0].toString().length() * 6; }
                        case "substrByWidth" -> { String text = (String)args[0]; return text.substring(0, Math.min(text.length(), Math.max(0, (int)args[1] / 6))); }
                        case "lineHeight" -> { return 9; }
                        case "enableScissor" -> scissors++;
                        case "disableScissor" -> scissorEnds++;
                        case "blitWholeImage" -> fail("The search bar must not use a vanilla widget texture");
                    }
                    return method.getReturnType() == int.class ? 0 : null;
                });
    }
}
