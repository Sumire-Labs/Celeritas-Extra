package com.sumire.celeritasextra.client;

import com.sumire.celeritasextra.client.gui.CeleritasExtraGameOptions.RenderSettings;
import com.sumire.celeritasextra.compat.RendererCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
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

    /** Actinium owns the cloud toggle; Extra's saved toggle applies to Celeritas. */
    public static boolean cloudsEnabled(RenderSettings settings) {
        return RendererCompat.isActinium() ? Minecraft.getMinecraft().gameSettings.clouds != 0 : settings.clouds;
    }

    /**
     * Forge 1.12.2 builds its legacy cloud mesh to {@code (renderDistance * 2) * 16} blocks.
     * Celeritas Extra passes Cloud Distance through as that render-distance input.
     */
    private static final float CLOUD_RANGE_BLOCKS_PER_DISTANCE_STEP = 32.0F;

    /**
     * True while EntityRenderer.renderCloudsCheck is executing.
     */
    public static boolean inCloudPass = false;

    private CloudPassState() {
    }

    /**
     * Far distance (in blocks) that must be reached for clouds to render out to the
     * configured cloud distance. Covers the cloud mesh's far horizontal corner plus
     * the camera-to-cloud vertical distance and a small grid-snap margin.
     */
    public static float cloudFar(int cloudDistance) {
        return cloudFar(cloudDistance, RenderSettings.CLOUD_SCALE_VANILLA);
    }

    /**
     * Scale-aware variant that also covers the largest possible cell-grid snap at the mesh edge.
     */
    public static float cloudFar(int cloudDistance, int cloudScale) {
        float verticalDistance = verticalDistanceToClouds();
        float cellSize = 12.0F * Math.max(RenderSettings.CLOUD_SCALE_MIN, cloudScale)
                / RenderSettings.CLOUD_SCALE_VANILLA;
        float gridSnapMargin = Math.max(16.0F, MathHelper.SQRT_2 * cellSize * 2.0F);

        float horizontalDistance = cloudHorizontalRangeBlocks(cloudDistance);
        return MathHelper.SQRT_2 * horizontalDistance + verticalDistance + gridSnapMargin;
    }

    /**
     * Converts the user-facing cloud distance into the physical radius used by Forge's legacy
     * renderer, independently of the selected cloud texture.
     */
    public static float cloudHorizontalRangeBlocks(int cloudDistance) {
        if (cloudDistance < 0) {
            throw new IllegalArgumentException("Cloud distance cannot be negative");
        }
        return cloudDistance * CLOUD_RANGE_BLOCKS_PER_DISTANCE_STEP;
    }

    /**
     * Resolves the cloud range shared by the mesh, cloud-pass projection, and fog.
     * An explicit distance wins; zero follows Minecraft's current render distance.
     */
    public static int effectiveCloudDistanceChunks(RenderSettings settings, int renderDistanceChunks) {
        return settings.cloudDistance > 0 ? settings.cloudDistance : renderDistanceChunks;
    }

    /**
     * Returns whether the current dimension is using Forge's default cloud path. Dimension-provided
     * render handlers must remain in control of their own geometry, projection, and fog range.
     */
    public static boolean usesDefaultCloudRenderer() {
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft.world != null
                && minecraft.world.provider != null
                && minecraft.world.provider.getCloudRenderer() == null;
    }

    /**
     * Resolve the effective dimension cloud height, including this mod's configured override.
     */
    public static float cloudHeight(float fallback) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world != null && minecraft.world.provider != null) {
            return minecraft.world.provider.getCloudHeight();
        }
        return fallback;
    }

    private static float verticalDistanceToClouds() {
        Minecraft minecraft = Minecraft.getMinecraft();
        float cloudHeight = cloudHeight(128.0F);
        Entity viewEntity = minecraft.getRenderViewEntity();
        return viewEntity == null
                ? Math.abs(cloudHeight)
                : (float) Math.abs(cloudHeight - viewEntity.posY);
    }
}
