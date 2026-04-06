package jp.s12kuma01.celeritasextra.mixin.render.sky;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Extends the far clipping plane when cloud render distance exceeds game render distance.
 * <p>
 * Without this, clouds beyond the far plane are clipped by the GPU even though
 * the geometry is generated. Angelica solves this identically via
 * {@code SettingsManager.minimumFarPlaneDistance}.
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRendererFarPlane {

    /**
     * Extend the far plane (4th arg of gluPerspective) to accommodate cloud distance.
     * <p>
     * Formula: {@code cloudDistance * 32 + cloudHeight} where 32 = 2 chunks * 16 blocks/chunk,
     * matching the cloud mesh extent calculation in Forge's CloudRenderer.
     */
    @ModifyArg(
            method = "setupCameraTransform",
            at = @At(value = "INVOKE",
                    target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V",
                    remap = false),
            index = 3
    )
    private float celeritasExtra$extendFarPlaneForClouds(float original) {
        var options = CeleritasExtraClientMod.options();
        int cloudDist = options.renderSettings.cloudDistance;
        if (cloudDist > 0 && options.renderSettings.clouds) {
            float minFarPlane = cloudDist * 32.0F + options.renderSettings.cloudHeight;
            return Math.max(original, minFarPlane);
        }
        return original;
    }
}
