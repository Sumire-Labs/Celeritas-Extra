/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.compat;

/** Optional renderer detection without defining its classes during startup. */
public final class RendererCompat {

    private RendererCompat() {}

    private static final boolean CELTIUM_ITEM_CACHE = RendererCompat.class.getClassLoader()
        .getResource("org/taumc/celeritas/mixin/core/RenderItemMixin.class") != null;
    public static boolean hasNativeItemCache() {
        return CELTIUM_ITEM_CACHE;
    }

}
