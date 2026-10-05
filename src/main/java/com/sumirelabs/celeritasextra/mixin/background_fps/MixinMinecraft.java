package com.sumirelabs.celeritasextra.mixin.background_fps;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.sumirelabs.celeritasextra.client.BackgroundFrameLimiter;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "runGameLoop", at = @At("HEAD"))
    private void celeritasExtra$scheduleFrame(CallbackInfo ci) {
        BackgroundFrameLimiter.beginFrame(System.nanoTime());
    }

    // Wrap the whole method so native cancellable HEAD injectors cannot bypass
    // the background limiter with a newly inserted early return.
    @WrapMethod(method = "getLimitFramerate")
    private int celeritasExtra$backgroundLimit(Operation<Integer> original) {
        return BackgroundFrameLimiter.loopLimit(BackgroundFrameLimiter.currentNormalLimit(original.call()), BackgroundFrameLimiter.currentLimit());
    }

    @WrapWithCondition(method = "runGameLoop", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GlStateManager;clear(I)V"))
    private boolean celeritasExtra$keepPreviousFrame(int mask) {
        return BackgroundFrameLimiter.shouldRender();
    }
}
