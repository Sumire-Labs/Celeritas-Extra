package jp.s12kuma01.celeritasextra.client.gui;

import org.embeddedt.embeddium.impl.gui.frame.AbstractFrame;
import org.embeddedt.embeddium.impl.gui.frame.ScrollableFrame;
import org.embeddedt.embeddium.impl.gui.frame.tab.Tab;
import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.embeddedt.embeddium.impl.gui.widgets.FlatButtonWidget;
import org.embeddedt.embeddium.impl.util.Dim2i;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/** Celeritas's native sidebar widgets with per-mod expand/collapse controls. */
public final class CollapsibleTabFrame extends AbstractFrame {
    private final Map<String, List<Tab<?>>> tabs;
    private final OptionsCollapseState collapse;
    private final boolean searching;
    private final AtomicReference<TextComponent> selection;
    private final AtomicReference<Integer> sidebarOffset;
    private final Runnable onSelect;
    private final Runnable rebuild;
    private final Dim2i sidebar;
    private final Dim2i content;
    private Tab<?> selected;
    private AbstractFrame selectedFrame;

    public CollapsibleTabFrame(Dim2i dim, DrawContext font, Map<String, List<Tab<?>>> tabs,
                               OptionsCollapseState collapse, boolean searching,
                               AtomicReference<TextComponent> selection, AtomicReference<Integer> sidebarOffset,
                               Runnable onSelect, Runnable rebuild) {
        super(dim, false);
        this.tabs = tabs;
        this.collapse = collapse;
        this.searching = searching;
        this.selection = selection;
        this.sidebarOffset = sidebarOffset;
        this.onSelect = onSelect;
        this.rebuild = rebuild;
        int width = 0;
        for (var entry : tabs.entrySet()) {
            width = Math.max(width, font.getStringWidth(font.getFriendlyModName(entry.getKey())) + 48);
            for (var tab : entry.getValue()) width = Math.max(width, font.getStringWidth(tab.title()) + 29);
        }
        width = Math.min(Math.max(60, width), Math.max(60, dim.width() / 2));
        sidebar = new Dim2i(dim.x(), dim.y(), width, dim.height());
        content = new Dim2i(sidebar.getLimitX(), dim.y(), Math.max(1, dim.width() - width), dim.height());
        selected = tabs.values().stream().flatMap(List::stream)
                .filter(tab -> tab.title().equals(selection.get())).findFirst()
                .orElseGet(() -> tabs.values().stream().flatMap(List::stream).findFirst().orElse(null));
        buildFrame();
        // Preserve Celeritas's initial control-element construction for inactive tabs.
        tabs.values().stream().flatMap(List::stream).filter(tab -> tab != selected)
                .forEach(tab -> tab.createFrame(content));
    }

    @Override public void buildFrame() {
        children.clear(); drawable.clear(); controlElements.clear();
        int rows = tabs.entrySet().stream().mapToInt(e -> 1 + (collapse.modCollapsed(e.getKey(), searching) ? 0 : e.getValue().size())).sum();
        var rail = new AbstractFrame(sidebar.withHeight(Math.max(sidebar.height(), rows * 18)), false) {
            @Override public void buildFrame() {
                children.clear(); drawable.clear(); controlElements.clear();
                int y = 0;
                for (var entry : tabs.entrySet()) {
                    String mod = entry.getKey();
                    boolean collapsed = collapse.modCollapsed(mod, searching);
                    var header = new CollapsibleModHeaderWidget(
                            new Dim2i(0, y, this.dim.width() - 4, 18).withParentOffset(this.dim), mod, collapsed, searching,
                            () -> { collapse.toggleMod(mod); rebuild.run(); });
                    children.add(header);
                    y += 18;
                    if (collapsed) continue;
                    for (var tab : entry.getValue()) {
                        var button = new FlatButtonWidget(new Dim2i(0, y, this.dim.width() - 4, 18).withParentOffset(this.dim), tab.title(), () -> {
                            if (tab.onSelectFunction() == null || tab.onSelectFunction().get()) {
                                selected = tab;
                                selection.set(tab.title());
                                onSelect.run();
                                CollapsibleTabFrame.this.buildFrame();
                            }
                        }) {
                            @Override protected int getLeftAlignedTextOffset(DrawContext context) {
                                return 5 + super.getLeftAlignedTextOffset(context);
                            }
                            @Override public void render(DrawContext context, int x, int y, float delta) {
                                setLabel(TextComponent.literal(context.substrByWidth(context.extractString(tab.title()),
                                        Math.max(0, this.dim.width() - 15))));
                                super.render(context, x, y, delta);
                            }
                        };
                        button.setLeftAligned(true);
                        button.setSelected(selected == tab);
                        children.add(button);
                        y += 18;
                    }
                }
                super.buildFrame();
            }
        };
        children.add(ScrollableFrame.createBuilder().setDimension(sidebar).setFrame(rail)
                .setVerticalScrollBarOffset(sidebarOffset).build());
        selectedFrame = selected == null ? null : selected.createFrame(content);
        if (selectedFrame != null) children.add(selectedFrame);
        super.buildFrame();
    }

    @Override public void render(DrawContext context, int x, int y, float delta) {
        // Keep native option tooltips above the sidebar and its scissor region.
        for (var child : children) if (child != selectedFrame) child.render(context, x, y, delta);
        if (selectedFrame != null) selectedFrame.render(context, x, y, delta);
    }
}
