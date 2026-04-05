package jp.s12kuma01.celeritasextra.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
     * Scan all mod jar files using ASM to discover Particle subclasses
     * without loading or instantiating them. Uses bytecode superclass analysis
     * to find classes that extend Particle or any known Particle subclass.
     * Also records which mod each class belongs to.
     */
    public void scanModJars() {
        // Build initial set of known particle class names (internal form: / separator)
        Set<String> knownParticleNames = new HashSet<>();
        knownParticleNames.add(Particle.class.getName().replace('.', '/'));
        for (String className : discoveredClasses.keySet()) {
            knownParticleNames.add(className.replace('.', '/'));
        }

        // Collect superclass map and class-to-mod mapping from mod sources using ASM
        Map<String, String> superMap = new HashMap<>();
        Map<String, String> classToModName = new HashMap<>();
        Set<File> scannedSources = new HashSet<>();

        for (ModContainer mod : Loader.instance().getModList()) {
            File source = mod.getSource();
            if (source == null || !scannedSources.add(source)) continue;

            String modName = mod.getModId();

            if (source.isFile() && source.getName().endsWith(".jar")) {
                scanJarForSuperclasses(source, superMap, classToModName, modName);
            } else if (source.isDirectory()) {
                scanDirectoryForSuperclasses(source, superMap, classToModName, modName);
            }
        }

        // Assign mod names to already-discovered classes (from config or runtime recording)
        for (String fullName : discoveredClasses.keySet()) {
            String internalName = fullName.replace('.', '/');
            String modName = classToModName.get(internalName);
            if (modName != null) {
                recordModName(fullName, modName);
            }
        }

        // Iteratively resolve: find classes whose superclass is a known particle class
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Map.Entry<String, String> entry : superMap.entrySet()) {
                String className = entry.getKey();
                String superName = entry.getValue();
                if (knownParticleNames.contains(className)) continue;
                if (superName != null && knownParticleNames.contains(superName)) {
                    String fullName = className.replace('/', '.');
                    String simpleName = toSimpleName(fullName);
                    recordClass(fullName, simpleName);
                    String modName = classToModName.get(className);
                    if (modName != null) {
                        recordModName(fullName, modName);
                    }
                    knownParticleNames.add(className);
                    changed = true;
                }
            }
        }
    }

    private void scanJarForSuperclasses(File jarFile, Map<String, String> superMap,
                                        Map<String, String> classToModName, String modName) {
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;
                try (InputStream is = jar.getInputStream(entry)) {
                    ClassReader cr = new ClassReader(is);
                    String className = cr.getClassName();
                    superMap.put(className, cr.getSuperName());
                    classToModName.put(className, modName);
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private void scanDirectoryForSuperclasses(File dir, Map<String, String> superMap,
                                              Map<String, String> classToModName, String modName) {
        Path root = dir.toPath();
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().endsWith(".class")) {
                        try (InputStream is = Files.newInputStream(file)) {
                            ClassReader cr = new ClassReader(is);
                            String className = cr.getClassName();
                            superMap.put(className, cr.getSuperName());
                            classToModName.put(className, modName);
                        } catch (Throwable ignored) {
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignored) {
        }
    }

    private void rebuildSnapshot() {
        disabledClassesSnapshot = new HashSet<>(disabledClasses);
    }
}
