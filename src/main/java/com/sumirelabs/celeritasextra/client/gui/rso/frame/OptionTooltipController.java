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

import java.util.ArrayList;

import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.taumc.celeritas.api.options.control.ControlElement;
import org.taumc.celeritas.api.options.structure.OptionPage;

import com.sumirelabs.celeritasextra.client.gui.rso.theme.GuiThemes;

/** RSO's delayed bordered tooltips, drawn after viewport clipping in the legacy immediate renderer. */
public final class OptionTooltipController {

    private ControlElement<?> target;
    private long started;
    public void update(ControlElement<?> row) {
        if (target != row) {
            target = row;
            started = System.nanoTime();
        }
    }
    public void render(DrawContext context, OptionPage page, Dim2i viewport, int offset) {
        if (target == null || System.nanoTime() - started < 500_000_000L)
            return;
        var option = target.getOption();
        int width = Math.max(16, Math.min(280, viewport.width()));
        var lines = new ArrayList<TextComponent>(context.split(option.getTooltip(), Math.max(1, width - 6)));
        if (option.getImpact() != null)
            lines.add(TextComponent.translatable("sodium.options.performance_impact_string",
                option.getImpact().getLocalizedName()));
        if (option.getId() != null && !option.getId().getModId().equals(page.getId().getModId()))
            lines.add(TextComponent.translatable("celeritas.options.added_by_mod_string",
                context.getFriendlyModName(option.getId().getModId())));
        if (lines.isEmpty())
            return;
        int height = lines.size() * 12 + 6;
        int x = viewport.x(), rowY = target.getDimensions().y() - offset;
        int y = rowY + target.getDimensions().height();
        if (y + height > viewport.getLimitY())
            y = Math.max(2, rowY - height);
        context.pushMatrix();
        try {
            context.translate(0, 0, 90);
            context.fill(x, y, x + width, y + height, 0xE0000000);
            context.drawBorder(x, y, x + width, y + height, GuiThemes.SELECTED_UNDERLINE);
            for (int i = 0; i < lines.size(); i++)
                context.drawString(lines.get(i), x + 3, y + 3 + i * 12, 0xFFFFFFFF, true);
        } finally {
            context.popMatrix();
        }
    }

}
