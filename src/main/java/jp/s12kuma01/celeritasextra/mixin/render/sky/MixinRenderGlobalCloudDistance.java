package jp.s12kuma01.celeritasextra.mixin.render.sky;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Controls cloud render distance
 * In 1.12.2, clouds are rendered based on the render distance setting
 * We modify the effective render distance for clouds
 */
@Mixin(RenderGlobal.class)
public class MixinRenderGlobalCloudDistance {

    /**
     * Modify the cloud render distance
     * This targets the local variable that stores the render distance used for cloud culling
     * The exact injection point may need adjustment based on the decompiled code
     */
    @ModifyVariable(
        method = "renderClouds",
        at = @At(value = "STORE", ordinal = 0),
        ordinal = 0
    )
    private int modifyCloudRenderDistance(int originalDistance) {
        int cloudDistance = CeleritasExtraClientMod.options().renderSettings.cloudDistance;
        return originalDistance * cloudDistance / 100;
    }
}
