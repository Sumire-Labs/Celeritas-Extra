package jp.s12kuma01.celeritasextra.client.gui;

import org.embeddedt.embeddium.impl.gui.frame.OptionPageFrame;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.gui.widgets.FlatButtonWidget;
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.taumc.celeritas.api.options.structure.Option;
import org.taumc.celeritas.api.options.structure.OptionGroup;
import org.taumc.celeritas.api.options.structure.OptionPage;

import java.util.function.Predicate;

/** Native controls and tooltips, with collapsible headers between option groups. */
public final class CollapsibleOptionPageFrame extends OptionPageFrame {
    private OptionsCollapseState collapse;
    private boolean searching;
    private Runnable rebuild;

    public CollapsibleOptionPageFrame(Dim2i dim, OptionPage page, Predicate<Option<?>> filter,
                                      OptionsCollapseState collapse, boolean searching, Runnable rebuild) {
        super(dim, false, page, filter);
        this.collapse = collapse;
        this.searching = searching;
        this.rebuild = rebuild;
        setupFrame();
        buildFrame();
    }

    private String key(OptionGroup group, int index) {
        return page.getId() + "/" + group.getId() + "/" + index;
    }

    private TextComponent label(OptionGroup group) {
        var label = group.getId().getModId().equals("celeritasextra") ? OptionGroupLabels.get(group.getId().getPath()) : null;
        return label == null ? group.getOptions().getFirst().getName() : TextComponent.literal(label);
    }

    @Override public void setupFrame() {
        if (collapse == null) { super.setupFrame(); return; }
        int y = 0;
        int index = 0;
        for (var group : page.getGroups()) {
            long count = group.getOptions().stream().filter(optionFilter).count();
            if (count > 0) y += 18 + (collapse.groupCollapsed(key(group, index), searching) ? 0 : (int) count * 18) + 4;
            index++;
        }
        dim = dim.withHeight(Math.max(0, y - 4));
    }

    @Override public void buildFrame() {
        // OptionPageFrame's constructor invokes this before subclass fields are initialized.
        if (collapse == null) { super.buildFrame(); return; }
        children.clear(); drawable.clear(); controlElements.clear();
        int y = 0;
        int index = 0;
        for (var group : page.getGroups()) {
            var options = group.getOptions().stream().filter(optionFilter).toList();
            String key = key(group, index++);
            if (options.isEmpty()) continue;
            boolean collapsed = collapse.groupCollapsed(key, searching);
            var header = new FlatButtonWidget(new Dim2i(0, y, dim.width(), 18).withParentOffset(dim), label(group),
                    () -> { collapse.toggleGroup(key); rebuild.run(); }) {
                @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                    setLabel(TextComponent.literal(context.substrByWidth(
                            (collapsed ? "+ " : "- ") + context.extractString(label(group)), Math.max(0, this.dim.width() - 10))));
                    super.render(context, mouseX, mouseY, delta);
                }
            };
            header.setLeftAligned(true);
            header.setEnabled(!searching);
            children.add(header);
            y += 18;
            if (!collapsed) for (var option : options) {
                var element = option.getControl().createElement(new Dim2i(0, y, dim.width(), 18).withParentOffset(dim));
                children.add(element);
                controlElements.add(element);
                y += 18;
            }
            y += 4;
        }
        drawable.addAll(children);
    }
}
