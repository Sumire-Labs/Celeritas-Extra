package jp.s12kuma01.celeritasextra.compat;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/** Chooses renderer-specific hooks before Forge's mod construction phase. */
public final class RendererMixinPlugin implements IMixinConfigPlugin {
    private boolean actinium;

    @Override
    public void onLoad(String mixinPackage) {
        // Query resources without defining optional renderer classes.
        this.actinium = getClass().getClassLoader().getResource("com/dhj/actinium/Actinium.class") != null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return supportsMixin(this.actinium, mixinClassName);
    }

    static boolean supportsMixin(boolean actinium, String name) {
        if (name.contains(".options_search.")) return !actinium;
        if (name.endsWith(".render.sky.MixinRenderGlobalClouds")) return !actinium;
        if (name.contains(".actinium.")) return actinium;
        return true;
    }

    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) { }
    @Override public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) { }
}
