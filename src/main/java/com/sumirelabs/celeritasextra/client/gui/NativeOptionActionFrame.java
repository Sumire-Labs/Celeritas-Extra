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
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.taumc.celeritas.api.options.structure.Option;
import org.taumc.celeritas.api.options.structure.OptionPage;

/** Retains Celeritas's group layout and tooltips while adding per-option actions. */
public final class NativeOptionActionFrame extends OptionPageFrame {

    private NativeOptionActionFrame(Dim2i dim, OptionPage page, Predicate<Option<?>> filter) {
        super(dim, false, page, filter);
    }

    public static ScrollableFrame create(Dim2i dim, OptionPage page, Predicate<Option<?>> filter,
        AtomicReference<Integer> offset) {
        return ScrollableFrame.createBuilder().setDimension(dim)
            .setFrame(new NativeOptionActionFrame(dim, page, filter)).setVerticalScrollBarOffset(offset).build();
    }

    @Override
    public void setupFrame() {
        if (optionFilter instanceof OptionActionFrame.Ranked ranked)
            dim = dim.withHeight(ranked.options().size() * 18);
        else
            super.setupFrame();
    }

    @Override
    public void buildFrame() {
        if (optionFilter instanceof OptionActionFrame.Ranked ranked) {
            children.clear();
            int y = dim.y();
            for (var option : ranked.options()) {
                var element = new OptionActionElement<>(option, new Dim2i(dim.x(), y, dim.width(), 18), false);
                element.setSearchSelected(option == ranked.selected());
                children.add(element);
                y += 18;
            }
        } else {
            super.buildFrame();
            var elements = List.copyOf(controlElements);
            children.clear();
            for (var element : elements)
                children.add(new OptionActionElement<>(element.getOption(), element.getDimensions(), false));
        }
        drawable.clear();
        controlElements.clear();
        for (var child : children) {
            drawable.add(child);
            if (child instanceof org.taumc.celeritas.api.options.control.ControlElement<?> element)
                controlElements.add(element);
        }
    }

}
