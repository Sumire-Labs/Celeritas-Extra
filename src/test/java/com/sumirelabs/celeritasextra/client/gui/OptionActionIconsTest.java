/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OptionActionIconsTest {

    @Test
    void imageIconsUseElevenPixelsAndStayCentered() throws Exception {
        var call = new AtomicReference<Object[]>();
        var context = (DrawContext) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{DrawContext.class},
            (proxy, method, args) -> {
                if (method.getName().equals("blitWholeImage"))
                    call.set(args);
                return null;
            });
        for (var icon : OptionActionIcon.values()) {
            OptionActionIcons.draw(context, new Dim2i(10, 20, 18, 18), icon);
            assertArrayEquals(new Object[]{icon.texture(), 14, 24, 11, 11}, call.get());
            String resource = "assets/" + icon.texture().replace(':', '/');
            try (var input = getClass().getClassLoader().getResourceAsStream(resource)) {
                assertNotNull(input);
                var image = javax.imageio.ImageIO.read(input);
                assertEquals(20, image.getWidth());
                assertEquals(20, image.getHeight());
            }
        }
    }

}
