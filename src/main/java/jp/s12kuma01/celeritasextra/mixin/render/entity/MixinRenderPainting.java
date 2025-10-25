package jp.s12kuma01.celeritasextra.mixin.render.entity;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.entity.RenderPainting;
import net.minecraft.entity.item.EntityPainting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls painting rendering
 * Ported from MixinPaintingEntityRenderer in Embeddium Extra 1.20.1
 */
@Mixin(RenderPainting.class)
public class MixinRenderPainting {

    @Inject(
        method = "doRender(Lnet/minecraft/entity/item/EntityPainting;DDDFF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    public void doRender(EntityPainting entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().renderSettings.paintings) {
            ci.cancel();
        }
    }
}
