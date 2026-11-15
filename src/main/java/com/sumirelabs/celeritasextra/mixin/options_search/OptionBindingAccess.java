/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.mixin.options_search;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.taumc.celeritas.api.options.binding.OptionBinding;
import org.taumc.celeritas.api.options.structure.OptionImpl;

@Mixin(value = OptionImpl.class, remap = false)
public interface OptionBindingAccess {

    @Accessor("binding")
    OptionBinding<?, ?> celeritasExtra$binding();

}
