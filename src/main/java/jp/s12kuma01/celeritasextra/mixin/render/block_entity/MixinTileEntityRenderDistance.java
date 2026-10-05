package jp.s12kuma01.celeritasextra.mixin.render.block_entity;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.VisibilityRules;
import jp.s12kuma01.celeritasextra.compat.ShaderPassCompat;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRenderDistance {
    @Shadow public double entityX;
    @Shadow public double entityY;
    @Shadow public double entityZ;

    // Cull before the inner dispatcher opens a profiler section or invokes a TESR.
    @Inject(method = "render(Lnet/minecraft/tileentity/TileEntity;FI)V", at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$limitDistance(TileEntity tile, float partialTicks, int destroyStage, CallbackInfo ci) {
        var settings = CeleritasExtraClientMod.options().renderSettings;
        if (ShaderPassCompat.isShadowPass()) return;
        if (settings.tileEntityRenderDistance > 0
                && !VisibilityRules.exempt(tile.getClass().getName(), settings.tileEntityDistanceExemptions)
                && VisibilityRules.beyondDistance(tile.getDistanceSq(entityX, entityY, entityZ), settings.tileEntityRenderDistance)) ci.cancel();
    }
}
