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

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow
    @Final
    private Minecraft mc;

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
