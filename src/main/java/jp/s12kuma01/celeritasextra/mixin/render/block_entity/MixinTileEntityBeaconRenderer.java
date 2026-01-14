package jp.s12kuma01.celeritasextra.mixin.render.block_entity;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls beacon rendering and beam height limiting
 * Ported from MixinBeaconBlockEntityRenderer in Embeddium Extra 1.20.1
 */
@Mixin(TileEntityBeaconRenderer.class)
public class MixinTileEntityBeaconRenderer {

    // Store the beacon being rendered for use in height calculation
    private static TileEntityBeacon currentBeacon;

    @Inject(
        method = "render(Lnet/minecraft/tileentity/TileEntityBeacon;DDDFIF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    public void onRenderHead(TileEntityBeacon te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().renderSettings.beacons) {
            ci.cancel();
            return;
        }
        // Store the current beacon for height limit calculation
        currentBeacon = te;
    }

    /**
     * Modifies the beam height (j1 variable which represents maxY - 256 typically)
     * when limitBeaconBeamHeight is enabled.
     * In 1.12.2, the beacon beam renders from the beacon position up to j1 (typically 256).
     * We modify this to limit it to the world height instead.
     */
    @ModifyVariable(
        method = "render(Lnet/minecraft/tileentity/TileEntityBeacon;DDDFIF)V",
        at = @At(value = "STORE"),
        ordinal = 1  // j1 is the second int variable (height)
    )
    private int modifyBeamHeight(int originalHeight) {
        if (!CeleritasExtraClientMod.options().renderSettings.limitBeaconBeamHeight) {
            return originalHeight;
        }

        if (currentBeacon != null && currentBeacon.getWorld() != null) {
            World world = currentBeacon.getWorld();
            BlockPos pos = currentBeacon.getPos();
            // Limit beam to world height (256 in 1.12.2) minus beacon Y position
            int worldHeight = world.getHeight();
            int beaconY = pos.getY();
            int limitedHeight = worldHeight - beaconY;
            return Math.min(originalHeight, limitedHeight);
        }
        return originalHeight;
    }
}
