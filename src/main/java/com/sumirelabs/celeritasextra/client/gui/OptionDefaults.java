/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import org.taumc.celeritas.api.options.structure.Option;

/** Only options with an explicitly registered default offer Reset; Undo uses the renderer's binding. */
public final class OptionDefaults {

    private static final Map<Option<?>, Object> DEFAULTS = new WeakHashMap<>();

    private OptionDefaults() {}

    public static <T, O extends Option<T>> O register(O option, T value) {
        DEFAULTS.put(option, value);
        return option;
    }

    public static boolean known(Option<?> option) {
        return DEFAULTS.containsKey(option);
    }

    public static boolean canReset(Option<?> option) {
        return option.isAvailable() && known(option) && !Objects.equals(option.getValue(), DEFAULTS.get(option));
    }

    @SuppressWarnings("unchecked")
    public static <T> void reset(Option<T> option) {
        if (canReset(option))
            option.setValue((T) DEFAULTS.get(option));
    }

}
