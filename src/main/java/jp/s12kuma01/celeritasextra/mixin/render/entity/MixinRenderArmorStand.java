package jp.s12kuma01.celeritasextra.mixin.render.entity;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.entity.RenderArmorStand;
import net.minecraft.entity.item.EntityArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Toggles armor stand rendering via {@link RenderArmorStand}.
 * <p>
 * When {@code renderSettings.armorStands} is disabled, cancels {@code doRender} at HEAD so the
 * armor stand is skipped entirely. Name-tag handling for hidden armor stands lives in
 * {@link MixinRenderLivingBase}, which still draws the label when the body is cancelled.
 */
@Mixin(RenderArmorStand.class)
public class MixinRenderArmorStand {

    @Inject(
            method = "doRender(Lnet/minecraft/entity/item/EntityArmorStand;DDDFF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void doRender(EntityArmorStand entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().renderSettings.armorStands) {
            ci.cancel();
        }
    }
}
