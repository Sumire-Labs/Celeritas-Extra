package jp.s12kuma01.celeritasextra.mixin.background_fps;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import jp.s12kuma01.celeritasextra.client.BackgroundFrameLimiter;
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

    @ModifyReturnValue(method = "getLimitFramerate", at = @At("RETURN"))
    private int celeritasExtra$backgroundLimit(int original) {
        return BackgroundFrameLimiter.loopLimit(original, BackgroundFrameLimiter.currentLimit());
    }

    @WrapWithCondition(method = "runGameLoop", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GlStateManager;clear(I)V"))
    private boolean celeritasExtra$keepPreviousFrame(int mask) {
        return BackgroundFrameLimiter.shouldRender();
    }
}
