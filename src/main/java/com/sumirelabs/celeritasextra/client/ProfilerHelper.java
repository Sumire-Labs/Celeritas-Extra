package com.sumirelabs.celeritasextra.client;

import net.minecraft.profiler.Profiler;

/**
 * Shared profiler helper for entity and block entity render dispatchers.
 * Caches class names and manages profiler section push/pop.
 */
public final class ProfilerHelper {

    private static final ClassValue<String> NAME_CACHE = new ClassValue<>() {
        @Override protected String computeValue(Class<?> type) { return type.getSimpleName(); }
    };

    private ProfilerHelper() {
    }

    /**
     * Pushes a profiler section named after the renderer's simple class name.
     * <p>
     * The name is resolved once per renderer class and cached in a ClassValue;
     * no section is pushed when the profiler is disabled/null, the renderer is null, or its name
     * is empty. Each successful call must be balanced by {@link #endSection}.
     *
     * @param profiler the active world's profiler; ignored when disabled or null
     * @param renderer the renderer whose simple class name labels the section
     */
    public static boolean startSection(Profiler profiler, Object renderer) {
        if (profiler != null && profiler.profilingEnabled && renderer != null) {
            String name = NAME_CACHE.get(renderer.getClass());
            if (!name.isEmpty()) {
                profiler.startSection(name);
                return true;
            }
        }
        return false;
    }

    /**
     * Pops the profiler section previously pushed for this renderer.
     * <p>
     * The successful-start token avoids a second renderer lookup and also works in a finally block.
     */
    public static void endSection(Profiler profiler, boolean started) {
        if (started && profiler != null) profiler.endSection();
    }
}
