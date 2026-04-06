package jp.s12kuma01.celeritasextra.client.particle;

import jp.s12kuma01.celeritasextra.mixin.particle.IMixinParticleManager;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime registry for discovered particle classes.
 * Records particle class names as they are encountered and manages a disabled set for filtering.
 */
public class ParticleClassRegistry {

    private static final ParticleClassRegistry INSTANCE = new ParticleClassRegistry();

    private final ConcurrentHashMap<String, String> discoveredClasses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> classModNames = new ConcurrentHashMap<>();
    private final ConcurrentHashMap.KeySetView<String, Boolean> disabledClasses = ConcurrentHashMap.newKeySet();
    private volatile HashSet<String> disabledClassesSnapshot = new HashSet<>();

    private ParticleClassRegistry() {
    }

    public static ParticleClassRegistry getInstance() {
        return INSTANCE;
    }

    private static String toSimpleName(String fullName) {
        String name = fullName.substring(fullName.lastIndexOf('.') + 1);
        int dollar = name.lastIndexOf('$');
        if (dollar >= 0) {
            name = name.substring(dollar + 1);
        }
        return name;
    }

    public void recordClass(String fullName, String simpleName) {
        discoveredClasses.putIfAbsent(fullName, simpleName);
    }

    public void recordModName(String fullClassName, String modName) {
        if (modName != null) {
            classModNames.putIfAbsent(fullClassName, modName);
        }
    }

    public String getModName(String fullClassName) {
        String modName = classModNames.get(fullClassName);
        if (modName != null) return modName;
        // Fallback: derive from package
        if (fullClassName.startsWith("net.minecraft.")) return "minecraft";
        return null;
    }

    public boolean isClassDisabled(String fullClassName) {
        return disabledClassesSnapshot.contains(fullClassName);
    }

    public void setClassEnabled(String fullClassName, boolean enabled) {
        if (enabled) {
            disabledClasses.remove(fullClassName);
        } else {
            disabledClasses.add(fullClassName);
        }
        rebuildSnapshot();
    }

    public void loadDisabledClasses(String[] classes) {
        disabledClasses.clear();
        for (String cls : classes) {
            if (cls != null && !cls.isEmpty()) {
                disabledClasses.add(cls);
            }
        }
        rebuildSnapshot();
    }

    public String[] getDisabledClassesArray() {
        return disabledClasses.toArray(new String[0]);
    }

    public Map<String, String> getDiscoveredClasses() {
        return Collections.unmodifiableMap(discoveredClasses);
    }

    /**
     * Load previously discovered class names from config, so they appear
     * in the settings UI even before they spawn in the current session.
     */
    public void loadDiscoveredClasses(String[] entries) {
        for (String entry : entries) {
            if (entry == null || entry.isEmpty()) continue;
            int sep = entry.indexOf('|');
            if (sep > 0 && sep < entry.length() - 1) {
                String fullName = entry.substring(0, sep);
                String simpleName = entry.substring(sep + 1);
                discoveredClasses.putIfAbsent(fullName, simpleName);
            }
        }
    }

    /**
     * Export all discovered classes as "fullName|simpleName" strings for config persistence.
     */
    public String[] getDiscoveredClassesArray() {
        return discoveredClasses.entrySet().stream()
                .map(e -> e.getKey() + "|" + e.getValue())
                .toArray(String[]::new);
    }

    /**
     * Scan ParticleManager's registered factories to discover particle classes.
     * Uses two strategies:
     * 1. Enclosing class - for inner class factories (e.g. ParticleFlame.Factory)
     * 2. Covariant return type - for factories that declare a specific Particle subclass return type
     */
    public void scanFactories(ParticleManager particleManager) {
        Map<Integer, IParticleFactory> factories = ((IMixinParticleManager) particleManager).getParticleTypes();

        for (var entry : factories.entrySet()) {
            IParticleFactory factory = entry.getValue();
            Class<?> factoryClass = factory.getClass();

            // Strategy 1: Enclosing class (inner class factories like ParticleFlame.Factory)
            Class<?> enclosingClass = factoryClass.getEnclosingClass();
            if (enclosingClass != null && Particle.class.isAssignableFrom(enclosingClass)) {
                recordClass(enclosingClass.getName(), enclosingClass.getSimpleName());
                continue;
            }

            // Strategy 2: Covariant return type analysis
            try {
                for (Method method : factoryClass.getDeclaredMethods()) {
                    Class<?> returnType = method.getReturnType();
                    if (returnType != Particle.class && Particle.class.isAssignableFrom(returnType)) {
                        recordClass(returnType.getName(), returnType.getSimpleName());
                        break;
                    }
                }
            } catch (Throwable _) {
            }
        }
    }

    private void rebuildSnapshot() {
        disabledClassesSnapshot = new HashSet<>(disabledClasses);
    }
}
