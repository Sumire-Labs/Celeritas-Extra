package com.sumirelabs.celeritasextra.compat;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.ByteArrayInputStream;
import java.net.URLClassLoader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PintoniumIntegrationTest {
    private static byte[] screenWithField(String name, String descriptor) {
        var writer = new ClassWriter(0);
        writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "org/taumc/celeritas/impl/gui/CeleritasVideoOptionsScreen",
                null, "net/minecraft/client/gui/GuiScreen", null);
        writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, name, descriptor, null, null).visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }

    @Test void selectsTheIndependentScreenForTheReportedMissingControllerAndChangedFieldTypes() throws Exception {
        // Pintonium owns a SearchTextFieldModel and has no CeleritasVideoOptionsController.
        assertFalse(CeleritasGuiCompatibility.hasControllerGui(new ByteArrayInputStream(screenWithField(
                "searchTextModel", "Lorg/taumc/celeritas/impl/gui/frame/components/SearchTextFieldModel;"))));
        assertFalse(CeleritasGuiCompatibility.hasControllerGui(new ByteArrayInputStream(screenWithField(
                "controller", "Lsome/fork/AnotherController;"))));
        for (String mixin : List.of("MixinOptionsScreen", "MixinOptionsController", "MixinGuiScreen",
                "MixinSliderScroll", "MixinScrollableFrame")) {
            assertFalse(RendererMixinPlugin.supportsMixin(false, false, false, "extra.mixin.options_search." + mixin));
        }
        assertTrue(RendererMixinPlugin.supportsMixin(false, false, false, "extra.mixin.render.sky.MixinCloudResources"));
        assertTrue(RendererMixinPlugin.supportsMixin(false, false, false, "extra.mixin.animation.MixinTextureAtlasSprite"));
    }

    @Test void preservesNativeCeleritasGuiIntegration() throws Exception {
        try (var input = getClass().getClassLoader().getResourceAsStream(CeleritasGuiCompatibility.SCREEN)) {
            assertNotNull(input, "The real Celeritas development API must be on the test classpath");
            assertTrue(CeleritasGuiCompatibility.hasControllerGui(input));
        }
        assertTrue(RendererMixinPlugin.supportsMixin(false, false, true, "extra.mixin.options_search.MixinOptionsScreen"));
    }

    @Test void fallbackAdapterAndProbeNeverDefinePrivateRendererGuiClasses() throws Exception {
        var classes = RendererMixinPlugin.class.getProtectionDomain().getCodeSource().getLocation();
        try (var isolated = new URLClassLoader(new java.net.URL[]{classes}, getClass().getClassLoader()) {
            @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("org.taumc.") || name.startsWith("org.embeddedt.") || name.startsWith("dhj.")) {
                    throw new ClassNotFoundException("Private renderer API unavailable: " + name);
                }
                if (name.startsWith("com.sumirelabs.celeritasextra.compat.pintonium.")
                        || name.equals("com.sumirelabs.celeritasextra.compat.CeleritasGuiCompatibility")) {
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
            var probe = isolated.loadClass("com.sumirelabs.celeritasextra.compat.CeleritasGuiCompatibility");
            assertEquals(Boolean.TRUE, probe.getMethod("hasControllerGui").invoke(null));
            var adapter = isolated.loadClass("com.sumirelabs.celeritasextra.compat.pintonium.PintoniumOptionsAdapter");
            assertTrue(adapter.getDeclaredMethods().length > 0);
        }
    }
}
