/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.gui.framework.InteractionContext;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.taumc.celeritas.api.options.control.ControlElement;
import org.taumc.celeritas.api.options.structure.Option;

/** Keeps native controls and reserves separate hit boxes for per-option actions. */
public final class OptionActionElement<T> extends ControlElement<T> {

    private final ControlElement<T> control;
    private final Dim2i undo, reset;

    public OptionActionElement(Option<T> option, Dim2i dim) {
        super(option, dim);
        int actions = OptionDefaults.known(option) ? 36 : 18;
        control = option.getControl()
            .createElement(new Dim2i(dim.x(), dim.y(), Math.max(1, dim.width() - actions), dim.height()));
        undo = new Dim2i(dim.getLimitX() - actions, dim.y(), 18, dim.height());
        reset = actions == 36 ? new Dim2i(dim.getLimitX() - 18, dim.y(), 18, dim.height()) : null;
    }

    @Override
    public void render(DrawContext context, int x, int y, float delta) {
        control.render(context, x, y, delta);
        drawAction(context, undo, "↶", option.isAvailable() && option.hasChanged(), x, y);
        if (reset != null)
            drawAction(context, reset, "↺", OptionDefaults.canReset(option), x, y);
        String key = undo.containsCursor(x, y)
            ? "celeritasextra.option_action.undo"
            : reset != null && reset.containsCursor(x, y) ? "celeritasextra.option_action.reset" : null;
        if (key != null) {
            String text = Translations.format(key);
            int left = Math.max(dim.x(), undo.x() - context.getStringWidth(text) - 8);
            context.fill(left, dim.y(), undo.x(), dim.getLimitY(), 0xF0202020);
            context.drawString(text, left + 4, dim.getCenterY() - context.lineHeight() / 2, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        // Native option tooltips apply to the control; action hints are drawn inside the row.
        return control.isMouseOver(x, y);
    }

    private static void drawAction(DrawContext context, Dim2i bounds, String label, boolean enabled, int x, int y) {
        context.fill(bounds.x(), bounds.y(), bounds.getLimitX(), bounds.getLimitY(),
            enabled && bounds.containsCursor(x, y) ? 0xFF555555 : 0x90303030);
        context.drawString(TextComponent.literal(label), bounds.getCenterX() - context.getStringWidth(label) / 2,
            bounds.getCenterY() - context.lineHeight() / 2, enabled ? 0xFFFFFFFF : 0xFF777777, false);
    }

    @Override
    public boolean mouseClicked(InteractionContext context, double x, double y, int button) {
        if (button == 0 && undo.containsCursor(x, y)) {
            if (option.isAvailable() && option.hasChanged())
                option.reset();
            return true;
        }
        if (button == 0 && reset != null && reset.containsCursor(x, y)) {
            OptionDefaults.reset(option);
            return true;
        }
        return control.mouseClicked(context, x, y, button);
    }

    @Override
    public boolean mouseReleased(InteractionContext context, double x, double y, int button) {
        return control.mouseReleased(context, x, y, button);
    }

    @Override
    public boolean mouseDragged(InteractionContext context, double x, double y, int button, double dx, double dy) {
        return control.mouseDragged(context, x, y, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(InteractionContext context, double x, double y, double horizontal, double vertical) {
        return control.isMouseOver(x, y) && control.mouseScrolled(context, x, y, horizontal, vertical);
    }

}
