package jp.s12kuma01.celeritasextra.mixin.render.block_entity;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.tileentity.TileEntityPistonRenderer;
import net.minecraft.tileentity.TileEntityPiston;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls piston rendering
 * Ported from MixinPistonBlockEntityRenderer in Embeddium Extra 1.20.1
 */
@Mixin(TileEntityPistonRenderer.class)
public class MixinTileEntityPistonRenderer {

    @Inject(
        method = "render(Lnet/minecraft/tileentity/TileEntityPiston;DDDFIF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    public void render(TileEntityPiston te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().renderSettings.pistons) {
            ci.cancel();
        }
    }
}
