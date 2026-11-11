/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ShaderPassCompatTest {

    private static boolean shadowPass;

    public interface TestApi {

        static TestApi getInstance() {
            return () -> shadowPass;
        }

        boolean isRenderingShadowPass();

    }

    @Test
    void optionalApiQueryReadsTheCurrentPassRatherThanCachingItsValue() throws Throwable {
        var query = ShaderPassCompat.shadowPassQuery(TestApi.class);
        shadowPass = false;
        assertFalse((boolean) query.invokeExact());
        shadowPass = true;
        assertTrue((boolean) query.invokeExact());
        shadowPass = false;
        assertFalse((boolean) query.invokeExact());
    }

    @Test
    void unknownApiConservativelyDisablesCameraBasedShadowOptimizations() throws Throwable {
        var query = ShaderPassCompat.shadowPassQuery(String.class);
        assertTrue((boolean) query.invokeExact());
    }

}
