package jp.s12kuma01.celeritasextra.client.gui;

import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.embeddedt.embeddium.impl.gui.framework.TextFormattingStyle;
import org.embeddedt.embeddium.impl.gui.widgets.FlatButtonWidget;
import org.embeddedt.embeddium.impl.util.Dim2i;

import java.util.Objects;

/** Celeritas header colors, mod icon and typography with a collapse indicator. */
public final class CollapsibleModHeaderWidget extends FlatButtonWidget {
    private final String mod;
    private final boolean collapsed;

    public CollapsibleModHeaderWidget(Dim2i dim, String mod, boolean collapsed, boolean searching, Runnable toggle) {
        super(dim, TextComponent.literal(""), toggle);
        this.mod = mod;
        this.collapsed = collapsed;
        setLeftAligned(true);
        setEnabled(!searching);
    }

    @Override protected int getLeftAlignedTextOffset(DrawContext context) {
        return super.getLeftAlignedTextOffset(context) + context.lineHeight() + 4;
    }

    @Override public void render(DrawContext context, int x, int y, float delta) {
        int textWidth = Math.max(0, dim.width() - getLeftAlignedTextOffset(context) - 14);
        setLabel(TextComponent.literal(context.substrByWidth(context.extractString(context.getFriendlyModName(mod)), textWidth))
                .withStyle(TextFormattingStyle.UNDERLINE));
        super.render(context, x, y, delta);
        int iconSize = context.lineHeight();
        int textY = dim.getCenterY() - iconSize / 2;
        context.blitWholeImage(Objects.requireNonNullElse(context.getModLogoPath(mod), "textures/misc/unknown_pack.png"),
                dim.x() + 5, textY, iconSize, iconSize);
        context.drawString(collapsed ? "+" : "-", dim.getLimitX() - 10, textY, 0xFFFFFFFF);
    }
}
