package com.sumirelabs.celeritasextra.compat.window;

import org.lwjgl.opengl.Display;
import net.minecraftforge.common.ForgeEarlyConfig;
import net.minecraft.client.gui.GuiTextField;

/** Select the window API without linking SDL classes on older Cleanroom versions. */
public final class WindowCompat {
    interface Backend {
        boolean borderless();
        boolean visible();
        boolean adaptiveSupported();
        boolean enableAdaptive();
    }

    private WindowCompat() { }

    public static boolean isSdl() { return Holder.BACKEND instanceof SdlWindowBackend; }
    public static boolean isBorderless() { return Holder.BACKEND.borderless(); }
    public static boolean isVisible() { return Holder.BACKEND.visible(); }
    public static boolean supportsAdaptiveVsync() { return Holder.BACKEND.adaptiveSupported(); }
    public static boolean tryAdaptiveVsync() { return Holder.BACKEND.enableAdaptive(); }
    public static boolean composingText() { return isSdl() && TextHolder.INPUT.composing(); }
    public static int textScrollOffset(GuiTextField field) { return isSdl() ? TextHolder.INPUT.scroll(field) : 0; }
    public static void drawTextInput(GuiTextField field, int x, int y) {
        if (isSdl()) TextHolder.INPUT.draw(field, x, y);
    }

    private static final class TextHolder {
        private static final SdlTextInput INPUT = create();
        private static SdlTextInput create() {
            try {
                return new SdlTextInput(Class.forName("com.cleanroommc.client.sdl.SDLHooks", false,
                        WindowCompat.class.getClassLoader()), GuiTextField.class);
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("Unsupported Cleanroom SDL text input API", failure);
            }
        }
    }

    private static final class Holder {
        private static final Backend BACKEND = create();
        private static Backend create() {
            var loader = WindowCompat.class.getClassLoader();
            try {
                var sdl = Class.forName("com.cleanroommc.client.sdl.SDL", false, loader);
                var window = Class.forName("com.cleanroommc.client.sdl.Window", false, loader);
                var video = Class.forName("org.lwjgl.sdl.SDLVideo", false, loader);
                return new SdlWindowBackend(sdl, window, video,
                        () -> ForgeEarlyConfig.WINDOW_BORDERLESS_REPLACES_FULLSCREEN);
            } catch (ClassNotFoundException absent) {
                // 0.6.x has neither SDL nor its window class.
                if (loader.getResource("com/cleanroommc/client/sdl/SDL.class") != null) {
                    throw new IllegalStateException("Cleanroom SDL window dependencies are incomplete", absent);
                }
                return new GlfwBackend();
            } catch (ReflectiveOperationException incompatible) {
                throw new IllegalStateException("Unsupported Cleanroom SDL window API", incompatible);
            }
        }
    }

    /** Defined only when selected; GLFW is absent from Cleanroom 0.7's runtime. */
    private static final class GlfwBackend implements Backend {
        public boolean borderless() { return Display.isBorderless(); }
        public boolean visible() { return Display.isVisible(); }
        public boolean adaptiveSupported() {
            return org.lwjgl.glfw.GLFW.glfwExtensionSupported("GLX_EXT_swap_control_tear")
                    || org.lwjgl.glfw.GLFW.glfwExtensionSupported("WGL_EXT_swap_control_tear");
        }
        public boolean enableAdaptive() {
            if (!adaptiveSupported()) return false;
            org.lwjgl.glfw.GLFW.glfwSwapInterval(-1);
            return true;
        }
    }
}
