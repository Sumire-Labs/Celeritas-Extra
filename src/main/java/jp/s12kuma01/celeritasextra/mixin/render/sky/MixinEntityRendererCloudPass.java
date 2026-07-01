package jp.s12kuma01.celeritasextra.mixin.render.sky;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.CloudPassState;
import jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraGameOptions;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes the cloud render distance setting actually take effect.
 * <p>
 * In 1.12.2 the cloud pass ({@code EntityRenderer.renderCloudsCheck}) reloads its OWN
 * projection ({@code gluPerspective(..., farPlaneDistance * 4)}) and runs {@code setupFog}
 * right before drawing clouds — so editing the terrain camera ({@code setupCameraTransform})
 * does nothing for clouds. This mixin instead:
 * <ul>
 *   <li>widens the cloud-pass far clip plane to cover the enlarged cloud mesh, and</li>
 *   <li>flags the cloud-pass window so {@code MixinEntityRendererFogFalloff} can push fog
 *       past the cloud volume (the dominant cap is fog, not the far plane).</li>
 * </ul>
 * The cloud mesh extent itself is enlarged by {@link MixinCloudRenderer}.
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRendererCloudPass {

    @Inject(method = "renderCloudsCheck", at = @At("HEAD"))
    private void celeritasExtra$beginCloudPass(RenderGlobal renderGlobalIn, float partialTicks, int pass,
                                               double x, double y, double z, CallbackInfo ci) {
        CloudPassState.inCloudPass = true;
    }

    @Inject(method = "renderCloudsCheck", at = @At("RETURN"))
    private void celeritasExtra$endCloudPass(RenderGlobal renderGlobalIn, float partialTicks, int pass,
                                             double x, double y, double z, CallbackInfo ci) {
        CloudPassState.inCloudPass = false;
    }

    /**
     * Widen the cloud-pass projection far plane (4th arg of the first gluPerspective in
     * renderCloudsCheck, the {@code farPlaneDistance * 4} call) so the extended cloud mesh
     * is not GPU-clipped. Keeps vanilla behavior when cloud distance is unset/smaller.
     */
    @ModifyArg(
            method = "renderCloudsCheck",
            at = @At(value = "INVOKE",
                    target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V",
                    remap = false,
                    ordinal = 0),
            index = 3
    )
    private float celeritasExtra$widenCloudFarPlane(float original) {
        CeleritasExtraGameOptions.RenderSettings rs = CeleritasExtraClientMod.options().renderSettings;
        if (rs.clouds && rs.cloudDistance > 0) {
            return Math.max(original, CloudPassState.cloudFar(rs.cloudDistance, rs.cloudHeight) + 128.0F);
        }
        return original;
    }
}
