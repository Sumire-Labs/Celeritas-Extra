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

import org.embeddedt.embeddium.impl.gui.framework.DrawContext;

import net.minecraft.client.renderer.GlStateManager;

/** RSO header icons, with explicit fixed-function tint/blending for Minecraft 1.12.2. */
public final class IconRenderer {

    private IconRenderer() {}
    public static void draw(DrawContext context, String icon, int x, int y, int size) {
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        context.blitWholeImage(icon, x, y, size, size);
        GlStateManager.disableBlend();
    }

}
