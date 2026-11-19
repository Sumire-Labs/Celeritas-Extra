/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.sumirelabs.celeritasextra.client.gui;

import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.embeddedt.embeddium.impl.gui.widgets.FlatButtonWidget;
import org.embeddedt.embeddium.impl.util.Dim2i;

/** Presentation-only group header; folding never edits the underlying options. */
public final class OptionSectionHeader extends FlatButtonWidget {

    private final OptionSections.Section section;
    private final boolean collapsed, searching;
    public OptionSectionHeader(Dim2i bounds, OptionSections.Section section, boolean collapsed, boolean searching,
        Runnable toggle) {
        super(bounds, section.title(), toggle);
        this.section = section;
        this.collapsed = collapsed;
        this.searching = searching;
        setEnabled(section.collapsible() && !searching);
    }
    @Override
    public void render(DrawContext context, int x, int y, float delta) {
        boolean hover = section.collapsible() && !searching && dim.containsCursor(x, y);
        context.fill(dim.x(), dim.y(), dim.getLimitX(), dim.getLimitY(), hover ? 0xD0000000 : 0x70000000);
        int indent = section.parent() == null ? 6 : 14;
        String prefix = section.collapsible() ? (collapsed && !searching ? "+ " : "- ") : "";
        String text = prefix + context.extractString(section.title());
        context.drawString(TextComponent.literal(context.substrByWidth(text, Math.max(0, dim.width() - indent - 6))),
            dim.x() + indent, dim.getCenterY() - context.lineHeight() / 2, 0xFFDDDDDD, false);
    }

}
