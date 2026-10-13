package com.sumirelabs.celeritasextra.compat.window;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.function.BooleanSupplier;

/** Cached optional API handles: window queries never call GLFW or LWJGLY's visibility stub. */
final class SdlWindowBackend implements WindowCompat.Backend {
    private final MethodHandle window, fullscreen, minimized, hidden, getSwapInterval, setSwapInterval;
    private final BooleanSupplier borderlessPreference;

    SdlWindowBackend(Class<?> sdl, Class<?> windowType, Class<?> video, BooleanSupplier borderlessPreference) throws ReflectiveOperationException {
        this.borderlessPreference = borderlessPreference;
        var lookup = MethodHandles.publicLookup();
        window = lookup.unreflect(sdl.getMethod("window")).asType(MethodType.methodType(Object.class));
        fullscreen = query(lookup, windowType, "fullscreen");
        minimized = query(lookup, windowType, "minimized");
        hidden = query(lookup, windowType, "hidden");
        getSwapInterval = lookup.unreflect(video.getMethod("SDL_GL_GetSwapInterval", IntBuffer.class))
                .asType(MethodType.methodType(boolean.class, IntBuffer.class));
        setSwapInterval = lookup.unreflect(video.getMethod("SDL_GL_SetSwapInterval", int.class))
                .asType(MethodType.methodType(boolean.class, int.class));
    }

    private static MethodHandle query(MethodHandles.Lookup lookup, Class<?> type, String name) throws ReflectiveOperationException {
        return lookup.unreflect(type.getMethod(name)).asType(MethodType.methodType(boolean.class, Object.class));
    }

    public boolean borderless() {
        try {
            Object current = (Object) window.invokeExact();
            // SDLHooks uses coverDisplay on Windows but fullscreenMode(null)
            // elsewhere. The latter is desktop fullscreen without necessarily
            // setting SDL_WINDOW_BORDERLESS, so decoration flags are insufficient.
            return current != null && (boolean) fullscreen.invokeExact(current) && borderlessPreference.getAsBoolean();
        } catch (Throwable failure) { throw failed(failure); }
    }

    public boolean visible() {
        try {
            Object current = (Object) window.invokeExact();
            return current != null && !(boolean) minimized.invokeExact(current) && !(boolean) hidden.invokeExact(current);
        } catch (Throwable failure) { throw failed(failure); }
    }

    public boolean adaptiveSupported() {
        // SDL has no extension-name query. Probe -1 and restore the exact prior
        // interval so merely opening the options screen cannot change VSync.
        IntBuffer previous = ByteBuffer.allocateDirect(Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
        try {
            if (!(boolean) getSwapInterval.invokeExact(previous)) return false;
            if (!(boolean) setSwapInterval.invokeExact(-1)) return false;
            if (!(boolean) setSwapInterval.invokeExact(previous.get(0))) {
                throw new IllegalStateException("SDL could not restore the swap interval after probing adaptive VSync");
            }
            return true;
        } catch (Throwable failure) { throw failed(failure); }
    }

    public boolean enableAdaptive() {
        try { return (boolean) setSwapInterval.invokeExact(-1); }
        catch (Throwable failure) { throw failed(failure); }
    }

    private static IllegalStateException failed(Throwable failure) {
        if (failure instanceof Error error) throw error;
        return failure instanceof IllegalStateException state ? state
                : new IllegalStateException("Cleanroom SDL window call failed", failure);
    }
}
