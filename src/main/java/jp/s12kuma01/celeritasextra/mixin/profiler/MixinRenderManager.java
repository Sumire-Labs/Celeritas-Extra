package jp.s12kuma01.celeritasextra.mixin.profiler;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.WeakHashMap;

/**
 * Add profiler sections for entity rendering
 * Port of embeddium-extra's profiler.MixinEntityRenderDispatcher
 *
 * In 1.20.1: EntityRenderDispatcher.render()
 * In 1.12.2: RenderManager.renderEntity()
 *
 * This helps identify rendering performance bottlenecks in F3 profiler
 */
@Mixin(RenderManager.class)
public abstract class MixinRenderManager {

    @Shadow
    public abstract <T extends Entity> Render<T> getEntityRenderObject(Entity entity);

    @Unique
    private static final WeakHashMap<Class<?>, String> celeritasExtra$names = new WeakHashMap<>();

    /**
     * Push profiler section before rendering entity
     */
    @Inject(
        method = "renderEntity",
        at = @At("HEAD")
    )
    private void beforeRenderEntity(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci) {
        World world = entity.world;
        if (world != null) {
            Render<?> renderer = this.getEntityRenderObject(entity);
            if (renderer != null) {
                String name = celeritasExtra$names.computeIfAbsent(renderer.getClass(), Class::getSimpleName);
                if (!name.isEmpty()) {
                    world.profiler.startSection(name);
                }
            }
        }
    }

    /**
     * Pop profiler section after rendering entity
     */
    @Inject(
        method = "renderEntity",
        at = @At("TAIL")
    )
    private void afterRenderEntity(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci) {
        World world = entity.world;
        if (world != null) {
            Render<?> renderer = this.getEntityRenderObject(entity);
            if (renderer != null) {
                String name = celeritasExtra$names.computeIfAbsent(renderer.getClass(), Class::getSimpleName);
                if (!name.isEmpty()) {
                    world.profiler.endSection();
                }
            }
        }
    }
}
