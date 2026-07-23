package jp.s12kuma01.celeritasextra.client.particle;

import jp.s12kuma01.celeritasextra.mixin.particle.IMixinParticleManager;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime registry for discovered particle classes.
 * <p>
 * Discovery has three sources, in order of authority:
 * <ol>
 *     <li>{@link #scanFactories(ParticleManager)} — reflection over the registered particle
 *         factories, run authoritatively at world load (see {@code ParticleDiscoveryHandler}).
 *         Resolves vanilla and statically-shaped mod factories.</li>
 *     <li>{@link #recordClass(Class)} — runtime spawn recording from the particle mixin.
 *         The only reliable path for mod particles registered as lambdas/anonymous classes,
 *         whose concrete {@link Particle} subclass cannot be resolved statically.</li>
 *     <li>{@link #registerFactoryMod(IParticleFactory, String)} — owning-mod captured at
 *         {@code registerParticle} time, used to attribute factory-resolved classes to a mod.</li>
 * </ol>
 * Discovered classes are persisted as a refreshable cache: reloaded on launch (so previously
 * seen classes appear immediately) and reconciled against currently loadable classes via
 * {@link #pruneDiscoveredCache()}. Only the user's disabled set is authoritative user data.
 */
public class ParticleClassRegistry {

    private static final ParticleClassRegistry INSTANCE = new ParticleClassRegistry();

    /** fullClassName -> display simple name (never empty). */
    private final ConcurrentHashMap<String, String> discoveredClasses = new ConcurrentHashMap<>();
    /** fullClassName -> owning mod id. */
    private final ConcurrentHashMap<String, String> classModNames = new ConcurrentHashMap<>();
    /** factory instance -> owning mod id, captured at registerParticle time. */
    private final ConcurrentHashMap<IParticleFactory, String> factoryModIds = new ConcurrentHashMap<>();
    /** User-disabled classes. This is the only authoritative, user-owned persisted state. */
    private final ConcurrentHashMap.KeySetView<String, Boolean> disabledClasses = ConcurrentHashMap.newKeySet();
    /** Per-session identity guard so the hot path does expensive work at most once per class. */
    private final Set<Class<?>> seenClasses = ConcurrentHashMap.newKeySet();

    /** Set whenever persisted state (discovered cache or disabled set) changes; cleared on save. */
    private volatile boolean dirty = false;
    /** Lazily-built mapping of mod source jar/dir -> mod id, for code-source attribution. */
    private volatile Map<File, String> sourceToModId;

    private ParticleClassRegistry() {
    }

    /** Returns the process-wide singleton registry. */
    public static ParticleClassRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Derive a non-empty simple name from a fully-qualified (possibly inner/anonymous) class name.
     * e.g. {@code com.foo.Bar$Baz -> Baz}, {@code com.foo.Bar$1 -> 1}.
     */
    private static String toSimpleName(String fullName) {
        String name = fullName.substring(fullName.lastIndexOf('.') + 1);
        int dollar = name.lastIndexOf('$');
        if (dollar >= 0) {
            name = name.substring(dollar + 1);
        }
        return name;
    }

    /**
     * Resolves a robust, non-empty simple name for a class. Tolerates reflection failures
     * and empty synthetic names by falling back to {@link #toSimpleName(String)} and finally
     * to the fully-qualified name, so the result is always usable as a display label.
     */
    private static String simpleNameOf(Class<?> clazz) {
        String s;
        try {
            s = clazz.getSimpleName();
        } catch (Throwable t) {
            s = "";
        }
        if (s == null || s.isEmpty()) {
            s = toSimpleName(clazz.getName());
        }
        if (s.isEmpty()) {
            s = clazz.getName();
        }
        return s;
    }

    // ------------------------------------------------------------------
    // Discovery
    // ------------------------------------------------------------------

    /** Record a discovered particle class (spawn-time path, no factory context). */
    public void recordClass(Class<?> clazz) {
        recordClass(clazz, null);
    }

    /**
     * Record a discovered particle class. Identity-guarded: the expensive name/attribution work
     * runs at most once per class per session, so this is cheap to call on the per-particle path.
     */
    public void recordClass(Class<?> clazz, IParticleFactory factory) {
        if (clazz == null || !seenClasses.add(clazz)) {
            return;
        }
        String fullName = clazz.getName();
        if (discoveredClasses.putIfAbsent(fullName, simpleNameOf(clazz)) == null) {
            dirty = true;
        }
        String modId = resolveModId(clazz, factory);
        if (modId != null) {
            classModNames.putIfAbsent(fullName, modId);
        }
    }

    /** Capture the owning mod of a factory as it is registered (primary attribution signal). */
    public void registerFactoryMod(IParticleFactory factory, String modId) {
        if (factory != null && modId != null) {
            factoryModIds.put(factory, modId);
        }
    }

    /**
     * Scan the ParticleManager's registered factories to discover particle classes.
     * Two strategies:
     * <ol>
     *     <li>Enclosing class — for inner-class factories (e.g. {@code ParticleFlame.Factory}).</li>
     *     <li>Covariant return type — for factories declaring a specific Particle subclass return.</li>
     * </ol>
     * Lambda/anonymous factories match neither and are left to spawn-time {@link #recordClass}.
     */
    public void scanFactories(ParticleManager particleManager) {
        if (particleManager == null) {
            return;
        }
        Map<Integer, IParticleFactory> factories;
        try {
            factories = ((IMixinParticleManager) particleManager).getParticleTypes();
        } catch (Throwable t) {
            return;
        }
        if (factories == null) {
            return;
        }

        for (var entry : factories.entrySet()) {
            IParticleFactory factory = entry.getValue();
            if (factory == null) {
                continue;
            }
            Class<?> factoryClass = factory.getClass();
            try {
                // Strategy 1: enclosing class (inner-class factories like ParticleFlame.Factory)
                Class<?> enclosingClass = factoryClass.getEnclosingClass();
                if (enclosingClass != null && Particle.class.isAssignableFrom(enclosingClass)) {
                    recordClass(enclosingClass, factory);
                    continue;
                }

                // Strategy 2: covariant return type analysis
                for (Method method : factoryClass.getDeclaredMethods()) {
                    Class<?> returnType = method.getReturnType();
                    if (returnType != Particle.class && Particle.class.isAssignableFrom(returnType)) {
                        recordClass(returnType, factory);
                        break;
                    }
                }
            } catch (Throwable t) {
                // Skip a factory that fails reflection rather than aborting the whole scan.
            }
        }
    }

    /**
     * Reconcile the persisted discovered cache against currently loadable classes:
     * drop entries whose class no longer exists (mod removed), keep broken-but-present ones,
     * and back-fill mod attribution for cached classes now that the classloader is ready.
     */
    public void pruneDiscoveredCache() {
        ClassLoader cl = ParticleClassRegistry.class.getClassLoader();
        for (String name : new ArrayList<>(discoveredClasses.keySet())) {
            Class<?> clazz;
            try {
                clazz = Class.forName(name, false, cl);
            } catch (ClassNotFoundException e) {
                // Owning mod is gone — drop the stale toggle.
                discoveredClasses.remove(name);
                classModNames.remove(name);
                dirty = true;
                continue;
            } catch (Throwable t) {
                // Present but not linkable right now — keep it, don't prune valid entries.
                continue;
            }
            if (!classModNames.containsKey(name)) {
                String modId = resolveModId(clazz, null);
                if (modId != null) {
                    classModNames.put(name, modId);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Mod attribution
    // ------------------------------------------------------------------

    /**
     * Determines the owning mod id for a class using, in order of authority: the vanilla
     * {@code net.minecraft.*} namespace, the mod captured for the supplying factory (see
     * {@link #registerFactoryMod}), then the class's code-source jar or directory.
     *
     * @param clazz   the particle class to attribute
     * @param factory the factory that produced it, or {@code null} at the spawn-time path
     * @return the resolved mod id, or {@code null} if none could be attributed
     */
    private String resolveModId(Class<?> clazz, IParticleFactory factory) {
        String name = clazz.getName();
        if (name.startsWith("net.minecraft.")) {
            return "minecraft";
        }
        if (factory != null) {
            String captured = factoryModIds.get(factory);
            if (captured != null) {
                return captured;
            }
        }
        return modIdFromCodeSource(clazz);
    }

    private String modIdFromCodeSource(Class<?> clazz) {
        try {
            CodeSource cs = clazz.getProtectionDomain().getCodeSource();
            if (cs == null) {
                return null;
            }
            URL loc = cs.getLocation();
            if (loc == null) {
                return null;
            }
            File f;
            try {
                f = new File(loc.toURI());
            } catch (Exception e) {
                f = new File(loc.getPath());
            }
            return sourceMap().get(canonical(f));
        } catch (Throwable t) {
            return null;
        }
    }

    private Map<File, String> sourceMap() {
        Map<File, String> map = sourceToModId;
        if (map == null) {
            map = new HashMap<>();
            try {
                for (ModContainer container : Loader.instance().getActiveModList()) {
                    File src = container.getSource();
                    if (src != null) {
                        map.put(canonical(src), container.getModId());
                    }
                }
            } catch (Throwable t) {
                // Best-effort: leave whatever was collected.
            }
            sourceToModId = map;
        }
        return map;
    }

    private static File canonical(File f) {
        try {
            return f.getCanonicalFile();
        } catch (IOException e) {
            return f.getAbsoluteFile();
        }
    }

    /**
     * Resolves the owning mod id for a discovered class. Falls back to {@code "minecraft"}
     * for {@code net.minecraft.*} classes when no attribution was recorded.
     *
     * @param fullClassName fully-qualified particle class name
     * @return the owning mod id, {@code "minecraft"} for vanilla classes, or {@code null} if unknown
     */
    public String getModName(String fullClassName) {
        String modName = classModNames.get(fullClassName);
        if (modName != null) {
            return modName;
        }
        if (fullClassName.startsWith("net.minecraft.")) {
            return "minecraft";
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Disabled set (authoritative user data)
    // ------------------------------------------------------------------

    /** Returns whether the user has disabled the given fully-qualified class. */
    public boolean isClassDisabled(String fullClassName) {
        return disabledClasses.contains(fullClassName);
    }

    /** Returns whether the disabled set is empty, i.e. no particle filtering is active. */
    public boolean isEmptyDisabled() {
        return disabledClasses.isEmpty();
    }

    /**
     * Enables or disables a particle class for the user. Enabling removes it from the disabled
     * set; disabling adds it. Marks the registry dirty only when the set actually changes.
     *
     * @param fullClassName fully-qualified particle class name
     * @param enabled       {@code true} to render the particle, {@code false} to suppress it
     */
    public void setClassEnabled(String fullClassName, boolean enabled) {
        boolean changed = enabled
                ? disabledClasses.remove(fullClassName)
                : disabledClasses.add(fullClassName);
        if (changed) {
            dirty = true;
        }
    }

    /**
     * Replaces the disabled set with the given classes loaded from config. Null and empty
     * entries are skipped.
     *
     * @param classes fully-qualified class names to mark disabled
     */
    public void loadDisabledClasses(String[] classes) {
        disabledClasses.clear();
        for (String cls : classes) {
            if (cls != null && !cls.isEmpty()) {
                disabledClasses.add(cls);
            }
        }
    }

    /**
     * Exports the disabled set for config persistence.
     *
     * @return the fully-qualified names of all user-disabled classes
     */
    public String[] getDisabledClassesArray() {
        return disabledClasses.toArray(new String[0]);
    }

    // ------------------------------------------------------------------
    // Discovered cache persistence
    // ------------------------------------------------------------------

    /**
     * Returns a read-only view of discovered classes keyed by fully-qualified name.
     *
     * @return an unmodifiable {@code fullClassName -> display simpleName} map
     */
    public Map<String, String> getDiscoveredClasses() {
        return Collections.unmodifiableMap(discoveredClasses);
    }

    /** Returns whether persisted state has changed since the last {@link #markClean()}. */
    public boolean isDirty() {
        return dirty;
    }

    /** Clears the dirty flag once the registry state has been persisted. */
    public void markClean() {
        dirty = false;
    }

    /**
     * Load the cached discovered classes from config ("fullName|simpleName" entries), so they
     * appear in the settings UI before they spawn (or are scanned) this session.
     */
    public void loadDiscoveredClasses(String[] entries) {
        for (String entry : entries) {
            if (entry == null || entry.isEmpty()) {
                continue;
            }
            int sep = entry.indexOf('|');
            String fullName;
            String simpleName;
            if (sep > 0) {
                fullName = entry.substring(0, sep);
                simpleName = entry.substring(sep + 1);
            } else {
                fullName = entry;
                simpleName = "";
            }
            if (fullName.isEmpty()) {
                continue;
            }
            if (simpleName.isEmpty()) {
                simpleName = toSimpleName(fullName);
            }
            if (simpleName.isEmpty()) {
                simpleName = fullName;
            }
            discoveredClasses.putIfAbsent(fullName, simpleName);
        }
    }

    /** Export discovered classes as "fullName|simpleName" strings for config persistence. */
    public String[] getDiscoveredClassesArray() {
        return discoveredClasses.entrySet().stream()
                .map(e -> e.getKey() + "|" + e.getValue())
                .toArray(String[]::new);
    }
}
