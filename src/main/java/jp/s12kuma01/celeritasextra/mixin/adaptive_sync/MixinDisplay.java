package jp.s12kuma01.celeritasextra.mixin.adaptive_sync;

import jp.s12kuma01.celeritasextra.CeleritasExtraMod;
import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Implements adaptive VSync by intercepting Display.setVSyncEnabled()
 * Port of sodium-extra's adaptive_sync.MixinWindow
 * <p>
 * In 1.21.1: Window.updateVsync() with GLFW.glfwSwapInterval redirect
 * In 1.12.2 (Cleanroom): Display.setVSyncEnabled() from lwjglx compat layer
 * <p>
 * Adaptive VSync uses swap interval -1, which enables VSync when FPS is above
 * the monitor refresh rate and disables it when below, reducing stuttering.
 * Requires GLX_EXT_swap_control_tear (Linux) or WGL_EXT_swap_control_tear (Windows).
 */
@Mixin(targets = "org.lwjgl.opengl.Display")
public class MixinDisplay {

    @Inject(
            method = "setVSyncEnabled",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void onSetVSyncEnabled(boolean enable, CallbackInfo ci) {
        if (CeleritasExtraClientMod.options().extraSettings.useAdaptiveSync && enable) {
            if (GLFW.glfwExtensionSupported("GLX_EXT_swap_control_tear")
                    || GLFW.glfwExtensionSupported("WGL_EXT_swap_control_tear")) {
                GLFW.glfwSwapInterval(-1);
                ci.cancel();
            } else {
                CeleritasExtraMod.LOGGER.warn("Adaptive VSync not supported, falling back to normal VSync.");
                CeleritasExtraClientMod.options().extraSettings.useAdaptiveSync = false;
                CeleritasExtraClientMod.options().writeChanges();
            }
        }
    }
}
