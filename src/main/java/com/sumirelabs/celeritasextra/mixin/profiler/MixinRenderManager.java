package com.sumirelabs.celeritasextra.mixin.profiler;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sumirelabs.celeritasextra.client.ProfilerHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Renderer names are resolved only while the F3 profiler is recording. */
@Mixin(RenderManager.class)
public abstract class MixinRenderManager {
    @Shadow public abstract <T extends Entity> Render<T> getEntityRenderObject(Entity entity);

    @WrapMethod(method = "renderEntity")
    private void celeritasExtra$profile(Entity entity, double x, double y, double z, float yaw,
                                      float partialTicks, boolean debug, Operation<Void> original) {
        var profiler = entity.world == null ? null : entity.world.profiler;
        if (profiler == null || !profiler.profilingEnabled) {
            original.call(entity, x, y, z, yaw, partialTicks, debug);
            return;
        }
        boolean started = ProfilerHelper.startSection(profiler, getEntityRenderObject(entity));
        try { original.call(entity, x, y, z, yaw, partialTicks, debug); }
        finally { ProfilerHelper.endSection(profiler, started); }
    }
}
