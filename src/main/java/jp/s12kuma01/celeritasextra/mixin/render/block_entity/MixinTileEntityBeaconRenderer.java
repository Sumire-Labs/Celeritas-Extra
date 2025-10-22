package jp.s12kuma01.celeritasextra.mixin.render.block_entity;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.tileentity.TileEntityBeacon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityBeaconRenderer.class)
public class MixinTileEntityBeaconRenderer {

    @Inject(
        method = "render(Lnet/minecraft/tileentity/TileEntityBeacon;DDDFIF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    public void render(TileEntityBeacon te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().renderSettings.beacons) {
            ci.cancel();
        }
    }
}
