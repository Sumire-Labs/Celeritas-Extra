/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

public final class SliderScroll {

    private SliderScroll() {}

    public static int adjust(int value, int min, int max, int interval, double delta) {
        if (!Double.isFinite(delta) || delta == 0)
            return value;
        long next = (long) value + (delta > 0 ? (long) interval : -(long) interval);
        return (int) Math.max(min, Math.min(max, next));
    }

}
