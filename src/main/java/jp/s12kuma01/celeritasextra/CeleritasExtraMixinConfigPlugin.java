package jp.s12kuma01.celeritasextra;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin configuration plugin that gates optional mixins by mod compatibility.
 * <p>
 * Referenced from the mixin config JSON, this plugin lets the mixin subsystem decide
 * which mixins to apply at load time. Mixins in a {@code .hei.} sub-package integrate
 * with the Just Enough Items ingredient overlay and are applied only when JEI/HEI is
 * present; all other mixins always apply. The remaining callbacks are unused no-ops
 * required by the {@link IMixinConfigPlugin} contract.
 */
public class CeleritasExtraMixinConfigPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    /**
     * Decides whether a candidate mixin should be applied to its target class.
     * <p>
     * Mixins whose class name contains {@code .hei.} depend on JEI/HEI and are applied
     * only when its ingredient-overlay class is on the classpath; every other mixin is
     * applied unconditionally.
     *
     * @param targetClassName fully-qualified name of the class being mixed into
     * @param mixinClassName  fully-qualified name of the candidate mixin
     * @return {@code true} if the mixin should be applied
     */
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains(".hei.")) {
            return isClassPresent("mezz.jei.gui.overlay.IngredientListOverlay");
        }
        return true;
    }

    /**
     * Tests whether a class is available on the classpath without initializing it.
     *
     * @param className fully-qualified class name to look up
     * @return {@code true} if the corresponding {@code .class} resource is found
     */
    private static boolean isClassPresent(String className) {
        return CeleritasExtraMixinConfigPlugin.class.getClassLoader()
                .getResource(className.replace('.', '/') + ".class") != null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
