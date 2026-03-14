package jp.s12kuma01.celeritasextra.mixin.profiler;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Add profiler sections for entity rendering
 * Port of embeddium-extra's profiler.MixinEntityRenderDispatcher
 * <p>
 * In 1.20.1: EntityRenderDispatcher.render()
 * In 1.12.2: RenderManager.renderEntity()
 * <p>
 * This helps identify rendering performance bottlenecks in F3 profiler
 */
@Mixin(RenderManager.class)
public abstract class MixinRenderManager {

    @Shadow
    public abstract <T extends Entity> Render<T> getEntityRenderObject(Entity entity);

    /**
     * Push profiler section before rendering entity
     */
    @Inject(
            method = "renderEntity",
            at = @At("HEAD")
    )
    private void beforeRenderEntity(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci) {
        ProfilerHelper.startSection(entity.world, this.getEntityRenderObject(entity));
    }

    /**
     * Pop profiler section after rendering entity
     */
    @Inject(
            method = "renderEntity",
            at = @At("TAIL")
    )
    private void afterRenderEntity(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci) {
        ProfilerHelper.endSection(entity.world, this.getEntityRenderObject(entity));
    }
}
