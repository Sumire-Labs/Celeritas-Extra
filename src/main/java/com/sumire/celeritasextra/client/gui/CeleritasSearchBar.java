package com.sumire.celeritasextra.client.gui;

import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.gui.framework.InteractionContext;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.embeddedt.embeddium.impl.gui.theme.DefaultColors;
import org.embeddedt.embeddium.impl.gui.widgets.AbstractWidget;
import org.embeddedt.embeddium.impl.gui.widgets.FlatButtonWidget;
import org.embeddedt.embeddium.impl.util.Dim2i;

/** Celeritas-themed drawing; text editing is supplied by the screen's input model. */
public final class CeleritasSearchBar extends AbstractWidget {
    private final Dim2i bounds;
    private final FlatButtonWidget.Style style = FlatButtonWidget.Style.defaults();
    private final FlatButtonWidget clearButton;
    private String text = "", placeholder = "", results = "";
    private int cursor, selection, visibleStart;
    private boolean focused, blink;

    public CeleritasSearchBar(Dim2i bounds, Runnable clear) {
        this.bounds = bounds;
        clearButton = new FlatButtonWidget(new Dim2i(bounds.getLimitX() - 20, bounds.y(), 20, bounds.height()),
                TextComponent.literal("×"), clear);
        var clearStyle = FlatButtonWidget.Style.defaults();
        clearStyle.bgDefault = 0; // The shared panel already supplies the background.
        clearStyle.bgDisabled = 0;
        clearButton.setStyle(clearStyle);
    }

    public void update(String text, int cursor, int selection, boolean focused, boolean blink,
                       String placeholder, String results) {
        this.text = text;
        this.cursor = Math.clamp(cursor, 0, text.length());
        this.selection = Math.clamp(selection, 0, text.length());
        this.focused = focused;
        this.blink = blink;
        this.placeholder = placeholder;
        this.results = results;
        clearButton.setVisible(!text.isEmpty());
    }

    private int textRight(DrawContext context) {
        if (text.isEmpty()) return bounds.getLimitX() - 6;
        String counter = context.substrByWidth(results, Math.max(0, bounds.width() / 3));
        return Math.max(bounds.x() + 6, bounds.getLimitX() - 26 - context.getStringWidth(counter) - 10);
    }

    private String visibleText(DrawContext context) {
        int width = Math.max(0, textRight(context) - bounds.x() - 7);
        visibleStart = Math.clamp(visibleStart, 0, text.length());
        // GuiTextField keeps the active cursor and the other end of a selection
        // separately. Keep the active cursor visible while extending a selection.
        if (cursor < visibleStart) visibleStart = cursor;
        while (visibleStart < cursor && context.getStringWidth(text.substring(visibleStart, cursor)) > width)
            visibleStart++;
        return context.substrByWidth(text.substring(visibleStart), width);
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(bounds.x(), bounds.y(), bounds.getLimitX(), bounds.getLimitY(), style.bgDefault);
        int baseline = bounds.getCenterY() - context.lineHeight() / 2;
        int left = bounds.x() + 6;
        int right = textRight(context);
        String visible = visibleText(context);
        context.enableScissor(left, bounds.y(), right, bounds.getLimitY());
        try {
            if (cursor != selection) {
                int first = Math.clamp(Math.min(cursor, selection) - visibleStart, 0, visible.length());
                int last = Math.clamp(Math.max(cursor, selection) - visibleStart, 0, visible.length());
                context.fill(left + context.getStringWidth(visible.substring(0, first)), baseline - 1,
                        left + context.getStringWidth(visible.substring(0, last)), baseline + context.lineHeight(),
                        0x60000000 | (DefaultColors.ELEMENT_ACTIVATED & 0xFFFFFF));
            }
            String label = text.isEmpty() && !focused ? context.substrByWidth(placeholder, Math.max(0, right - left)) : visible;
            context.drawString(TextComponent.literal(label), left, baseline,
                    text.isEmpty() && !focused ? style.textDisabled : style.textDefault, false);
            if (focused && blink) {
                int caret = Math.clamp(cursor - visibleStart, 0, visible.length());
                int x = left + context.getStringWidth(visible.substring(0, caret));
                context.fill(x, baseline - 1, x + 1, baseline + context.lineHeight(), style.textDefault);
            }
        } finally { context.disableScissor(); }
        if (focused) context.fill(bounds.x(), bounds.getLimitY() - 1, bounds.getLimitX(), bounds.getLimitY(),
                DefaultColors.ELEMENT_ACTIVATED);
        if (!text.isEmpty()) {
            String counter = context.substrByWidth(results, Math.max(0, bounds.width() / 3));
            context.drawString(TextComponent.literal(counter), bounds.getLimitX() - 26 - context.getStringWidth(counter),
                    baseline, style.textDisabled, false);
        }
        clearButton.render(context, mouseX, mouseY, delta);
    }

    public int cursorAt(DrawContext context, double mouseX) {
        String visible = visibleText(context);
        double distance = Math.max(0, mouseX - bounds.x() - 6);
        for (int i = 0; i < visible.length(); i++) {
            int before = context.getStringWidth(visible.substring(0, i));
            int after = context.getStringWidth(visible.substring(0, i + 1));
            if (distance < (before + after) / 2.0) return visibleStart + i;
        }
        return visibleStart + visible.length();
    }

    public boolean clickClear(InteractionContext context, double x, double y, int button) {
        return !text.isEmpty() && clearButton.mouseClicked(context, x, y, button);
    }

    @Override public boolean isMouseOver(double x, double y) { return bounds.containsCursor(x, y); }
    public Dim2i bounds() { return bounds; }
}
