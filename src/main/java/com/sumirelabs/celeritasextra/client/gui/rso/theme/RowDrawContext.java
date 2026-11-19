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

package com.sumirelabs.celeritasextra.client.gui.rso.theme;

import java.util.List;

import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.embeddedt.embeddium.impl.util.Dim2i;

/** Applies RSO's row background while delegating every native/add-on control drawing operation. */
public final class RowDrawContext implements DrawContext {

    private static final org.embeddedt.embeddium.impl.gui.widgets.FlatButtonWidget.Style NATIVE_STYLE = org.embeddedt.embeddium.impl.gui.widgets.FlatButtonWidget.Style
        .defaults();

    private DrawContext delegate;
    private Dim2i row;
    private boolean highlighted;
    public RowDrawContext bind(DrawContext context, Dim2i bounds, boolean highlighted) {
        delegate = context;
        row = bounds;
        this.highlighted = highlighted;
        return this;
    }
    @Override
    public void fill(int x, int y, int right, int bottom, int color) {
        if (x == row.x() && y == row.y() && right == row.getLimitX() && bottom == row.getLimitY()
            && (color == NATIVE_STYLE.bgDefault || color == NATIVE_STYLE.bgHovered || color == NATIVE_STYLE.bgDisabled))
            color = highlighted ? 0xE0000000 : 0x40000000;
        delegate.fill(x, y, right, bottom, color);
    }
    @Override
    public int drawString(TextComponent text, int x, int y, int color, boolean shadow) {
        return delegate.drawString(text, x, y, color, shadow);
    }
    @Override
    public int getStringWidth(TextComponent text) {
        return delegate.getStringWidth(text);
    }
    @Override
    public String substrByWidth(String text, int width) {
        return delegate.substrByWidth(text, width);
    }
    @Override
    public List<TextComponent> split(TextComponent text, int width) {
        return delegate.split(text, width);
    }
    @Override
    public String extractString(TextComponent text) {
        return delegate.extractString(text);
    }
    @Override
    public int lineHeight() {
        return delegate.lineHeight();
    }
    @Override
    public void blitWholeImage(String texture, int x, int y, int width, int height) {
        delegate.blitWholeImage(texture, x, y, width, height);
    }
    @Override
    public void pushMatrix() {
        delegate.pushMatrix();
    }
    @Override
    public void popMatrix() {
        delegate.popMatrix();
    }
    @Override
    public void translate(double x, double y, double z) {
        delegate.translate(x, y, z);
    }
    @Override
    public void enableScissor(int x, int y, int right, int bottom) {
        delegate.enableScissor(x, y, right, bottom);
    }
    @Override
    public void disableScissor() {
        delegate.disableScissor();
    }
    @Override
    public String getModLogoPath(String mod) {
        return delegate.getModLogoPath(mod);
    }
    @Override
    public TextComponent getFriendlyModName(String mod) {
        return delegate.getFriendlyModName(mod);
    }

}
