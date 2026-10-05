package com.sumirelabs.celeritasextra.mixin.actinium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.sumirelabs.celeritasextra.client.ItemFrameLodState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Cached fast item geometry cannot honor per-frame LOD quad filtering. */
@Mixin(targets = "com.dhj.actinium.config.ActiniumRuntimeOptions", remap = false)
public abstract class MixinFastLitItemLod {
    @ModifyReturnValue(method = "useFastLitItemRendering", at = @At("RETURN"))
    private static boolean celeritasExtra$allowLodFiltering(boolean enabled) {
        return enabled && !ItemFrameLodState.active;
    }
}
