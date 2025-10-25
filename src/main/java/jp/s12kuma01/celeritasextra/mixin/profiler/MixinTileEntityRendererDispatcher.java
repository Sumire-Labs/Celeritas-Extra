package jp.s12kuma01.celeritasextra.mixin.profiler;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.WeakHashMap;

/**
 * Add profiler sections for block entity rendering
 * Port of embeddium-extra's profiler.MixinBlockEntityRenderDispatcher
 *
 * In 1.20.1: BlockEntityRenderDispatcher.render()
 * In 1.12.2: TileEntityRendererDispatcher.render()
 *
 * This helps identify rendering performance bottlenecks in F3 profiler
 */
@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRendererDispatcher {

    @Unique
    private static final WeakHashMap<Class<?>, String> celeritasExtra$names = new WeakHashMap<>();

    /**
     * Push profiler section before rendering block entity
     */
    @Inject(
        method = "render(Lnet/minecraft/tileentity/TileEntity;DDDFI)V",
        at = @At("HEAD")
    )
    private void beforeRender(TileEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage, CallbackInfo ci) {
        World world = tileEntity.getWorld();
        if (world != null) {
            TileEntitySpecialRenderer<?> renderer = TileEntityRendererDispatcher.instance.getRenderer(tileEntity.getClass());
            if (renderer != null) {
                String name = celeritasExtra$names.computeIfAbsent(renderer.getClass(), Class::getSimpleName);
                if (!name.isEmpty()) {
                    world.profiler.startSection(name);
                }
            }
        }
    }

    /**
     * Pop profiler section after rendering block entity
     */
    @Inject(
        method = "render(Lnet/minecraft/tileentity/TileEntity;DDDFI)V",
        at = @At("TAIL")
    )
    private void afterRender(TileEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage, CallbackInfo ci) {
        World world = tileEntity.getWorld();
        if (world != null) {
            TileEntitySpecialRenderer<?> renderer = TileEntityRendererDispatcher.instance.getRenderer(tileEntity.getClass());
            if (renderer != null) {
                String name = celeritasExtra$names.computeIfAbsent(renderer.getClass(), Class::getSimpleName);
                if (!name.isEmpty()) {
                    world.profiler.endSection();
                }
            }
        }
    }
}
