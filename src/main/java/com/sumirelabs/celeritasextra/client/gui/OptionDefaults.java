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
import org.taumc.celeritas.impl.gui.MinecraftOptionsStorage;

import com.sumirelabs.celeritasextra.mixin.options_search.OptionBindingAccess;

import net.minecraft.client.Minecraft;

/** Adapts native defaults and add-on default capabilities to the legacy renderer options API. */
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
        if (DEFAULTS.containsKey(option))
            return true;
        if (!DEFAULTS.containsKey(option) && option.getStorage() != null
            && option.getStorage().getClass() == MinecraftOptionsStorage.class) {
            Object value = VanillaOptionDefaults.find(option.getId(), Minecraft.getMinecraft().isJava64bit());
            if (value != null && option.getValue() != null && option.getValue().getClass() == value.getClass())
                DEFAULTS.put(option, value);
        }
        if (!DEFAULTS.containsKey(option) && option.getStorage() != null
            && option.getStorage().getClass() == SodiumGameOptions.class) {
            var access = bindingAccess(option);
            if (access != null)
                registerRendererDefault(option, access);
        }
        if (option.getStorage() instanceof DefaultOptionStorage<?> && bindingAccess(option) != null)
            return true;
        return DEFAULTS.containsKey(option);
    }

    private static OptionBindingAccess bindingAccess(Option<?> option) {
        Option<?> current = option;
        for (int depth = 0; depth < 8; depth++) {
            if (current instanceof OptionBindingAccess access)
                return access;
            var control = current.getControl();
            var next = control == null ? null : control.getOption();
            if (next == null || next == current || next.getStorage() != option.getStorage())
                return null;
            current = next;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static void registerRendererDefault(Option<?> option, OptionBindingAccess access) {
        var binding = (OptionBinding<SodiumGameOptions, ?>) access.celeritasExtra$binding();
        DEFAULTS.put(option, binding.getValue(new SodiumGameOptions()));
    }

    private static Object value(Option<?> option) {
        if (option instanceof DefaultValueOption<?> provider)
            return provider.getDefaultValue();
        if (option.getStorage() instanceof DefaultOptionStorage<?> storage) {
            var access = bindingAccess(option);
            if (access != null)
                return storageValue(access, storage);
        }
        return DEFAULTS.get(option);
    }

    @SuppressWarnings("unchecked")
    private static Object storageValue(OptionBindingAccess access, DefaultOptionStorage<?> storage) {
        return ((OptionBinding<Object, ?>) access.celeritasExtra$binding()).getValue(storage.getDefaultData());
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
