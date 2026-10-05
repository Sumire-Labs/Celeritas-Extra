package com.sumire.celeritasextra.mixin.render.fog;

import com.sumire.celeritasextra.client.CeleritasExtraClientMod;
import com.sumire.celeritasextra.client.CloudPassState;
import com.sumire.celeritasextra.client.FogState;
import com.sumire.celeritasextra.client.DimensionFog;
import com.sumire.celeritasextra.compat.ShaderPassCompat;
import com.sumire.celeritasextra.client.gui.CeleritasExtraGameOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Fog falloff control — adjusts the GL_LINEAR fog start/end distances.
 * <p>
 * Celeritas/Sodium reads GL_FOG_START / GL_FOG_END into its terrain & sky shader fog
 * uniforms, so these values must always be finite with {@code start < end} — feeding
 * Float.MAX_VALUE (or start == end) poisons the shader fog math. When fog is OFF we leave
 * the values untouched and let {@link MixinEntityRendererFog}'s {@code disableFog()} drive
 * Celeritas to its no-fog shader variant (and disable fixed-function fog).
 * <p>
 * During the cloud pass (see {@link com.sumire.celeritasextra.client.CloudPassState}) we
 * push fog past the extended cloud volume so distant clouds are not faded out by the
 * render-distance fog end — without affecting terrain fog.
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRendererFogFalloff {

    /**
     * Vanilla GL_LINEAR fog end == farPlaneDistance; used to keep custom fog start below the end.
     */
    @Shadow
    private float farPlaneDistance;

    @ModifyArg(
            method = "setupFog",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;setFogStart(F)V"),
            index = 0
    )
    private float modifyFogStart(float original) {
        CeleritasExtraGameOptions.RenderSettings rs = CeleritasExtraClientMod.options().renderSettings;
        var fog = DimensionFog.current();

        if (FogState.isGameplayFog() || ShaderPassCompat.isShadowPass()) return original;

        // Cloud pass: keep clouds out of fog so extended cloud distance is actually visible.
        if (CloudPassState.inCloudPass && extendsCloudRange(rs)) {
            return cloudFar(rs);
        }

        // Fog off: do nothing here; disableFog() handles suppression. Never write MAX_VALUE.
        if (!fog.enabled()) {
            return original;
        }

        float startPercent = fog.start() / 100.0f;
        if (fog.distance() > 0) {
            float end = (fog.distance() + 1) * 16.0f;
            float start = fog.distance() * 16.0f * startPercent;
            return Math.min(start, end - 0.5f);
        }

        // Default: scale vanilla start, but keep it below the fog end (== farPlaneDistance).
        float start = original * startPercent;
        return Math.min(start, farPlaneDistance - 0.5f);
    }

    @ModifyArg(
            method = "setupFog",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;setFogEnd(F)V"),
            index = 0
    )
    private float modifyFogEnd(float original) {
        CeleritasExtraGameOptions.RenderSettings rs = CeleritasExtraClientMod.options().renderSettings;
        var fog = DimensionFog.current();

        if (FogState.isGameplayFog() || ShaderPassCompat.isShadowPass()) return original;

        // Cloud pass: end just beyond the cloud-far start (finite, start < end).
        if (CloudPassState.inCloudPass && extendsCloudRange(rs)) {
            return cloudFar(rs) + 64.0f;
        }

        if (!fog.enabled()) {
            return original;
        }

        if (fog.distance() > 0) {
            return (fog.distance() + 1) * 16.0f;
        }

        return original;
    }

    private static boolean extendsCloudRange(CeleritasExtraGameOptions.RenderSettings settings) {
        return CloudPassState.cloudsEnabled(settings) && settings.cloudDistance > 0
                && CloudPassState.usesDefaultCloudRenderer();
    }

    private static int effectiveCloudDistance(CeleritasExtraGameOptions.RenderSettings settings) {
        return CloudPassState.effectiveCloudDistanceChunks(
                settings, Minecraft.getMinecraft().gameSettings.renderDistanceChunks);
    }

    private static float cloudFar(CeleritasExtraGameOptions.RenderSettings settings) {
        return CloudPassState.cloudFar(effectiveCloudDistance(settings), settings.cloudScale);
    }
}
