/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.embeddedt.embeddium.impl.gui.frame.OptionPageFrame;
import org.embeddedt.embeddium.impl.gui.frame.ScrollableFrame;
import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.taumc.celeritas.api.options.control.ControlElement;
import org.taumc.celeritas.api.options.structure.Option;
import org.taumc.celeritas.api.options.structure.OptionPage;

import com.sumirelabs.celeritasextra.client.gui.rso.frame.OptionTooltipController;
import com.sumirelabs.celeritasextra.client.gui.rso.frame.PageLayout;
import com.sumirelabs.celeritasextra.client.gui.rso.frame.TooltipFrame;

/** Reuses native layout and tooltip behavior, including a flat ranked search view. */
public final class OptionActionFrame extends OptionPageFrame implements TooltipFrame {

    private final OptionTooltipController tooltip = new OptionTooltipController();
    private final boolean deferredTooltip;

    public record Ranked(List<Option<?>> options, Option<?> selected) implements Predicate<Option<?>> {

        public Ranked(List<Option<?>> options) {
            this(options, null);
        }

        public Ranked {
            options = List.copyOf(options);
        }
        @Override
        public boolean test(Option<?> option) {
            return options.contains(option);
        }

    }

    private OptionActionFrame(Dim2i dim, OptionPage page, Predicate<Option<?>> filter) {
        this(dim, page, filter, false);
    }
    private OptionActionFrame(Dim2i dim, OptionPage page, Predicate<Option<?>> filter, boolean deferredTooltip) {
        super(dim, false, page, filter);
        this.deferredTooltip = deferredTooltip;
    }

    public static ScrollableFrame create(Dim2i dim, OptionPage page, Predicate<Option<?>> filter,
        AtomicReference<Integer> offset) {
        return ScrollableFrame.createBuilder().setDimension(dim)
            .setFrame(new OptionActionFrame(dim, page, filter)).setVerticalScrollBarOffset(offset).build();
    }

    public static com.sumirelabs.celeritasextra.client.gui.rso.frame.ScrollableFrame createRso(Dim2i dim,
        OptionPage page, Predicate<Option<?>> filter, AtomicReference<Integer> offset) {
        var layout = layout(page, filter);
        return new com.sumirelabs.celeritasextra.client.gui.rso.frame.ScrollableFrame(dim, layout.contentHeight(),
            offset,
            content -> new OptionActionFrame(content, page, filter, true));
    }

    private static PageLayout layout(OptionPage page, Predicate<Option<?>> filter) {
        return PageLayout.create(page, filter, filter instanceof Ranked ranked ? ranked.options() : null);
    }

    @Override
    public void setupFrame() {
        dim = dim.withHeight(layout(page, optionFilter).contentHeight());
    }

    @Override
    public void buildFrame() {
        children.clear();
        for (var row : layout(page, optionFilter).rows()) {
            var element = new OptionActionElement<>(row.option(),
                new Dim2i(dim.x(), dim.y() + row.y(), dim.width(), 18));
            element.setSearchSelected(optionFilter instanceof Ranked ranked && ranked.selected() == row.option());
            children.add(element);
        }
        drawable.clear();
        controlElements.clear();
        for (var child : children) {
            drawable.add(child);
            if (child instanceof ControlElement<?> element)
                controlElements.add(element);
        }
    }

    @Override
    public void render(DrawContext context, int x, int y, float delta) {
        for (var child : children)
            child.render(context, x, y, delta);
        tooltip.update(controlElements.stream().filter(row -> row.isMouseOver(x, y)).findFirst().orElse(null));
        if (!deferredTooltip)
            tooltip.render(context, page, dim, 0);
    }

    @Override
    public void renderTooltip(DrawContext context, Dim2i viewport,
        int offset) {
        tooltip.render(context, page, viewport, offset);
    }

}
