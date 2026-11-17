/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.mixin.options_search;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod;

/** Prevents the replaced native field from drawing or consuming input behind Extra's search bar. */
@Pseudo
@Mixin(targets = "org.embeddedt.embeddium.impl.gui.frame.components.SearchTextFieldComponent", remap = false)
public abstract class MixinNativeSearchComponent {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$hideNativeSearch(CallbackInfo ci) {
        if (CeleritasExtraClientMod.options().extraSettings.replaceSearchWithRso)
            ci.cancel();
    }

    @Inject(method = {"mouseClicked", "charTyped", "keyPressed", "isMouseOver"}, at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$skipNativeSearchInput(CallbackInfoReturnable<Boolean> cir) {
        if (CeleritasExtraClientMod.options().extraSettings.replaceSearchWithRso)
            cir.setReturnValue(false);
    }

}
