package jp.s12kuma01.celeritasextra.mixin.profiler;

import jp.s12kuma01.celeritasextra.client.ProfilerHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wraps block-entity (tile-entity) rendering in named profiler sections for the F3 profiler graph.
 * Port of embeddium-extra's profiler.MixinBlockEntityRenderDispatcher.
 * <p>
 * Pushes a profiler section keyed by the tile entity's renderer at the {@code HEAD} of
 * {@link TileEntityRendererDispatcher#render} and pops it at the {@code TAIL}, exposing the
 * per-renderer cost so block-entity rendering bottlenecks can be identified.
 * <p>
 * In 1.20.1: BlockEntityRenderDispatcher.render()
 * In 1.12.2: TileEntityRendererDispatcher.render()
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
