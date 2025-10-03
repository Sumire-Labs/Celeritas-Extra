package jp.s12kuma01.celeritasextra.mixin.instant_sneak;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Implements instant sneak feature (no camera transition when sneaking)
 * In 1.12.2, camera height is handled in EntityRenderer
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow
    @Final
    private Minecraft mc;

    /**
     * Modify the eye height to remove the interpolation when sneaking
     * This makes the camera snap instantly to sneak height
     */
    @ModifyVariable(
        method = "orientCamera",
        at = @At(value = "STORE", ordinal = 0),
        ordinal = 3
    )
    private double modifyEyeHeight(double eyeHeight) {
        if (CeleritasExtraClientMod.options().extraSettings.instantSneak) {
            Entity entity = this.mc.getRenderViewEntity();
            if (entity instanceof EntityLivingBase) {
                return entity.getEyeHeight();
            }
        }
        return eyeHeight;
    }
}
