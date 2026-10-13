package com.sumirelabs.celeritasextra.compat.window;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SdlTextInputTest {
    @Test void textHooksAreSafeOnTheLegacyRuntimeWithoutSdl() {
        org.junit.jupiter.api.Assumptions.assumeFalse(WindowCompat.isSdl());
        assertFalse(WindowCompat.composingText());
        assertEquals(0, WindowCompat.textScrollOffset(null));
        assertDoesNotThrow(() -> WindowCompat.drawTextInput(null, 0, 0));
    }

    @Test void forwardsCustomDrawnInputAndReadsNativeScrollAndComposition() throws Exception {
        var input = new SdlTextInput(FakeHooks.class, FakeField.class);
        var field = new FakeField();
        field.scroll = 12;
        FakeHooks.composing = true;
        input.draw(field, 30, 42);
        assertSame(field, FakeHooks.field);
        assertEquals(30, FakeHooks.x);
        assertEquals(42, FakeHooks.y);
        assertEquals(12, input.scroll(field));
        assertTrue(input.composing());
        FakeHooks.composing = false;
        assertFalse(input.composing());
    }

    public static final class FakeField {
        int scroll;
        public int getLineScrollOffset() { return scroll; }
    }
    public static final class FakeHooks {
        static FakeField field;
        static int x, y;
        static boolean composing;
        public static void textFieldDraw(FakeField value, int textX, int textY) { field = value; x = textX; y = textY; }
        public static boolean composing() { return composing; }
    }
}
