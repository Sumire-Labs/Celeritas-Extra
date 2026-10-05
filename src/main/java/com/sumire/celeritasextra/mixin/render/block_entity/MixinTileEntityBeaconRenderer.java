package com.sumire.celeritasextra.mixin.render.block_entity;

import com.sumire.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.tileentity.TileEntityBeacon;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls beacon rendering and beam height via {@link TileEntityBeaconRenderer}.
 * Ported from MixinBeaconBlockEntityRenderer in Embeddium Extra 1.20.1.
 * <p>
 * Two behaviors, each gated by a render setting:
 * - {@code beacons}: cancels {@code render} at HEAD to skip the beacon and its beam entirely.
 * - {@code limitBeaconBeamHeight}: clamps the beam so it stops at the world ceiling instead of
 * allowing its final segment to draw past the top of the world.
 */
@Mixin(TileEntityBeaconRenderer.class)
public class MixinTileEntityBeaconRenderer {

    @Unique
    private TileEntityBeacon celeritasExtra$currentBeacon;

    @Inject(
            method = "render(Lnet/minecraft/tileentity/TileEntityBeacon;DDDFIF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void celeritasExtra$beginRender(TileEntityBeacon te, double x, double y, double z,
                                            float partialTicks, int destroyStage, float alpha,
                                            CallbackInfo ci) {
        this.celeritasExtra$currentBeacon = null;
        if (!CeleritasExtraClientMod.options().renderSettings.beacons) {
            ci.cancel();
            return;
        }
        this.celeritasExtra$currentBeacon = te;
    }

    @Inject(
            method = "render(Lnet/minecraft/tileentity/TileEntityBeacon;DDDFIF)V",
            at = @At("RETURN")
    )
    private void celeritasExtra$endRender(TileEntityBeacon te, double x, double y, double z,
                                          float partialTicks, int destroyStage, float alpha,
                                          CallbackInfo ci) {
        this.celeritasExtra$currentBeacon = null;
    }

    /**
     * Clamp each segment at the real world ceiling. The 1.12 renderer has no local "beam height"
     * variable in {@code render}; it forwards a list of segments to {@code renderBeacon}. Wrapping
     * the segment draw therefore targets the value that is actually consumed and also accounts for
     * the accumulated height of earlier segments.
     */
    @WrapOperation(
            method = "renderBeacon(DDDDDLjava/util/List;D)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/tileentity/TileEntityBeaconRenderer;renderBeamSegment(DDDDDDII[F)V"
            )
    )
    private void celeritasExtra$limitBeamSegment(double x, double y, double z,
                                                  double partialTicks, double beamScale,
                                                  double worldTime, int segmentStart,
                                                  int segmentHeight, float[] colors,
                                                  Operation<Void> original) {
        TileEntityBeacon beacon = this.celeritasExtra$currentBeacon;
        if (CeleritasExtraClientMod.options().renderSettings.limitBeaconBeamHeight
                && beacon != null && beacon.getWorld() != null) {
            int remainingHeight = beacon.getWorld().getHeight()
                    - beacon.getPos().getY() - segmentStart;
            segmentHeight = Math.min(segmentHeight, Math.max(0, remainingHeight));
        }

        if (segmentHeight > 0) {
            original.call(x, y, z, partialTicks, beamScale, worldTime,
                    segmentStart, segmentHeight, colors);
        }
    }
}
