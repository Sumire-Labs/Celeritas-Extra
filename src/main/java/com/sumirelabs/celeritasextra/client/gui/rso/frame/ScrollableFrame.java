/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 FlashyReese
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * Adapted from Reese's Sodium Options 97fff483846c7a162aeebffd48373c903d730c38.
 * Minecraft rendering, input and Sodium dependencies are adapted to Celeritas 1.12.2.
 */

package com.sumirelabs.celeritasextra.client.gui.rso.frame;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.embeddedt.embeddium.impl.gui.frame.AbstractFrame;
import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.gui.framework.Interactable;
import org.embeddedt.embeddium.impl.gui.framework.InteractionContext;
import org.embeddedt.embeddium.impl.util.Dim2i;

import com.sumirelabs.celeritasextra.client.gui.rso.layout.LayoutBounds;
import com.sumirelabs.celeritasextra.client.gui.rso.widget.ScrollBarWidget;

/** RSO viewport sizing and clipping around Celeritas frames; option objects and custom controls remain native. */
public final class ScrollableFrame extends AbstractFrame {

    private final AbstractFrame content;
    private final ScrollBarWidget scrollBar;
    private final AtomicReference<Integer> offset;
    private final Dim2i viewport;
    private Interactable captured;

    public ScrollableFrame(Dim2i dim, int contentHeight, AtomicReference<Integer> offset,
        Function<Dim2i, AbstractFrame> factory) {
        super(dim, false);
        this.offset = offset;
        var bounds = new LayoutBounds(dim.x(), dim.y(), dim.width(), dim.height());
        var layout = ScrollFrameLayout.create(bounds, bounds.withHeight(Math.max(dim.height(), contentHeight)));
        var view = layout.viewport();
        viewport = new Dim2i(view.x(), view.y(), view.width(), view.height());
        content = factory.apply(new Dim2i(dim.x(), dim.y(), layout.contentWidth(), contentHeight));
        if (layout.canScrollVertical()) {
            var track = layout.verticalScrollBarBounds();
            scrollBar = new ScrollBarWidget(new Dim2i(track.x(), track.y(), track.width(), track.height()),
                contentHeight, viewport.height(), viewport, offset.get(), offset::set);
            offset.set(scrollBar.offset());
        } else {
            scrollBar = null;
            offset.set(0);
        }
        children.add(content);
        if (scrollBar != null)
            children.add(scrollBar);
        super.buildFrame();
    }
    @Override
    public void render(DrawContext context, int x, int y, float delta) {
        context.enableScissor(viewport.x(), viewport.y(), viewport.getLimitX(), viewport.getLimitY());
        context.pushMatrix();
        try {
            context.translate(0, -offset.get(), 0);
            boolean inside = viewport.containsCursor(x, y);
            content.render(context, inside ? x : -10000, inside ? y + offset.get() : -10000, delta);
        } finally {
            context.popMatrix();
            context.disableScissor();
        }
        if (scrollBar != null)
            scrollBar.render(context, x, y, delta);
        if (content instanceof TooltipFrame tooltip)
            tooltip.renderTooltip(context, viewport, offset.get());
    }
    @Override
    public boolean mouseClicked(InteractionContext c, double x, double y, int b) {
        if (b == 0)
            captured = null;
        if (scrollBar != null && scrollBar.mouseClicked(c, x, y, b))
            return true;
        if (!viewport.containsCursor(x, y))
            return false;
        boolean consumed = content.mouseClicked(c, x, y + offset.get(), b);
        if (consumed && b == 0)
            captured = content.interactableChildren().filter(child -> child.isMouseOver(x, y + offset.get()))
                .findFirst().orElse(null);
        return consumed;
    }
    @Override
    public boolean mouseDragged(InteractionContext c, double x, double y, int b, double dx, double dy) {
        if (scrollBar != null && scrollBar.mouseDragged(c, x, y, b, dx, dy))
            return true;
        return captured != null
            ? captured.mouseDragged(c, x, y + offset.get(), b, dx, dy)
            : content.mouseDragged(c, x, y + offset.get(), b, dx, dy);
    }
    @Override
    public boolean mouseReleased(InteractionContext c, double x, double y, int b) {
        boolean bar = scrollBar != null && scrollBar.mouseReleased(c, x, y, b);
        boolean child;
        if (captured != null) {
            captured.mouseReleased(c, x, y + offset.get(), b);
            child = true;
            if (b == 0)
                captured = null;
        } else
            child = content.mouseReleased(c, x, y + offset.get(), b);
        return bar || child;
    }
    @Override
    public boolean mouseScrolled(InteractionContext c, double x, double y, double h, double v) {
        if (!dim.containsCursor(x, y))
            return false;
        return (viewport.containsCursor(x, y) && content.mouseScrolled(c, x, y + offset.get(), h, v))
            || (scrollBar != null && scrollBar.mouseScrolled(c, x, y, h, v));
    }
    @Override
    public boolean isMouseOver(double x, double y) {
        // Legacy Celeritas dispatches drag events only through hovered containers.
        return captured != null || (scrollBar != null && scrollBar.isDragging()) || dim.containsCursor(x, y);
    }

}
