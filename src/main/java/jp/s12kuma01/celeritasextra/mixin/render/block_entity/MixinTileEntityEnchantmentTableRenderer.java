package jp.s12kuma01.celeritasextra.mixin.render.block_entity;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.tileentity.TileEntityEnchantmentTableRenderer;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Toggles the enchanting table's animated floating book via {@link TileEntityEnchantmentTableRenderer}.
 * Ported from MixinEnchantingTableBlockEntityRenderer in Embeddium Extra 1.20.1.
 * <p>
 * When {@code renderSettings.enchantingTableBooks} is disabled, cancels {@code render} at HEAD so
 * the book is skipped.
 */
@Mixin(TileEntityEnchantmentTableRenderer.class)
public class MixinTileEntityEnchantmentTableRenderer {

    @Inject(
            method = "render(Lnet/minecraft/tileentity/TileEntityEnchantmentTable;DDDFIF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void render(TileEntityEnchantmentTable te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().renderSettings.enchantingTableBooks) {
            ci.cancel();
        }
    }
}
