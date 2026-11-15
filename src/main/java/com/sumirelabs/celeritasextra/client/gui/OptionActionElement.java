/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.gui.framework.InteractionContext;
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.taumc.celeritas.api.options.control.ControlElement;
import org.taumc.celeritas.api.options.structure.Option;

import net.minecraft.client.renderer.GlStateManager;

/** Keeps native controls and reserves separate hit boxes for per-option actions. */
public final class OptionActionElement<T> extends ControlElement<T> {

    private ControlElement<T> control;
    private Dim2i undo, reset;
    private int reservedWidth = -1;
    private int actionMask = -1;
    private boolean holding;

    public OptionActionElement(Option<T> option, Dim2i dim) {
        super(option, dim);
        layout();
    }

    private void layout() {
        if (holding)
            return;
        boolean canUndo = option.isAvailable() && option.hasChanged();
        boolean canReset = OptionDefaults.canReset(option);
        int mask = (canUndo ? 1 : 0) | (canReset ? 2 : 0);
        if (mask == actionMask)
            return;
        actionMask = mask;
        int actions = (canUndo ? 18 : 0) + (canReset ? 18 : 0);
        if (actions != reservedWidth) {
            control = option.getControl()
                .createElement(new Dim2i(dim.x(), dim.y(), Math.max(1, dim.width() - actions), dim.height()));
            reservedWidth = actions;
        }
        undo = canUndo ? new Dim2i(dim.getLimitX() - actions, dim.y(), 18, dim.height()) : null;
        reset = canReset ? new Dim2i(dim.getLimitX() - 18, dim.y(), 18, dim.height()) : null;
    }

    @Override
    public void render(DrawContext context, int x, int y, float delta) {
        layout();
        control.render(context, x, y, delta);
        boolean canUndo = undo != null && option.isAvailable() && option.hasChanged();
        boolean canReset = reset != null && OptionDefaults.canReset(option);
        if (canUndo)
            drawAction(context, undo, OptionActionIcon.UNDO, true, x, y);
        if (canReset)
            drawAction(context, reset, OptionActionIcon.RESET, OptionDefaults.canReset(option), x, y);
        String key = canUndo && undo.containsCursor(x, y)
            ? "celeritasextra.option_action.undo"
            : canReset && reset.containsCursor(x, y) ? "celeritasextra.option_action.reset" : null;
        if (key != null) {
            String text = Translations.format(key);
            int actionStart = dim.getLimitX() - reservedWidth;
            int left = Math.max(dim.x(), actionStart - context.getStringWidth(text) - 8);
            context.fill(left, dim.y(), actionStart, dim.getLimitY(), 0xF0202020);
            context.drawString(text, left + 4, dim.getCenterY() - context.lineHeight() / 2, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        layout();
        // Native option tooltips apply to the control; action hints are drawn inside the row.
        return control.isMouseOver(x, y);
    }

    private static void drawAction(DrawContext context, Dim2i bounds, OptionActionIcon icon, boolean enabled, int x,
        int y) {
        context.fill(bounds.x(), bounds.y(), bounds.getLimitX(), bounds.getLimitY(),
            enabled && bounds.containsCursor(x, y) ? 0xFF555555 : 0x90303030);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        OptionActionIcons.draw(context, bounds, icon);
        GlStateManager.disableBlend();
    }

    @Override
    public boolean mouseClicked(InteractionContext context, double x, double y, int button) {
        layout();
        if (button == 0 && undo != null && undo.containsCursor(x, y) && option.isAvailable() && option.hasChanged()) {
            option.reset();
            return true;
        }
        if (button == 0 && reset != null && reset.containsCursor(x, y) && OptionDefaults.canReset(option)) {
            OptionDefaults.reset(option);
            return true;
        }
        boolean consumed = control.mouseClicked(context, x, y, button);
        holding = consumed && button == 0;
        return consumed;
    }

    @Override
    public boolean mouseReleased(InteractionContext context, double x, double y, int button) {
        boolean consumed = control.mouseReleased(context, x, y, button);
        if (button == 0)
            holding = false;
        return consumed;
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
