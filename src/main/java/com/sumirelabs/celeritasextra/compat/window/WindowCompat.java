package com.sumirelabs.celeritasextra.compat.window;

import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.SDLHooks;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.common.ForgeEarlyConfig;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.system.MemoryStack;

/** Window and IME operations for the supported Cleanroom 0.7.x runtime. */
public final class WindowCompat {
    private WindowCompat() { }

    public static boolean isBorderless() {
        var window = SDL.window();
        return window != null && window.fullscreen() && ForgeEarlyConfig.WINDOW_BORDERLESS_REPLACES_FULLSCREEN;
    }

    public static boolean isVisible() {
        var window = SDL.window();
        return window != null && !window.minimized() && !window.hidden();
    }

    public static boolean supportsAdaptiveVsync() {
        // Preserve the current context's interval: inspecting options must not change VSync.
        try (var stack = MemoryStack.stackPush()) {
            var previous = stack.mallocInt(1);
            if (!SDLVideo.SDL_GL_GetSwapInterval(previous)) return false;
            if (!SDLVideo.SDL_GL_SetSwapInterval(-1)) return false;
            if (!SDLVideo.SDL_GL_SetSwapInterval(previous.get(0))) {
                throw new IllegalStateException("SDL could not restore the swap interval after probing adaptive VSync");
            }
            return true;
        }
    }

    public static boolean tryAdaptiveVsync() { return SDLVideo.SDL_GL_SetSwapInterval(-1); }
    public static boolean composingText() { return SDLHooks.composing(); }
    public static int textScrollOffset(GuiTextField field) { return field.getLineScrollOffset(); }
    public static void drawTextInput(GuiTextField field, int x, int y) { SDLHooks.textFieldDraw(field, x, y); }
}
