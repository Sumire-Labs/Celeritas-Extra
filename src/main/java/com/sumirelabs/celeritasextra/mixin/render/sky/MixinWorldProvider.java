package com.sumirelabs.celeritasextra.mixin.render.sky;

import com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overrides the world's cloud altitude only when a custom cloud height is configured.
 * <p>
 * Injects at the return of {@code WorldProvider.getCloudHeight} and replaces the result with the
 * The default sentinel preserves the value returned by the current dimension provider, including
 * values supplied by other mods.
 */
@Mixin(WorldProvider.class)
public class MixinWorldProvider {

    /**
     * Override the cloud height with a custom non-negative value.
     */
    @Inject(method = "getCloudHeight", at = @At("RETURN"), cancellable = true)
    private void modifyCloudHeight(CallbackInfoReturnable<Float> cir) {
        int configuredHeight = CeleritasExtraClientMod.options().renderSettings.cloudHeight;
        if (configuredHeight >= 0) {
            cir.setReturnValue((float) configuredHeight);
        }
    }
}
