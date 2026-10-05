package com.sumire.celeritasextra.mixin.steady_debug_hud;

import com.sumire.celeritasextra.client.CeleritasExtraClientMod;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiOverlayDebug;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Throttles how often the F3 debug overlay text is rebuilt.
 * Port of embeddium-extra's steady_debug_hud.MixinDebugHud.
 * <p>
 * The vanilla debug screen recomputes every line each frame, which is comparatively expensive.
 * When enabled, this caches the left- and right-hand text lists and only rebuilds them on a fixed
 * interval (default 1 tick / 50 milliseconds), returning the cached lists in between to reduce CPU
 * usage. When the feature is off, the text is rebuilt every frame as in vanilla.
 * <p>
 * In 1.12.2 the text producers live on Minecraft's {@link GuiOverlayDebug}.
 */
@Mixin(GuiOverlayDebug.class)
public abstract class MixinForgeGuiDebugOverlay {

    @Unique
    private final List<String> celeritasExtra$leftTextCache = new ArrayList<>();
    @Unique
    private final List<String> celeritasExtra$rightTextCache = new ArrayList<>();
    @Unique
    private long celeritasExtra$nextUpdateNanos;
    @Unique
    private boolean celeritasExtra$rebuild = true;

    /**
     * Control when to rebuild debug text
     */
    @Inject(
            method = "renderDebugInfo",
            at = @At("HEAD")
    )
    private void celeritasExtra$beforeRenderDebugInfo(ScaledResolution resolution, CallbackInfo ci) {
        if (CeleritasExtraClientMod.options().extraSettings.steadyDebugHud) {
            long now = System.nanoTime();
            if (now >= this.celeritasExtra$nextUpdateNanos) {
                this.celeritasExtra$rebuild = true;
                this.celeritasExtra$nextUpdateNanos = now
                        + CeleritasExtraClientMod.options().extraSettings.steadyDebugHudRefreshInterval
                        * 50_000_000L;
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
    @WrapMethod(method = "call()Ljava/util/List;")
    private List<String> celeritasExtra$cacheLeftDebugText(Operation<List<String>> original) {
        // Cache the complete method, including Nothirium/RenderLib's RETURN injections.
        // Cancelling at HEAD would run those injections on the cached list again each frame.
        if (!this.celeritasExtra$rebuild) return new ArrayList<>(this.celeritasExtra$leftTextCache);
        List<String> result = original.call();
        this.celeritasExtra$leftTextCache.clear();
        this.celeritasExtra$leftTextCache.addAll(result);
        return result;
    }

    /**
     * Cache right side debug text
     */
    @WrapMethod(method = "getDebugInfoRight()Ljava/util/List;")
    private List<String> celeritasExtra$cacheRightDebugText(Operation<List<String>> original) {
        if (!this.celeritasExtra$rebuild) return new ArrayList<>(this.celeritasExtra$rightTextCache);
        List<String> result = original.call();
        this.celeritasExtra$rightTextCache.clear();
        this.celeritasExtra$rightTextCache.addAll(result);
        return result;
    }
}
