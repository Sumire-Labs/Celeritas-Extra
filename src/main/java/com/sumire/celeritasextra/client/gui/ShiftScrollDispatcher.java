package com.sumire.celeritasextra.client.gui;

import org.embeddedt.embeddium.impl.gui.frame.AbstractFrame;
import org.embeddedt.embeddium.impl.gui.framework.InteractionContext;
import org.embeddedt.embeddium.impl.util.Dim2i;

/** Routes Shift-wheel to controls before scrollbars, including frames which need no scrolling. */
public final class ShiftScrollDispatcher {
    private ShiftScrollDispatcher() {}

    public static boolean dispatch(AbstractFrame frame, Dim2i viewport, int offsetX, int offsetY,
                                   InteractionContext context, double x, double y, double horizontal, double vertical) {
        if (!context.isSpecialKeyDown(InteractionContext.SpecialKey.SHIFT)) return false;
        // Celeritas intentionally leaves the viewport null when the content already fits.
        Dim2i visible = viewport == null ? frame.getDimensions() : viewport;
        if (!visible.containsCursor(x, y)) return false;
        return frame.interactableChildren().toList().stream()
                .anyMatch(child -> child.mouseScrolled(context, x + offsetX, y + offsetY, horizontal, vertical));
    }
}
