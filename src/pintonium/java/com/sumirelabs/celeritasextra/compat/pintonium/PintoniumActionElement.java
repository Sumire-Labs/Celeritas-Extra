/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.compat.pintonium;

import org.taumc.celeritas.api.options.control.ControlElement;
import org.taumc.celeritas.impl.gui.compat.GuiGraphics;
import org.taumc.celeritas.impl.util.Dim2i;

import com.sumirelabs.celeritasextra.client.gui.OptionActionIcon;
import com.sumirelabs.celeritasextra.client.gui.Translations;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

/** Keeps native hit testing and reserves independent 18px cells for the shared 11px action icons. */
final class PintoniumActionElement extends ControlElement<Object> {

    private final PintoniumOption extra;
    private final PintoniumControl factory;
    private ControlElement<Object> delegate;
    private Dim2i undo, reset;
    private int mask = -1;
    private boolean holding;

    PintoniumActionElement(PintoniumOption option, PintoniumControl factory, Dim2i dim) {
        super(option, dim);
        this.extra = option;
        this.factory = factory;
        layout();
    }

    private void layout() {
        if (holding)
            return;
        int next = (extra.isAvailable() && extra.hasChanged() ? 1 : 0) | (extra.canReset() ? 2 : 0);
        if (next == mask)
            return;
        mask = next;
        int width = Integer.bitCount(mask) * 18;
        delegate = factory.createDelegate(dim.withWidth(Math.max(1, dim.width() - width)));
        delegate.setFocused(isFocused());
        undo = (mask & 1) != 0 ? new Dim2i(dim.getLimitX() - width, dim.y(), 18, dim.height()) : null;
        reset = (mask & 2) != 0 ? new Dim2i(dim.getLimitX() - 18, dim.y(), 18, dim.height()) : null;
    }

    @Override
    public void render(GuiGraphics context, int x, int y, float delta) {
        layout();
        delegate.render(context, x, y, delta);
        hovered = delegate.isHovered();
        if (undo != null)
            drawAction(context, undo, OptionActionIcon.UNDO, x, y);
        if (reset != null)
            drawAction(context, reset, OptionActionIcon.RESET, x, y);
        String key = undo != null && undo.containsCursor(x, y)
            ? "celeritasextra.option_action.undo"
            : reset != null && reset.containsCursor(x, y) ? "celeritasextra.option_action.reset" : null;
        if (key != null) {
            String text = Translations.format(key);
            int right = dim.getLimitX() - Integer.bitCount(mask) * 18;
            int left = Math.max(dim.x(), right - font.getStringWidth(text) - 8);
            context.fill(left, dim.y(), right, dim.getLimitY(), 0xF0202020);
            context.drawString(font, text, left + 4, dim.getCenterY() - 4, 0xFFFFFFFF);
        }
    }

    private static void drawAction(GuiGraphics context, Dim2i bounds, OptionActionIcon icon, int x, int y) {
        context.fill(bounds.x(), bounds.y(), bounds.getLimitX(), bounds.getLimitY(),
            bounds.containsCursor(x, y) ? 0xFF555555 : 0x90303030);
        int size = Math.min(OptionActionIcon.DRAW_SIZE, Math.min(bounds.width(), bounds.height()) - 2);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(icon.texture()));
        Gui.drawScaledCustomSizeModalRect(bounds.getCenterX() - size / 2, bounds.getCenterY() - size / 2,
            0, 0, 20, 20, size, size, 20, 20);
        GlStateManager.disableBlend();
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        layout();
        if (button == 0 && extra.isAvailable()) {
            if (undo != null && undo.containsCursor(x, y)) {
                extra.reset();
                playClickSound();
                return true;
            }
            if (reset != null && reset.containsCursor(x, y)) {
                extra.resetToDefault();
                playClickSound();
                return true;
            }
        }
        boolean consumed = delegate.mouseClicked(x, y, button);
        holding = consumed && button == 0;
        return consumed;
    }
    @Override
    public boolean mouseReleased(double x, double y, int button) {
        boolean consumed = delegate.mouseReleased(x, y, button);
        holding = false;
        return consumed;
    }
    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        return delegate.mouseDragged(x, y, button, dx, dy);
    }
    @Override
    public boolean mouseScrolled(double x, double y, double delta) {
        return delegate.isMouseOver(x, y) && delegate.mouseScrolled(x, y, delta);
    }
    @Override
    public boolean keyPressed(int key) {
        return delegate.keyPressed(key);
    }
    @Override
    public boolean charTyped(char value) {
        return delegate.charTyped(value);
    }
    @Override
    public boolean isMouseOver(double x, double y) {
        return delegate.isMouseOver(x, y);
    }
    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (delegate != null)
            delegate.setFocused(focused);
    }

}
