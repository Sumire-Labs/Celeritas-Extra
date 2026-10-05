package jp.s12kuma01.celeritasextra.mixin.render.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.ItemFrameLodState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItemFrame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Independent toggles for the frame, its name tag, and distance LOD of non-block items.
 * Maps retain normal rendering; map visibility culling is a separate MoreCulling feature.
 */
@Mixin(RenderItemFrame.class)
public class MixinRenderItemFrame {
    @Inject(
            method = "doRender(Lnet/minecraft/entity/item/EntityItemFrame;DDDFF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void celeritasExtra$doRender(EntityItemFrame entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().renderSettings.itemFrames) {
            ci.cancel();
        }
    }

    @Inject(
            method = "renderName(Lnet/minecraft/entity/item/EntityItemFrame;DDD)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void celeritasExtra$renderName(EntityItemFrame entity, double x, double y, double z, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().renderSettings.itemFrameNameTag) {
            ci.cancel();
        }
    }

    /** Scope LOD after Forge's frame render event and restore state even if rendering throws. */
    @WrapOperation(
            method = "renderItem(Lnet/minecraft/entity/item/EntityItemFrame;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderItem;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V")
    )
    private void celeritasExtra$renderLodItem(RenderItem renderer, ItemStack stack,
                                            ItemCameraTransforms.TransformType transform, Operation<Void> original,
                                            @Local(argsOnly = true) EntityItemFrame entity) {
        boolean previous = ItemFrameLodState.active;
        int distance = CeleritasExtraClientMod.options().renderSettings.itemFrameLodDistance;
        Entity view = Minecraft.getMinecraft().getRenderManager().renderViewEntity;
        ItemFrameLodState.active = distance > 0 && view != null
                && entity.getDistanceSq(view) > (double) distance * distance;
        try {
            original.call(renderer, stack, transform);
        } finally {
            ItemFrameLodState.active = previous;
        }
    }
}
