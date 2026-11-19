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

import org.embeddedt.embeddium.impl.gui.widgets.FlatButtonWidget;

/** RSO's palette; the native Celeritas option/control API supplies the settings. */
public final class GuiThemes {

    public static final GuiTheme DEFAULT_BUTTON = new GuiTheme(0xFFFFFFFF, 0xFFFFFFFF, 0xFFAAAAAA,
        0xE0000000, 0x90000000, 0x40000000);
    public static final int FLAT_BUTTON_FOCUS_BORDER = 0x8000FFEE;
    public static final int OPTION_FOCUS_BORDER = 0xFFFFFFFF;
    public static final int SELECTED_UNDERLINE = 0xFF94E4D3;
    private GuiThemes() {}

    public static FlatButtonWidget.Style buttonStyle() {
        var style = new FlatButtonWidget.Style();
        style.bgDefault = DEFAULT_BUTTON.bgDefault;
        style.bgHovered = DEFAULT_BUTTON.bgHighlight;
        style.bgDisabled = DEFAULT_BUTTON.bgInactive;
        style.textDefault = DEFAULT_BUTTON.themeLighter;
        style.textDisabled = DEFAULT_BUTTON.themeDarker;
        return style;
    }

}
