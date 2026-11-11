/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.compat;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Chooses renderer-specific hooks before Forge's mod construction phase. */
public final class RendererMixinPlugin implements IMixinConfigPlugin {

    private boolean nothirium;
    private boolean controllerGui;

    @Override
    public void onLoad(String mixinPackage) {
        // Query resources without defining optional renderer classes.
        this.nothirium = getClass().getClassLoader().getResource("meldexun/nothirium/mc/Nothirium.class") != null;
        this.controllerGui = !nothirium && CeleritasGuiCompatibility.hasControllerGui();
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return supportsMixin(this.nothirium, this.controllerGui, mixinClassName);
    }

    static boolean supportsMixin(boolean nothirium, boolean controllerGui, String name) {
        if (name.contains(".options_search."))
            return !nothirium && controllerGui;
        if (name.contains(".nothirium."))
            return nothirium;
        return true;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }
    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override
    public List<String> getMixins() {
        return null;
    }
    @Override
    public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
    @Override
    public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {}

}
