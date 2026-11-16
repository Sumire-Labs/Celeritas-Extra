/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.util.Dim2i;

/** Centers image icons independently of font glyph metrics. */
public final class OptionActionIcons {

    private OptionActionIcons() {}

    public static void draw(DrawContext context, Dim2i bounds, OptionActionIcon icon) {
        int size = Math.max(1, Math.min(OptionActionIcon.DRAW_SIZE, Math.min(bounds.width(), bounds.height()) - 2));
        context.blitWholeImage(icon.texture(), bounds.getCenterX() - size / 2, bounds.getCenterY() - size / 2, size,
            size);
    }

}
