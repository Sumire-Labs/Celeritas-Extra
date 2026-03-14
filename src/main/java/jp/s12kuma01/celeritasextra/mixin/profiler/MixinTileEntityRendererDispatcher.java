package jp.s12kuma01.celeritasextra.mixin.profiler;

import jp.s12kuma01.celeritasextra.client.ProfilerHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Add profiler sections for block entity rendering
 * Port of embeddium-extra's profiler.MixinBlockEntityRenderDispatcher
 * <p>
 * In 1.20.1: BlockEntityRenderDispatcher.render()
 * In 1.12.2: TileEntityRendererDispatcher.render()
 * <p>
 * This helps identify rendering performance bottlenecks in F3 profiler
 */
@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRendererDispatcher {

    /**
     * Push profiler section before rendering block entity
     */
    @Inject(
            method = "render(Lnet/minecraft/tileentity/TileEntity;DDDFI)V",
            at = @At("HEAD")
    )
    private void beforeRender(TileEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage, CallbackInfo ci) {
        ProfilerHelper.startSection(tileEntity.getWorld(),
                TileEntityRendererDispatcher.instance.getRenderer(tileEntity.getClass()));
    }

    /**
     * Pop profiler section after rendering block entity
     */
    @Inject(
            method = "render(Lnet/minecraft/tileentity/TileEntity;DDDFI)V",
            at = @At("TAIL")
    )
    private void afterRender(TileEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage, CallbackInfo ci) {
        ProfilerHelper.endSection(tileEntity.getWorld(),
                TileEntityRendererDispatcher.instance.getRenderer(tileEntity.getClass()));
    }
}
