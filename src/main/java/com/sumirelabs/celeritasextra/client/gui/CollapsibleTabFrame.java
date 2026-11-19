/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.sumirelabs.celeritasextra.client.gui;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.embeddedt.embeddium.impl.gui.frame.AbstractFrame;
import org.embeddedt.embeddium.impl.gui.frame.tab.Tab;
import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.taumc.celeritas.api.options.OptionIdentifier;

import com.sumirelabs.celeritasextra.client.gui.rso.frame.ScrollableFrame;
import com.sumirelabs.celeritasextra.client.gui.rso.layout.ScreenLayout;
import com.sumirelabs.celeritasextra.client.gui.rso.widget.TabButtonWidget;
import com.sumirelabs.celeritasextra.client.gui.rso.widget.TabHeaderWidget;
/** Celeritas option/action tabs hosted by the ported RSO rail and viewport widgets. */
public final class CollapsibleTabFrame extends AbstractFrame {

    private final Map<String, List<Tab<?>>> tabs;
    private final OptionsCollapseState collapse;
    private final boolean searching;
    private final AtomicReference<TextComponent> selection;
    private final AtomicReference<Integer> sidebarOffset, pageOffset;
    private final Runnable onSelect, rebuild;
    private final Dim2i sidebar, content;
    private Tab<?> selected;
    private AbstractFrame selectedFrame;
    public CollapsibleTabFrame(Dim2i dim, DrawContext font, Map<String, List<Tab<?>>> tabs,
        OptionsCollapseState collapse,
        boolean searching, AtomicReference<TextComponent> selection, AtomicReference<Integer> sidebarOffset,
        Runnable onSelect, Runnable rebuild) {
        this(dim, font, tabs, collapse, searching, selection, sidebarOffset, onSelect, rebuild,
            new AtomicReference<>(0));
    }
    public CollapsibleTabFrame(Dim2i dim, DrawContext font, Map<String, List<Tab<?>>> tabs,
        OptionsCollapseState collapse,
        boolean searching, AtomicReference<TextComponent> selection, AtomicReference<Integer> sidebarOffset,
        Runnable onSelect, Runnable rebuild, AtomicReference<Integer> pageOffset) {
        super(dim, false);
        this.tabs = tabs;
        this.collapse = collapse;
        this.searching = searching;
        this.selection = selection;
        this.sidebarOffset = sidebarOffset;
        this.pageOffset = pageOffset;
        this.onSelect = onSelect;
        this.rebuild = rebuild;
        int width = 0;
        for (var group : tabs.entrySet()) {
            width = Math.max(width, font.getStringWidth(font.getFriendlyModName(group.getKey())) + 22);
            for (var tab : group.getValue())
                width = Math.max(width, font.getStringWidth(tab.title()));
        }
        int railWidth = ScreenLayout.sidebarWidth(width, dim.width(), railHeight() > dim.height());
        sidebar = new Dim2i(dim.x(), dim.y(), railWidth, dim.height());
        content = new Dim2i(sidebar.getLimitX(), dim.y(), Math.max(1, dim.width() - railWidth), dim.height());
        var selectable = tabs.values().stream().flatMap(List::stream).filter(t -> t.frameFunction() != null).toList();
        selected = selectable.stream().filter(t -> key(t).equals(collapse.selectedTabId())).findFirst().orElse(null);
        if (selected == null)
            selected = selectable.stream().filter(t -> t.title().equals(selection.get())).findFirst()
                .orElse(selectable.isEmpty() ? null : selectable.getFirst());
        buildFrame();
        int saved = pageOffset.get();
        try {
            for (var group : tabs.values())
                for (var tab : group)
                    if (tab != selected)
                        tab.createFrame(content);
        } finally {
            pageOffset.set(saved);
        }
    }
    private static String key(Tab<?> tab) {
        return tab.id().getModId() + ":" + tab.id().getPath();
    }
    private int railHeight() {
        int y = 0;
        for (var group : tabs.entrySet()) {
            if (y > 0)
                y += 4;
            y += 22;
            if (group.getValue().size() > 1 && !collapse.modCollapsed(group.getKey(), searching))
                y += 18 * group.getValue().size();
        }
        return y;
    }
    public boolean selectTab(OptionIdentifier<?> id) {
        var found = tabs.values().stream().flatMap(List::stream).filter(tab -> tab.id().equals(id)).findFirst()
            .orElse(null);
        if (found == null)
            return false;
        if (found.onSelectFunction() != null && !found.onSelectFunction().get())
            return true;
        if (found.frameFunction() == null)
            return true;
        selected = found;
        selection.set(found.title());
        collapse.selectTab(key(found));
        onSelect.run();
        buildFrame();
        return true;
    }
    @Override
    public void buildFrame() {
        children.clear();
        drawable.clear();
        controlElements.clear();
        int height = railHeight();
        children.add(new ScrollableFrame(sidebar, height, sidebarOffset, railDim -> new AbstractFrame(railDim, false) {

            {
                buildFrame();
            }
            @Override
            public void buildFrame() {
                children.clear();
                drawable.clear();
                controlElements.clear();
                int y = 0;
                for (var entry : tabs.entrySet()) {
                    if (y > 0)
                        y += 4;
                    String mod = entry.getKey();
                    var list = entry.getValue();
                    boolean single = list.size() == 1, collapsed = collapse.modCollapsed(mod, searching);
                    boolean active = selected != null && list.contains(selected) && (single || collapsed);
                    var headerDim = new Dim2i(dim.x(), dim.y() + y, dim.width(), 22);
                    children.add(new TabHeaderWidget(headerDim, mod, active, collapsed, searching, () -> {
                        if (single || searching)
                            selectTab(list.getFirst().id());
                        else {
                            collapse.toggleMod(mod);
                            rebuild.run();
                        }
                    }));
                    y += 22;
                    if (single || collapsed)
                        continue;
                    for (var tab : list) {
                        children.add(new TabButtonWidget(new Dim2i(dim.x(), dim.y() + y, dim.width(), 18),
                            tab.title(), selected == tab, () -> selectTab(tab.id())));
                        y += 18;
                    }
                }
                super.buildFrame();
            }

        }));
        selectedFrame = selected == null ? null : selected.createFrame(content);
        if (selectedFrame != null)
            children.add(selectedFrame);
        super.buildFrame();
    }
    @Override
    public void render(DrawContext context, int x, int y, float delta) {
        for (var child : children)
            if (child != selectedFrame)
                child.render(context, x, y, delta);
        if (selectedFrame != null)
            selectedFrame.render(context, x, y, delta);
    }

}
