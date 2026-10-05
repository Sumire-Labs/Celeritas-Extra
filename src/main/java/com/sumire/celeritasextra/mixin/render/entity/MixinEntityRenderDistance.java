package com.sumire.celeritasextra.mixin.render.entity;

import com.sumire.celeritasextra.client.CeleritasExtraClientMod;
import com.sumire.celeritasextra.client.VisibilityRules;
import com.sumire.celeritasextra.compat.ShaderPassCompat;
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
        if (ShaderPassCompat.isShadowPass()) return;
        if (entity != Minecraft.getMinecraft().getRenderViewEntity()
                && settings.entityRenderDistance > 0
                && !VisibilityRules.exempt(entity.getClass().getName(), settings.entityDistanceExemptions)
                && VisibilityRules.beyondDistance(entity.getDistanceSq(x, y, z), settings.entityRenderDistance)) {
            cir.setReturnValue(false);
        }
    }
}
