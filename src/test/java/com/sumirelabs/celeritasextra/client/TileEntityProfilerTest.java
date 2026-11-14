/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sumirelabs.celeritasextra.mixin.profiler.MixinTileEntityRendererDispatcher;

import net.minecraft.tileentity.TileEntity;

import static org.junit.jupiter.api.Assertions.*;

class TileEntityProfilerTest {

    @Test
    void nullTileStillReachesTheOriginalNoOpRenderer() throws Exception {
        var hook = MixinTileEntityRendererDispatcher.class.getDeclaredMethod("celeritasExtra$profile",
            TileEntity.class, double.class, double.class, double.class, float.class, int.class, float.class,
            Operation.class);
        hook.setAccessible(true);
        var calls = new AtomicInteger();
        Operation<Void> original = arguments -> {
            assertArrayEquals(new Object[]{null, 1.0, 2.0, 3.0, 0.5f, -1, 1.0f}, arguments);
            calls.incrementAndGet();
            return null;
        };
        hook.invoke(new MixinTileEntityRendererDispatcher(), null, 1.0, 2.0, 3.0, 0.5f, -1, 1.0f, original);
        assertEquals(1, calls.get());
    }

}
