package jp.s12kuma01.celeritasextra.client;

import net.minecraft.world.World;

import java.util.WeakHashMap;

/**
 * Shared profiler helper for entity and block entity render dispatchers.
 * Caches class names and manages profiler section push/pop.
 */
public final class ProfilerHelper {

    private static final WeakHashMap<Class<?>, String> NAME_CACHE = new WeakHashMap<>();

    private ProfilerHelper() {
    }

    /**
     * Pushes a profiler section named after the renderer's simple class name.
     * <p>
     * The name is resolved once per renderer class and cached in a {@link WeakHashMap};
     * no section is pushed when the world or renderer is null, or when the resolved name
     * is empty. Each successful call must be balanced by {@link #endSection}.
     *
     * @param world    the world whose profiler receives the section; ignored when null
     * @param renderer the renderer whose simple class name labels the section
     */
    public static void startSection(World world, Object renderer) {
        if (world != null && renderer != null) {
            String name = NAME_CACHE.computeIfAbsent(renderer.getClass(), Class::getSimpleName);
            if (!name.isEmpty()) {
                world.profiler.startSection(name);
            }
        }
    }

    /**
     * Pops the profiler section previously pushed for this renderer.
     * <p>
     * Pops only when a cached name exists for the renderer's class (i.e. a matching
     * {@link #startSection} pushed one), keeping the profiler's push/pop calls balanced.
     *
     * @param world    the world whose profiler is popped; ignored when null
     * @param renderer the renderer whose {@link #startSection} call is being closed
     */
    public static void endSection(World world, Object renderer) {
        if (world != null && renderer != null) {
            String name = NAME_CACHE.get(renderer.getClass());
            if (name != null && !name.isEmpty()) {
                world.profiler.endSection();
            }
        }
    }
}
