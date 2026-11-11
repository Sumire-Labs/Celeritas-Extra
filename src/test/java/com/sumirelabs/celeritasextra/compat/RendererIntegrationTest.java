/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.compat;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import static org.junit.jupiter.api.Assertions.*;

class RendererIntegrationTest {

    @Test
    void backgroundLimiterWrapsNativeEarlyReturns() throws Exception {
        var type = com.sumirelabs.celeritasextra.mixin.background_fps.MixinMinecraft.class;
        var node = new ClassNode();
        try (var input = type.getResourceAsStream("MixinMinecraft.class")) {
            new ClassReader(input).accept(node, ClassReader.SKIP_CODE);
        }
        var method = node.methods.stream().filter(m -> m.name.equals("celeritasExtra$backgroundLimit")).findFirst()
            .orElseThrow();
        var annotations = new java.util.ArrayList<org.objectweb.asm.tree.AnnotationNode>();
        if (method.visibleAnnotations != null)
            annotations.addAll(method.visibleAnnotations);
        if (method.invisibleAnnotations != null)
            annotations.addAll(method.invisibleAnnotations);
        assertTrue(annotations.stream()
            .anyMatch(a -> a.desc.equals("Lcom/llamalad7/mixinextras/injector/wrapmethod/WrapMethod;")));
    }

    @Test
    void celeritasSelectsNativeGuiAndCommonRenderingHooks() {
        assertTrue(RendererMixinPlugin.supportsMixin(false, true, "extra.mixin.options_search.MixinOptionsScreen"));
        assertTrue(RendererMixinPlugin.supportsMixin(false, true, "extra.mixin.render.entity.MixinRenderItemFrame"));
        assertTrue(RendererMixinPlugin.supportsMixin(false, true, "extra.mixin.render.sky.MixinRenderGlobalClouds"));
        assertFalse(
            RendererMixinPlugin.supportsMixin(false, true, "extra.mixin.nothirium.MixinRenderLibEntityDistance"));
    }

}
