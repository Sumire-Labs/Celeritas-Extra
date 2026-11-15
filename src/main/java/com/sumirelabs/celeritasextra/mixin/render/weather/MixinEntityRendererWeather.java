/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.mixin.render.weather;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod;
import com.sumirelabs.celeritasextra.client.RainVisibility;
import com.sumirelabs.celeritasextra.compat.ShaderPassCompat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.math.BlockPos;

/**
 * Toggles rain and snow rendering according to the detail config.
 * <p>
 * In 1.12.2 the weather draw code lives in {@link EntityRenderer#renderRainSnow}. When
 * precipitation rendering is disabled, this mixin cancels that call at {@code HEAD} so no rain
 * or snow particles are drawn.
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRendererWeather {

    @Unique private ICamera celeritasExtra$weatherCamera;
    @Unique private boolean celeritasExtra$cullWeather;
    @Unique private int celeritasExtra$worldHeight;

    @Inject(method = "renderWorldPass", at = @At("HEAD"))
    private void celeritasExtra$beginWeatherView(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        celeritasExtra$weatherCamera = null;
    }

    @WrapOperation(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/culling/ICamera;setPosition(DDD)V"))
    private void celeritasExtra$captureWeatherView(ICamera camera, double x, double y, double z,
        Operation<Void> original) {
        original.call(camera, x, y, z);
        celeritasExtra$weatherCamera = camera;
    }

    @Inject(method = "renderWorldPass", at = @At("RETURN"))
    private void celeritasExtra$endWeatherView(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        celeritasExtra$weatherCamera = null;
    }

    @ModifyExpressionValue(method = "renderRainSnow", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;getY()I"))
    private int celeritasExtra$cullWeatherColumn(int height, @Local BlockPos.MutableBlockPos column,
        @Local(index = 7) int y, @Local(index = 18) int radius) {
        if (!celeritasExtra$cullWeather)
            return height;
        // Capture vanilla's final radius so renderer-specific weather quality overrides remain effective.
        return RainVisibility.visible(celeritasExtra$weatherCamera, column.getX(), column.getZ(), height,
            celeritasExtra$worldHeight, y, radius) ? height : RainVisibility.culledHeight(height, y, radius);
    }

    /**
     * Control rain and snow rendering
     * Method signature for 1.12.2: renderRainSnow(float partialTicks)
     */
    @Inject(method = "renderRainSnow", at = @At("HEAD"), cancellable = true)
    private void renderRainSnow(float partialTicks, CallbackInfo ci) {
        var settings = CeleritasExtraClientMod.options().detailSettings;
        var world = Minecraft.getMinecraft().world;
        celeritasExtra$cullWeather = settings.rainSnowCulling && celeritasExtra$weatherCamera != null
            && world != null && !ShaderPassCompat.isShadowPass();
        if (celeritasExtra$cullWeather)
            celeritasExtra$worldHeight = world.getHeight();
        if (!settings.rainSnow) {
            ci.cancel();
        }
    }

}
