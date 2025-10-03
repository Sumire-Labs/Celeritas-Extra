package jp.s12kuma01.celeritasextra.mixin.render.entity;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.entity.RenderItemFrame;
import net.minecraft.entity.item.EntityItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Controls item frame rendering and name tags
 * Ported from MixinItemFrameEntityRenderer in Embeddium Extra 1.20.1
 */
@Mixin(RenderItemFrame.class)
public class MixinRenderItemFrame {

    @Inject(
        method = "doRender(Lnet/minecraft/entity/item/EntityItemFrame;DDDFF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    public void doRender(EntityItemFrame entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().renderSettings.itemFrames) {
            ci.cancel();
        }
    }

    /**
     * Controls item frame name tag rendering
     * In 1.12.2, RenderItemFrame inherits from Render which has canRenderName method
     */
    @Inject(
        method = "canRenderName(Lnet/minecraft/entity/item/EntityItemFrame;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    protected void canRenderName(EntityItemFrame entity, CallbackInfoReturnable<Boolean> cir) {
        if (!CeleritasExtraClientMod.options().renderSettings.itemFrameNameTag) {
            cir.setReturnValue(false);
        }
    }
}
