/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

/** Texture identifiers shared by renderer-native and renderer-independent screens. */
public enum OptionActionIcon {

    UNDO("celeritasextra:textures/gui/undo_to_unmodified.png"), RESET(
        "celeritasextra:textures/gui/reset_to_default.png");

    public static final int DRAW_SIZE = 11;

    private final String texture;
    OptionActionIcon(String texture) {
        this.texture = texture;
    }
    public String texture() {
        return texture;
    }

}
