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
import org.taumc.celeritas.api.options.control.ControlElement;
import org.taumc.celeritas.api.options.structure.Option;
import org.taumc.celeritas.api.options.structure.OptionPage;

/** Reuses native layout and tooltip behavior, including a flat ranked search view. */
public final class OptionActionFrame extends OptionPageFrame {

    public record Ranked(List<Option<?>> options) implements Predicate<Option<?>> {

        public Ranked {
            options = List.copyOf(options);
        }
        @Override
        public boolean test(Option<?> option) {
            return options.contains(option);
        }

    }

    private OptionActionFrame(Dim2i dim, OptionPage page, Predicate<Option<?>> filter) {
        super(dim, false, page, filter);
    }

    public static ScrollableFrame create(Dim2i dim, OptionPage page, Predicate<Option<?>> filter,
        AtomicReference<Integer> offset) {
        return ScrollableFrame.createBuilder().setDimension(dim)
            .setFrame(new OptionActionFrame(dim, page, filter)).setVerticalScrollBarOffset(offset).build();
    }

    @Override
    public void setupFrame() {
        if (optionFilter instanceof Ranked ranked)
            dim = dim.withHeight(ranked.options().size() * 18);
        else
            super.setupFrame();
    }

    @Override
    public void buildFrame() {
        if (optionFilter instanceof Ranked ranked) {
            children.clear();
            int y = 0;
            for (var option : ranked.options()) {
                children.add(new OptionActionElement<>(option, new Dim2i(dim.x(), dim.y() + y, dim.width(), 18)));
                y += 18;
            }
        } else {
            children.clear();
            int y = 0;
            for (var group : page.getGroups()) {
                boolean added = false;
                for (var option : group.getOptions()) {
                    if (!optionFilter.test(option))
                        continue;
                    children.add(new OptionActionElement<>(option, new Dim2i(dim.x(), dim.y() + y, dim.width(), 18)));
                    y += 18;
                    added = true;
                }
                if (added)
                    y += 4;
            }
        }
        drawable.clear();
        controlElements.clear();
        for (var child : children) {
            drawable.add(child);
            if (child instanceof ControlElement<?> element)
                controlElements.add(element);
        }
    }

}
