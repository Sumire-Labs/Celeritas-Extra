/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import org.embeddedt.embeddium.impl.gui.SodiumGameOptions;
import org.taumc.celeritas.api.options.binding.OptionBinding;
import org.taumc.celeritas.api.options.structure.Option;

import com.sumirelabs.celeritasextra.mixin.options_search.OptionBindingAccess;

/** Only options with an explicitly registered default offer Reset; Undo uses the renderer's binding. */
public final class OptionDefaults {

    private static final Map<Option<?>, Object> DEFAULTS = new WeakHashMap<>();

    private OptionDefaults() {}

    public static <T, O extends Option<T>> O register(O option, T value) {
        DEFAULTS.put(option, value);
        return option;
    }

    public static boolean known(Option<?> option) {
        if (option instanceof DefaultValueOption<?>)
            return true;
        if (!DEFAULTS.containsKey(option) && option instanceof OptionBindingAccess access
            && option.getStorage() != null && option.getStorage().getClass() == SodiumGameOptions.class)
            registerRendererDefault(option, access);
        return DEFAULTS.containsKey(option);
    }

    @SuppressWarnings("unchecked")
    private static void registerRendererDefault(Option<?> option, OptionBindingAccess access) {
        var binding = (OptionBinding<SodiumGameOptions, ?>) access.celeritasExtra$binding();
        DEFAULTS.put(option, binding.getValue(new SodiumGameOptions()));
    }

    private static Object value(Option<?> option) {
        return option instanceof DefaultValueOption<?> provider ? provider.getDefaultValue() : DEFAULTS.get(option);
    }

    public static boolean canReset(Option<?> option) {
        return option.isAvailable() && known(option) && !Objects.equals(option.getValue(), value(option));
    }

    @SuppressWarnings("unchecked")
    public static <T> void reset(Option<T> option) {
        if (canReset(option))
            option.setValue((T) value(option));
    }

}
