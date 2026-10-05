package jp.s12kuma01.celeritasextra.compat;

/** Keeps camera-based optimizations out of Iris's light-space shadow pass. */
public final class ShaderPassCompat {
    private static final boolean HAS_IRIS = ShaderPassCompat.class.getClassLoader()
            .getResource("net/irisshaders/iris/api/v0/IrisApi.class") != null;

    private ShaderPassCompat() { }

    public static boolean isShadowPass() {
        return HAS_IRIS && IrisAccess.isShadowPass();
    }

    // This class is resolved only in installations providing Iris's API.
    private static final class IrisAccess {
        private static boolean isShadowPass() {
            return net.irisshaders.iris.api.v0.IrisApi.getInstance().isRenderingShadowPass();
        }
    }
}
