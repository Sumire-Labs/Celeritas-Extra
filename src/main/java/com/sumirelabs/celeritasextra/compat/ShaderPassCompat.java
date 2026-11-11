/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/** Keeps camera-based optimizations out of Iris's light-space shadow pass. */
public final class ShaderPassCompat {

    private static final boolean HAS_IRIS = ShaderPassCompat.class.getClassLoader()
        .getResource("net/irisshaders/iris/api/v0/IrisApi.class") != null;

    private ShaderPassCompat() {}
    public static boolean hasIris() {
        return HAS_IRIS;
    }

    public static boolean isShadowPass() {
        return HAS_IRIS && IrisAccess.isShadowPass();
    }

    /** Resolve an optional API once; an unknown API must not enable camera-based shadow culling. */
    static MethodHandle shadowPassQuery(Class<?> api) {
        try {
            var lookup = MethodHandles.publicLookup();
            var instance = lookup.findStatic(api, "getInstance", MethodType.methodType(api));
            var shadow = lookup.findVirtual(api, "isRenderingShadowPass", MethodType.methodType(boolean.class));
            return MethodHandles.filterReturnValue(instance, shadow);
        } catch (ReflectiveOperationException exception) {
            return MethodHandles.constant(boolean.class, true);
        }
    }

    // This class is resolved only in installations providing Iris's API.
    private static final class IrisAccess {

        private static final MethodHandle QUERY = resolve();

        private static MethodHandle resolve() {
            try {
                return shadowPassQuery(Class.forName("net.irisshaders.iris.api.v0.IrisApi", false,
                    ShaderPassCompat.class.getClassLoader()));
            } catch (ClassNotFoundException | LinkageError exception) {
                return MethodHandles.constant(boolean.class, true);
            }
        }

        private static boolean isShadowPass() {
            try {
                return (boolean) QUERY.invokeExact();
            } catch (Throwable exception) {
                return true;
            }
        }

    }

}
