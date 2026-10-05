package jp.s12kuma01.celeritasextra.mixin.render.entity;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.VisibilityRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderManager.class)
public class MixinEntityRenderDistance {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$limitDistance(Entity entity, ICamera camera, double x, double y, double z,
                                              CallbackInfoReturnable<Boolean> cir) {
        var settings = CeleritasExtraClientMod.options().renderSettings;
        if (entity != Minecraft.getMinecraft().getRenderViewEntity()
                && settings.entityRenderDistance > 0
                && !VisibilityRules.exempt(entity.getClass().getName(), settings.entityDistanceExemptions)
                && VisibilityRules.beyondDistance(entity.getDistanceSq(x, y, z), settings.entityRenderDistance)) {
            cir.setReturnValue(false);
        }
    }
}
