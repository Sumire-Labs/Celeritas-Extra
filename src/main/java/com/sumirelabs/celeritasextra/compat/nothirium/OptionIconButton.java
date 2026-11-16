/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.compat.nothirium;

import com.sumirelabs.celeritasextra.client.gui.OptionActionIcon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

/** Uses vanilla button backgrounds and a texture icon without depending on Celeritas's GUI classes. */
final class OptionIconButton extends GuiButton {

    private final ResourceLocation texture;
    OptionIconButton(int id, int x, int y, OptionActionIcon icon) {
        super(id, x, y, 20, 20, "");
        texture = new ResourceLocation(icon.texture());
    }

    @Override
    public void drawButton(Minecraft mc, int x, int y, float partialTicks) {
        if (!visible)
            return;
        super.drawButton(mc, x, y, partialTicks);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        mc.getTextureManager().bindTexture(texture);
        int size = Math.min(OptionActionIcon.DRAW_SIZE, Math.min(width, height) - 2);
        drawModalRectWithCustomSizedTexture(this.x + (width - size) / 2, this.y + (height - size) / 2,
            0, 0, size, size, size, size);
        GlStateManager.disableBlend();
    }

}
