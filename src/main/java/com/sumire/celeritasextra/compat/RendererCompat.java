package com.sumire.celeritasextra.compat;

/** Optional renderer detection without defining its classes during startup. */
public final class RendererCompat {
    private static final boolean ACTINIUM = RendererCompat.class.getClassLoader()
            .getResource("com/dhj/actinium/Actinium.class") != null;

    private RendererCompat() { }

    public static boolean isActinium() { return ACTINIUM; }

    public static int menuFramerate(int fallback) {
        return ACTINIUM ? ActiniumAccess.menuFramerate() : fallback;
    }

    private static final class ActiniumAccess {
        private static int menuFramerate() {
            return com.dhj.actinium.runtime.ActiniumRuntime.options().performance.loadingScreenFramerateLimit;
        }
    }
}
