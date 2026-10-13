package com.sumirelabs.celeritasextra.compat.window;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/** Optional SDL IME hooks for text fields drawn by Extra rather than drawTextBox. */
final class SdlTextInput {
    private final MethodHandle draw, scroll, composing;

    SdlTextInput(Class<?> hooks, Class<?> fieldType) throws ReflectiveOperationException {
        var lookup = MethodHandles.publicLookup();
        draw = lookup.unreflect(hooks.getMethod("textFieldDraw", fieldType, int.class, int.class))
                .asType(MethodType.methodType(void.class, Object.class, int.class, int.class));
        scroll = lookup.unreflect(fieldType.getMethod("getLineScrollOffset"))
                .asType(MethodType.methodType(int.class, Object.class));
        composing = lookup.unreflect(hooks.getMethod("composing"))
                .asType(MethodType.methodType(boolean.class));
    }

    void draw(Object field, int x, int y) {
        try { draw.invokeExact(field, x, y); }
        catch (Throwable failure) { throw failed(failure); }
    }
    int scroll(Object field) {
        try { return (int) scroll.invokeExact(field); }
        catch (Throwable failure) { throw failed(failure); }
    }
    boolean composing() {
        try { return (boolean) composing.invokeExact(); }
        catch (Throwable failure) { throw failed(failure); }
    }
    private static IllegalStateException failed(Throwable failure) {
        if (failure instanceof Error error) throw error;
        return new IllegalStateException("Cleanroom SDL text input call failed", failure);
    }
}
