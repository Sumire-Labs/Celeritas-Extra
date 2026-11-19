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

package com.sumirelabs.celeritasextra.client.gui.rso.layout;

/** RSO's 16:9 outer layout, top search row and bottom toolbar. */
public record ScreenLayout(LayoutBounds root, LayoutBounds tabs, LayoutBounds search) {

    public static final float ASPECT_RATIO = 16f / 9f;
    public static ScreenLayout create(int width, int height) {
        int newWidth = width;
        if ((float) width / height > ASPECT_RATIO)
            newWidth = (int) (height * ASPECT_RATIO);
        var root = new LayoutBounds((width - newWidth) / 2, 0, newWidth, height);
        var tabs = new LayoutBounds(root.x() + root.width() / 20 / 2, root.height() / 4 / 2,
            root.width() - root.width() / 20, root.height() / 4 * 3);
        return new ScreenLayout(root, tabs, new LayoutBounds(tabs.x(), tabs.y() - 26, tabs.width(), 20));
    }

    public static int sidebarWidth(int textWidth, int frameWidth, boolean scrollable) {
        int maximum = Math.clamp((int) (frameWidth * 0.35), 120, 220);
        // Reserve a usable content panel on legacy GUI scales smaller than RSO's minimum rail width.
        maximum = Math.min(maximum, Math.max(40, frameWidth / 2));
        return Math.clamp(textWidth + (scrollable ? 32 : 24), Math.min(120, maximum), maximum);
    }

}
