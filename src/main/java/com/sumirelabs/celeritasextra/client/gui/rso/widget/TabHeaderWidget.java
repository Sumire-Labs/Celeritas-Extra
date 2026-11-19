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

import java.util.Objects;

import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.embeddedt.embeddium.impl.gui.widgets.FlatButtonWidget;
import org.embeddedt.embeddium.impl.util.Dim2i;

import com.sumirelabs.celeritasextra.client.gui.rso.theme.GuiThemes;

import net.minecraftforge.fml.common.Loader;

/** RSO's icon/name/version header and dwell/scroll animation, using Forge mod metadata. */
public final class TabHeaderWidget extends FlatButtonWidget {

    private final String mod;
    private final boolean active, collapsed, searching;
    private String version;
    public TabHeaderWidget(Dim2i dim, String mod, boolean active, boolean collapsed, boolean searching,
        Runnable action) {
        super(dim, TextComponent.literal(""), action);
        this.mod = mod;
        this.active = active;
        this.collapsed = collapsed;
        this.searching = searching;
    }
    @Override
    public void render(DrawContext context, int x, int y, float delta) {
        context.fill(dim.x(), dim.y(), dim.getLimitX(), dim.getLimitY(),
            dim.containsCursor(x, y) ? 0xE0000000 : 0xBE000000);
        if (active)
            context.fill(dim.x(), dim.y(), dim.x() + 2, dim.getLimitY(), GuiThemes.SELECTED_UNDERLINE);
        int icon = dim.height() - 8;
        com.sumirelabs.celeritasextra.client.gui.rso.theme.IconRenderer.draw(context,
            Objects.requireNonNullElse(context.getModLogoPath(mod), "textures/misc/unknown_pack.png"),
            dim.x() + 4, dim.getCenterY() - icon / 2, icon);
        int left = dim.x() + icon + 8;
        if (version == null) {
            var container = Loader.instance().getIndexedModList().get(mod);
            version = container == null ? "" : container.getVersion();
        }
        drawScrollingString(context, context.extractString(context.getFriendlyModName(mod)), left, dim.y() + 2,
            0xFFFFFFFF, Math.max(0, dim.getLimitX() - left - 12));
        drawScrollingString(context, version, left, dim.y() + 12, 0xFFAAAAAA,
            Math.max(0, dim.getLimitX() - left - 12));
        if (!searching)
            context.drawString(collapsed ? "+" : "-", dim.getLimitX() - 10, dim.getCenterY() - 4, 0xFFAAAAAA);
    }
    private static void drawScrollingString(DrawContext context, String text, int x, int y, int color, int width) {
        if (width <= 0 || text.isEmpty())
            return;
        int overflow = context.getStringWidth(text) - width;
        if (overflow <= 0) {
            context.drawString(TextComponent.literal(text), x, y, color, false);
            return;
        }
        long travel = Math.max(1, (long) Math.ceil(overflow / 0.01));
        long time = net.minecraft.client.Minecraft.getSystemTime() % (2000 + travel * 2);
        double offset = time < 1000
            ? 0
            : time < 1000 + travel
                ? Math.min(overflow, (time - 1000) * 0.01)
                : time < 2000 + travel ? overflow : Math.max(0, overflow - (time - 2000 - travel) * 0.01);
        // Clip text on the CPU so a header never replaces its parent's scroll viewport scissor.
        int first = 0;
        while (first < text.length() && context.getStringWidth(text.substring(0, first + 1)) <= offset)
            first++;
        String visible = context.substrByWidth(text.substring(first), width);
        context.drawString(TextComponent.literal(visible), x, y, color, false);
    }

}
