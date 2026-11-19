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

import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.embeddedt.embeddium.impl.gui.widgets.FlatButtonWidget;
import org.embeddedt.embeddium.impl.util.Dim2i;

import com.sumirelabs.celeritasextra.client.gui.rso.theme.GuiThemes;

/** RSO tab rendering, with Celeritas retaining its original action callback. */
public final class TabButtonWidget extends FlatButtonWidget {

    private final TextComponent title;
    private final boolean active;
    public TabButtonWidget(Dim2i dim, TextComponent title, boolean selected, Runnable action) {
        super(dim, title, action);
        this.title = title;
        this.active = selected;
        setStyle(GuiThemes.buttonStyle());
    }
    @Override
    public void render(DrawContext context, int x, int y, float delta) {
        boolean hovered = dim.containsCursor(x, y);
        var theme = GuiThemes.DEFAULT_BUTTON;
        context.fill(dim.x(), dim.y(), dim.getLimitX(), dim.getLimitY(), hovered ? theme.bgHighlight : theme.bgDefault);
        context.drawString(
            TextComponent.literal(context.substrByWidth(context.extractString(title), Math.max(0, dim.width() - 16))),
            dim.x() + 10, dim.getCenterY() - context.lineHeight() / 2,
            active || hovered ? theme.themeLighter : theme.themeDarker, false);
        if (active)
            context.fill(dim.x(), dim.y(), dim.x() + 2, dim.getLimitY(), GuiThemes.SELECTED_UNDERLINE);
    }

}
