package com.sumirelabs.celeritasextra.mixin.profiler;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sumirelabs.celeritasextra.client.ProfilerHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRendererDispatcher {
    @WrapMethod(method = "render(Lnet/minecraft/tileentity/TileEntity;DDDFIF)V")
    private void celeritasExtra$profile(TileEntity tile, double x, double y, double z, float partialTicks,
                                      int destroyStage, float alpha, Operation<Void> original) {
        var world = tile.getWorld();
        var profiler = world == null ? null : world.profiler;
        if (profiler == null || !profiler.profilingEnabled) {
            original.call(tile, x, y, z, partialTicks, destroyStage, alpha);
            return;
        }
        boolean started = ProfilerHelper.startSection(profiler,
                TileEntityRendererDispatcher.instance.getRenderer(tile.getClass()));
        try { original.call(tile, x, y, z, partialTicks, destroyStage, alpha); }
        finally { ProfilerHelper.endSection(profiler, started); }
    }
}
