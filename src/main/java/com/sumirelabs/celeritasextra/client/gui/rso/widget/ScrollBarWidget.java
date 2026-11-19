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

package com.sumirelabs.celeritasextra.client.gui.rso.widget;

import java.util.function.IntConsumer;

import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.gui.framework.InteractionContext;
import org.embeddedt.embeddium.impl.gui.widgets.AbstractWidget;
import org.embeddedt.embeddium.impl.util.Dim2i;

/** RSO's bordered track, thumb dragging and six-pixel wheel steps, adapted to legacy input. */
public final class ScrollBarWidget extends AbstractWidget {

    private final Dim2i track, area;
    private final int visible, content;
    private final IntConsumer listener;
    private int offset, clickOffset;
    private boolean dragging;
    private Dim2i thumb;
    public ScrollBarWidget(Dim2i track, int content, int visible, Dim2i area, int offset, IntConsumer listener) {
        this.track = track;
        this.area = area;
        this.content = Math.max(1, content);
        this.visible = Math.max(1, visible);
        this.listener = listener;
        setOffset(offset);
    }
    public int offset() {
        return offset;
    }
    public boolean isDragging() {
        return dragging;
    }
    public void setOffset(int value) {
        offset = Math.clamp(value, 0, Math.max(0, content - visible));
        int size = Math.max(1, track.height() - 4);
        int length = Math.min(size, Math.max(6, (int) ((long) visible * size / content)));
        int position = content <= visible ? 0 : (int) ((long) offset * (size - length) / (content - visible));
        thumb = new Dim2i(track.x() + 2, track.y() + 2 + position, Math.max(1, track.width() - 4), length);
    }
    private void update(int value) {
        setOffset(value);
        listener.accept(offset);
    }
    @Override
    public void render(DrawContext context, int x, int y, float delta) {
        context.drawBorder(track.x(), track.y(), track.getLimitX(), track.getLimitY(), 0xFFAAAAAA);
        context.fill(thumb.x(), thumb.y(), thumb.getLimitX(), thumb.getLimitY(), 0xFFAAAAAA);
    }
    @Override
    public boolean isMouseOver(double x, double y) {
        return track.containsCursor(x, y);
    }
    @Override
    public boolean mouseClicked(InteractionContext context, double x, double y, int button) {
        if (button == 0)
            dragging = false;
        if (button != 0 || !isMouseOver(x, y))
            return false;
        dragging = true;
        clickOffset = thumb.containsCursor(x, y) ? (int) y - thumb.getCenterY() : 0;
        if (!thumb.containsCursor(x, y))
            move(y);
        return true;
    }
    private void move(double y) {
        int distance = Math.max(1, track.height() - 4 - thumb.height());
        update((int) ((y - track.y() - 2 - thumb.height() / 2.0 - clickOffset) * Math.max(0, content - visible)
            / distance));
    }
    @Override
    public boolean mouseDragged(InteractionContext context, double x, double y, int button, double dx, double dy) {
        if (!dragging || button != 0)
            return false;
        move(y);
        return true;
    }
    @Override
    public boolean mouseReleased(InteractionContext context, double x, double y, int button) {
        boolean wasDragging = dragging;
        if (button == 0)
            dragging = false;
        return wasDragging;
    }
    @Override
    public boolean mouseScrolled(InteractionContext context, double x, double y, double horizontal, double vertical) {
        if (vertical == 0 || !(track.containsCursor(x, y) || area.containsCursor(x, y)))
            return false;
        update((int) Math.round(offset - vertical * 6));
        return true;
    }

}
