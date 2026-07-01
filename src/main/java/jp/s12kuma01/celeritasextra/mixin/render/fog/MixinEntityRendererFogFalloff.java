package jp.s12kuma01.celeritasextra.mixin.render.fog;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.CloudPassState;
import jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraGameOptions;
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
 * During the cloud pass (see {@link jp.s12kuma01.celeritasextra.client.CloudPassState}) we
 * push fog past the extended cloud volume so distant clouds are not faded out by the
 * render-distance fog end — without affecting terrain fog.
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRendererFogFalloff {

    /** Vanilla GL_LINEAR fog end == farPlaneDistance; used to keep custom fog start below the end. */
    @Shadow
    private float farPlaneDistance;

    @ModifyArg(
            method = "setupFog",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;setFogStart(F)V"),
            index = 0
    )
    private float modifyFogStart(float original) {
        CeleritasExtraGameOptions.RenderSettings rs = CeleritasExtraClientMod.options().renderSettings;

        // Cloud pass: keep clouds out of fog so extended cloud distance is actually visible.
        if (CloudPassState.inCloudPass && rs.clouds && rs.cloudDistance > 0) {
            return CloudPassState.cloudFar(rs.cloudDistance, rs.cloudHeight);
        }

        // Fog off: do nothing here; disableFog() handles suppression. Never write MAX_VALUE.
        if (!rs.fog || rs.fogType == CeleritasExtraGameOptions.FogType.OFF) {
            return original;
        }

        float startPercent = rs.fogStart / 100.0f;
        if (rs.fogDistance > 0) {
            float end = (rs.fogDistance + 1) * 16.0f;
            float start = rs.fogDistance * 16.0f * startPercent;
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

        // Cloud pass: end just beyond the cloud-far start (finite, start < end).
        if (CloudPassState.inCloudPass && rs.clouds && rs.cloudDistance > 0) {
            return CloudPassState.cloudFar(rs.cloudDistance, rs.cloudHeight) + 64.0f;
        }

        if (!rs.fog || rs.fogType == CeleritasExtraGameOptions.FogType.OFF) {
            return original;
        }

        if (rs.fogDistance > 0) {
            return (rs.fogDistance + 1) * 16.0f;
        }

        return original;
    }
}
