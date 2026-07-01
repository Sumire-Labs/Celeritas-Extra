package jp.s12kuma01.celeritasextra.client;

import net.minecraft.util.math.MathHelper;

/**
 * Shared client render-thread state for the cloud rendering pass.
 * <p>
 * {@code EntityRenderer.renderCloudsCheck} installs its own projection and calls
 * {@code setupFog} before drawing clouds. We flag that window so the fog mixin can
 * push fog past the (extended) cloud volume only during the cloud pass, without
 * touching terrain fog. Client render thread only — no synchronization needed.
 */
public final class CloudPassState {

    /** True while EntityRenderer.renderCloudsCheck is executing. */
    public static boolean inCloudPass = false;

    private CloudPassState() {
    }

    /**
     * Far distance (in blocks) that must be reached for clouds to render out to the
     * configured cloud distance. Covers the cloud mesh's far horizontal corner
     * (diagonal of the {@code cloudDistance * 32} half-extent) plus the vertical
     * offset and a small grid-snap margin.
     */
    public static float cloudFar(int cloudDistance, int cloudHeight) {
        return MathHelper.SQRT_2 * cloudDistance * 32.0F + cloudHeight + 16.0F;
    }
}
