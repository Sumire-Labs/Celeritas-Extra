package jp.s12kuma01.celeritasextra.mixin.profiler;

import net.minecraft.world.World;

import java.util.WeakHashMap;

/**
 * Shared profiler helper for entity and block entity render dispatchers.
 * Caches class names and manages profiler section push/pop.
 */
public final class ProfilerHelper {

    private static final WeakHashMap<Class<?>, String> NAME_CACHE = new WeakHashMap<>();

    private ProfilerHelper() {}

    public static void startSection(World world, Object renderer) {
        if (world != null && renderer != null) {
            String name = NAME_CACHE.computeIfAbsent(renderer.getClass(), Class::getSimpleName);
            if (!name.isEmpty()) {
                world.profiler.startSection(name);
            }
        }
    }

    public static void endSection(World world, Object renderer) {
        if (world != null && renderer != null) {
            String name = NAME_CACHE.get(renderer.getClass());
            if (name != null && !name.isEmpty()) {
                world.profiler.endSection();
            }
        }
    }
}
