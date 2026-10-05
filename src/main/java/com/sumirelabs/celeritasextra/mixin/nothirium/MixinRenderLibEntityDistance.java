package com.sumirelabs.celeritasextra.mixin.nothirium;

import com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod;
import com.sumirelabs.celeritasextra.client.VisibilityRules;
import com.sumirelabs.celeritasextra.compat.ShaderPassCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** RenderLib bypasses RenderManager.shouldRender when building its entity list. */
@Pseudo
@Mixin(targets = "meldexun.renderlib.renderer.entity.EntityRenderer", remap = false)
public class MixinRenderLibEntityDistance {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$limitDistance(Entity entity, ICamera camera, double partialTicks,
                                            double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
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
