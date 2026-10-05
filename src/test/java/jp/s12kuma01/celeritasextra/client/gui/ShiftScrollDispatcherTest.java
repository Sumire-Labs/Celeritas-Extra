package jp.s12kuma01.celeritasextra.client.gui;

import org.embeddedt.embeddium.impl.gui.frame.AbstractFrame;
import org.embeddedt.embeddium.impl.gui.frame.BasicFrame;
import org.embeddedt.embeddium.impl.gui.frame.ScrollableFrame;
import org.embeddedt.embeddium.impl.gui.frame.components.ScrollBarComponent;
import org.embeddedt.embeddium.impl.gui.framework.InteractionContext;
import org.embeddedt.embeddium.impl.gui.widgets.AbstractWidget;
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class ShiftScrollDispatcherTest {
    private static final InteractionContext SHIFT = new InteractionContext() {
        public boolean isSpecialKeyDown(SpecialKey key) { return key == SpecialKey.SHIFT; }
    };

    @Test void fittingNativeFrameHasNullViewportAndStillDispatchesShiftWheel() throws Exception {
        var calls = new AtomicInteger();
        AbstractWidget control = control(new Dim2i(0, 0, 200, 20), calls);
        var content = BasicFrame.createBuilder().setDimension(new Dim2i(0, 0, 200, 20))
                .addChild(bounds -> control).build();
        var frame = ScrollableFrame.createBuilder().setDimension(new Dim2i(0, 0, 200, 40)).setFrame(content).build();
        Dim2i viewport = (Dim2i) field(frame, "viewPortDimension");
        assertNull(viewport, "Reproduce the real state from the reported crash");
        assertTrue(ShiftScrollDispatcher.dispatch(frame, viewport, 0, 0, SHIFT, 10, 10, 0, 1));
        assertEquals(1, calls.get());
        assertFalse(ShiftScrollDispatcher.dispatch(frame, viewport, 0, 0, SHIFT, 210, 10, 0, 1));
        assertEquals(1, calls.get());
    }

    @Test void nativeScrolledFrameOffsetsMouseCoordinatesAndLeavesOrdinaryScrollingIntact() throws Exception {
        var calls = new AtomicInteger();
        AbstractWidget control = control(new Dim2i(0, 120, 180, 20), calls);
        var content = BasicFrame.createBuilder().setDimension(new Dim2i(0, 0, 200, 200))
                .addChild(bounds -> control).build();
        var frame = ScrollableFrame.createBuilder().setDimension(new Dim2i(0, 0, 200, 40)).setFrame(content).build();
        Dim2i viewport = (Dim2i) field(frame, "viewPortDimension");
        var scroll = (ScrollBarComponent) field(frame, "verticalScrollBar");
        assertNotNull(viewport);
        scroll.setOffset(100);
        assertTrue(ShiftScrollDispatcher.dispatch(frame, viewport, 0, scroll.getOffset(), SHIFT, 10, 25, 0, 1));
        assertEquals(1, calls.get());
        assertEquals(100, scroll.getOffset());
        var ordinary = new InteractionContext() {};
        assertFalse(ShiftScrollDispatcher.dispatch(frame, viewport, 0, scroll.getOffset(), ordinary, 10, 25, 0, 1));
        assertTrue(frame.mouseScrolled(ordinary, 10, 25, 0, 1));
        assertTrue(scroll.getOffset() < 100);
    }

    private static AbstractWidget control(Dim2i bounds, AtomicInteger calls) {
        return new AbstractWidget() {
            public void render(org.embeddedt.embeddium.impl.gui.framework.DrawContext context, int x, int y, float delta) {}
            public boolean isMouseOver(double x, double y) { return bounds.containsCursor(x, y); }
            public boolean mouseScrolled(InteractionContext context, double x, double y, double horizontal, double vertical) {
                if (!context.isSpecialKeyDown(InteractionContext.SpecialKey.SHIFT) || !bounds.containsCursor(x, y)) return false;
                calls.incrementAndGet();
                return true;
            }
        };
    }
    private static Object field(ScrollableFrame frame, String name) throws Exception {
        Field field = ScrollableFrame.class.getDeclaredField(name); field.setAccessible(true); return field.get(frame);
    }
}
