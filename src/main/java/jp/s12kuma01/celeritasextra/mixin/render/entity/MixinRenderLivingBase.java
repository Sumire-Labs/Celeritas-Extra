package jp.s12kuma01.celeritasextra.mixin.render.entity;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Controls living entity rendering and name tags via {@link RenderLivingBase}, the shared
 * living-entity renderer in 1.12.2.
 * <p>
 * Complements {@link MixinRenderArmorStand}: when armor stand rendering is disabled it cancels
 * {@code doRender} but still draws the entity's label, so hidden armor stands keep their name tag.
 * Independently, it suppresses player name tags when {@code renderSettings.playerNameTag} is
 * disabled.
 */
@Mixin(RenderLivingBase.class)
public abstract class MixinRenderLivingBase<T extends EntityLivingBase> extends Render<T> {

    protected MixinRenderLivingBase(RenderManager renderManager) {
        super(renderManager);
    }

    /**
     * Control armor stand rendering
     * Armor stands are already handled by MixinRenderArmorStand, but this adds label support
     */
    @Inject(
            method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (entity instanceof EntityArmorStand && !CeleritasExtraClientMod.options().renderSettings.armorStands) {
            ci.cancel();
            // Still render name tag if it has one
            if (this.canRenderName(entity)) {
                this.renderLivingLabel(entity, entity.getDisplayName().getFormattedText(), x, y, z, 64);
            }
        }
    }

    /**
     * Control player name tags
     */
    @Inject(
            method = "canRenderName(Lnet/minecraft/entity/EntityLivingBase;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    protected void canRenderName(T entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof AbstractClientPlayer && !CeleritasExtraClientMod.options().renderSettings.playerNameTag) {
            cir.setReturnValue(false);
        }
    }
}
