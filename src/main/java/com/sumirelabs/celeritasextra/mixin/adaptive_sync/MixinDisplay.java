package com.sumirelabs.celeritasextra.mixin.adaptive_sync;

import com.sumirelabs.celeritasextra.CeleritasExtraMod;
import com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod;
import com.sumirelabs.celeritasextra.compat.window.WindowCompat;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Implements adaptive VSync by intercepting Display.setVSyncEnabled()
 * Port of sodium-extra's adaptive_sync.MixinWindow
 * <p>
 * In 1.21.1: Window.updateVsync() with GLFW.glfwSwapInterval redirect
 * In 1.12.2 (Cleanroom): Display.setVSyncEnabled() from the selected compatibility layer
 * <p>
 * Adaptive VSync uses swap interval -1, which enables VSync when FPS is above
 * the monitor refresh rate and disables it when below, reducing stuttering.
 * Uses SDL on Cleanroom 0.7+, and GLFW tear-control extensions on 0.6.x.
 */
@Mixin(value = Display.class, remap = false)
public class MixinDisplay {

    @Inject(
            method = "setVSyncEnabled",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void onSetVSyncEnabled(boolean enable, CallbackInfo ci) {
        if (enable && CeleritasExtraClientMod.options().extraSettings.useAdaptiveSync) {
            if (WindowCompat.tryAdaptiveVsync()) {
                ci.cancel();
            } else {
                CeleritasExtraMod.LOGGER.warn("Adaptive VSync not supported, falling back to normal VSync.");
                CeleritasExtraClientMod.options().extraSettings.useAdaptiveSync = false;
                CeleritasExtraClientMod.options().writeChanges();
            }
        }
    }
}
