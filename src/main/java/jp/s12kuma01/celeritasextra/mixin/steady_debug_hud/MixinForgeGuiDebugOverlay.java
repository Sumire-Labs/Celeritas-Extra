package jp.s12kuma01.celeritasextra.mixin.steady_debug_hud;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Reduce F3 debug screen update frequency
 * Port of embeddium-extra's steady_debug_hud.MixinDebugHud
 *
 * In 1.20.1: Forge's ForgeGui$ForgeDebugScreenOverlay
 * In 1.12.2: Forge's GuiIngameForge (debug rendering is built into GuiIngame)
 *
 * This reduces CPU usage by caching debug strings and only updating them periodically
 * Default refresh interval is 20 ticks (1 second)
 */
@Mixin(targets = "net.minecraftforge.client.GuiIngameForge")
public abstract class MixinForgeGuiDebugOverlay {

    @Unique
    private final List<String> celeritasExtra$leftTextCache = new ArrayList<>();
    @Unique
    private final List<String> celeritasExtra$rightTextCache = new ArrayList<>();
    @Unique
    private long celeritasExtra$nextTime = 0L;
    @Unique
    private boolean celeritasExtra$rebuild = true;

    /**
     * Control when to rebuild debug text
     */
    @Inject(
        method = "renderDebugInfo",
        at = @At("HEAD"),
        remap = false
    )
    private void beforeRenderDebugInfo(int width, CallbackInfo ci) {
        if (CeleritasExtraClientMod.options().extraSettings.steadyDebugHud) {
            final long currentTime = System.currentTimeMillis();
            if (currentTime > this.celeritasExtra$nextTime) {
                this.celeritasExtra$rebuild = true;
                this.celeritasExtra$nextTime = currentTime +
                    (CeleritasExtraClientMod.options().extraSettings.steadyDebugHudRefreshInterval * 50L);
            } else {
                this.celeritasExtra$rebuild = false;
            }
        } else {
            this.celeritasExtra$rebuild = true;
        }
    }

    /**
     * Cache left side debug text
     */
    @Inject(
        method = "getDebugInfoLeft",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void beforeGetLeftDebugText(CallbackInfoReturnable<List<String>> cir) {
        if (!this.celeritasExtra$rebuild) {
            cir.setReturnValue(this.celeritasExtra$leftTextCache);
        }
    }

    @Inject(
        method = "getDebugInfoLeft",
        at = @At("RETURN"),
        remap = false
    )
    private void afterGetLeftDebugText(CallbackInfoReturnable<List<String>> cir) {
        if (this.celeritasExtra$rebuild) {
            this.celeritasExtra$leftTextCache.clear();
            this.celeritasExtra$leftTextCache.addAll(cir.getReturnValue());
        }
    }

    /**
     * Cache right side debug text
     */
    @Inject(
        method = "getDebugInfoRight",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void beforeGetRightDebugText(CallbackInfoReturnable<List<String>> cir) {
        if (!this.celeritasExtra$rebuild) {
            cir.setReturnValue(this.celeritasExtra$rightTextCache);
        }
    }

    @Inject(
        method = "getDebugInfoRight",
        at = @At("RETURN"),
        remap = false
    )
    private void afterGetRightDebugText(CallbackInfoReturnable<List<String>> cir) {
        if (this.celeritasExtra$rebuild) {
            this.celeritasExtra$rightTextCache.clear();
            this.celeritasExtra$rightTextCache.addAll(cir.getReturnValue());
        }
    }
}
