package jp.s12kuma01.celeritasextra.compat;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.net.URLClassLoader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActiniumIntegrationTest {
    @Test void nativePagesSupportPendingChangesAndResetDefaults() throws Exception {
        var configField = jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod.class.getDeclaredField("CONFIG");
        configField.setAccessible(true);
        Object previousConfig = configField.get(null);
        var localeField = java.util.Arrays.stream(net.minecraft.client.resources.I18n.class.getDeclaredFields())
                .filter(f -> f.getType() == net.minecraft.client.resources.Locale.class).findFirst().orElseThrow();
        localeField.setAccessible(true);
        Object previousLocale = localeField.get(null);
        var config = new jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraGameOptions();
        config.animationSettings.animation = false;
        try {
            configField.set(null, config);
            localeField.set(null, new net.minecraft.client.resources.Locale());
            for (String factory : List.of("animation", "sky", "clouds", "fog", "entities", "blocks", "overlay", "misc")) {
                var pages = jp.s12kuma01.celeritasextra.compat.actinium.gui.CeleritasExtraGameOptionPages.class;
                var page = (dhj.embeddedt.embeddium.api.options.structure.OptionPage) pages.getMethod(factory).invoke(null);
                assertEquals("celeritasextra", page.getId().getModId());
                assertFalse(page.getOptions().isEmpty());
                assertEquals(page.getOptions().size(), page.getOptions().stream().map(o -> o.getId()).distinct().count());
                for (var option : page.getOptions()) {
                    assertNotNull(option.getDefaultValue());
                    option.resetToDefault();
                }
                if (factory.equals("animation")) {
                    var animation = page.getOptions().getFirst();
                    assertEquals(Boolean.TRUE, animation.getValue());
                    assertTrue(animation.hasChanged());
                    animation.applyChanges();
                    assertTrue(config.animationSettings.animation);
                }
            }
        } finally {
            configField.set(null, previousConfig);
            localeField.set(null, previousLocale);
        }
    }

    @Test void rendererHooksAreSelectedWithoutLoadingTheOtherGui() {
        assertFalse(RendererMixinPlugin.supportsMixin(true, "extra.mixin.options_search.MixinOptionsScreen"));
        assertTrue(RendererMixinPlugin.supportsMixin(false, "extra.mixin.options_search.MixinOptionsScreen"));
        assertTrue(RendererMixinPlugin.supportsMixin(true, "extra.mixin.actinium.MixinFastLitItemLod"));
        assertFalse(RendererMixinPlugin.supportsMixin(false, "extra.mixin.actinium.MixinFastLitItemLod"));
        assertTrue(RendererMixinPlugin.supportsMixin(true, "extra.mixin.render.entity.MixinRenderItemFrame"));
    }

    @Test void actiniumAdapterLinksWithCeleritasApiUnavailable() throws Exception {
        var classes = ActiniumIntegrationTest.class.getProtectionDomain().getCodeSource().getLocation();
        var mainClasses = RendererMixinPlugin.class.getProtectionDomain().getCodeSource().getLocation();
        try (var isolated = new URLClassLoader(new java.net.URL[]{mainClasses, classes}, getClass().getClassLoader()) {
            @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("org.taumc.celeritas.") || name.startsWith("org.embeddedt.embeddium.")) {
                    throw new ClassNotFoundException("Celeritas is not installed: " + name);
                }
                if (name.startsWith("jp.s12kuma01.celeritasextra.compat.actinium.")) {
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
            for (String name : List.of("CeleritasExtraGameOptionPages", "CeleritasExtraOptionPages",
                    "CeleritasExtraOptionsStorage", "CeleritasExtraOptionsListener")) {
                Class<?> type = isolated.loadClass("jp.s12kuma01.celeritasextra.compat.actinium.gui." + name);
                assertTrue(type.getDeclaredMethods().length > 0 || type.getDeclaredFields().length > 0);
            }
            var adapter = isolated.loadClass("jp.s12kuma01.celeritasextra.compat.actinium.ActiniumOptionsAdapter");
            // Exercise registration against the release's real event buses.
            adapter.getMethod("register").invoke(null);
        }
    }

    @Test void fastItemHookTargetsAMethodInTheActualRelease() throws Exception {
        var mixin = jp.s12kuma01.celeritasextra.mixin.actinium.MixinFastLitItemLod.class;
        var node = new ClassNode();
        try (var stream = mixin.getResourceAsStream("MixinFastLitItemLod.class")) {
            new ClassReader(stream).accept(node, ClassReader.SKIP_CODE);
        }
        // @Mixin is retained in bytecode rather than Java reflection metadata.
        var annotation = node.invisibleAnnotations.stream()
                .filter(a -> a.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")).findFirst().orElseThrow();
        @SuppressWarnings("unchecked")
        var targets = (List<String>) annotation.values.get(annotation.values.indexOf("targets") + 1);
        for (String target : targets) {
            var type = Class.forName(target, false, getClass().getClassLoader());
            assertEquals(boolean.class, type.getDeclaredMethod("useFastLitItemRendering").getReturnType());
        }
    }
}
