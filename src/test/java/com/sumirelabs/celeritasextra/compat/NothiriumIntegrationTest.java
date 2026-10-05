package com.sumirelabs.celeritasextra.compat;

import org.junit.jupiter.api.Test;

import java.net.URLClassLoader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NothiriumIntegrationTest {
    @Test void versionRangeAcceptsTheCurrentBetaRelease() {
        String dependencies = com.sumirelabs.celeritasextra.CeleritasExtraMod.class
                .getAnnotation(net.minecraftforge.fml.common.Mod.class).dependencies();
        String rangeText = dependencies.substring(dependencies.indexOf("nothirium@") + "nothirium@".length()).split(";")[0];
        var range = net.minecraftforge.fml.common.versioning.VersionParser.parseRange(rangeText);
        assertTrue(range.containsVersion(new net.minecraftforge.fml.common.versioning.DefaultArtifactVersion("0.4.9-beta")));
        assertFalse(range.containsVersion(new net.minecraftforge.fml.common.versioning.DefaultArtifactVersion("0.4.8-beta")));
    }

    @Test void nothiriumSelectsCommonHooksAndRenderLibWithoutSodiumGuiTargets() {
        assertFalse(RendererMixinPlugin.supportsMixin(false, true, "extra.mixin.options_search.MixinOptionsScreen"));
        assertFalse(RendererMixinPlugin.supportsMixin(false, true, "extra.mixin.actinium.MixinFastLitItemLod"));
        assertTrue(RendererMixinPlugin.supportsMixin(false, true, "extra.mixin.nothirium.MixinRenderLibEntityDistance"));
        assertFalse(RendererMixinPlugin.supportsMixin(false, false, "extra.mixin.nothirium.MixinRenderLibEntityDistance"));
        assertFalse(RendererMixinPlugin.supportsMixin(true, false, "extra.mixin.nothirium.MixinRenderLibEntityDistance"));
        assertTrue(RendererMixinPlugin.supportsMixin(false, true, "extra.mixin.render.sky.MixinRenderGlobalClouds"));
        assertTrue(RendererMixinPlugin.supportsMixin(false, true, "extra.mixin.render.block_entity.MixinTileEntityRenderDistance"));
    }

    @Test void forgeAdapterLinksWithoutEitherRendererApiOrNothiriumApi() throws Exception {
        var classes = RendererMixinPlugin.class.getProtectionDomain().getCodeSource().getLocation();
        try (var isolated = new URLClassLoader(new java.net.URL[]{classes}, getClass().getClassLoader()) {
            @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("org.taumc.") || name.startsWith("org.embeddedt.")
                        || name.startsWith("dhj.") || name.startsWith("com.dhj.") || name.startsWith("meldexun.")) {
                    throw new ClassNotFoundException("Optional renderer API unavailable: " + name);
                }
                if (name.startsWith("com.sumirelabs.celeritasextra.compat.nothirium.")) {
                    synchronized (getClassLoadingLock(name)) {
                        Class<?> type = findLoadedClass(name);
                        if (type == null) type = findClass(name);
                        if (resolve) resolveClass(type);
                        return type;
                    }
                }
                return super.loadClass(name, resolve);
            }
        }) {
            for (String name : List.of("NothiriumOptionsAdapter", "VanillaExtraOptionsScreen", "VanillaExtraTextScreen", "ForgeExtraConfigElements")) {
                Class<?> type = isolated.loadClass("com.sumirelabs.celeritasextra.compat.nothirium." + name);
                assertTrue(type.getDeclaredMethods().length > 0);
                assertTrue(type.getDeclaredConstructors().length > 0);
            }
        }
    }
}
