/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.sumirelabs.celeritasextra.mixin.options_search;
import org.embeddedt.embeddium.impl.gui.framework.FontMetricsProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.taumc.celeritas.api.options.structure.Option;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.sumirelabs.celeritasextra.client.gui.OptionSections;
/** Group names remain searchable with Celeritas's native engine too. */
@Pseudo
@Mixin(targets = "org.embeddedt.embeddium.impl.gui.frame.components.SearchTextFieldModel", remap = false)
public abstract class MixinNativeSearchTerms {

    @Shadow
    @Final private FontMetricsProvider font;
    @ModifyReturnValue(method = "getSearchableText", at = @At("RETURN"))
    private String celeritasExtra$sectionTerms(String original, Option<?> option) {
        return original + OptionSections.searchTerms(option, font);
    }

}
