/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.mixin.render.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod;
import com.sumirelabs.celeritasextra.client.ItemFrameLodState;
import com.sumirelabs.celeritasextra.client.ItemFrameThreeFaceState;
import com.sumirelabs.celeritasextra.client.VisibilityRules;
import com.sumirelabs.celeritasextra.compat.ShaderPassCompat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItemFrame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.storage.MapData;

/**
 * Independent toggles for the frame, its name tag, and distance LOD of non-block items.
 * Framed maps can independently skip drawing when the camera is behind the frame.
 */
@Mixin(RenderItemFrame.class)
public class MixinRenderItemFrame {

    @WrapOperation(method = "renderItem(Lnet/minecraft/entity/item/EntityItemFrame;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/MapItemRenderer;renderMap(Lnet/minecraft/world/storage/MapData;Z)V"))
    private void celeritasExtra$visibleMap(MapItemRenderer renderer, MapData data, boolean noOverlay,
        Operation<Void> original, @Local(argsOnly = true) EntityItemFrame frame) {
        Minecraft mc = Minecraft.getMinecraft();
        if (CeleritasExtraClientMod.options().renderSettings.mapBackFaceCulling
            && !ShaderPassCompat.isShadowPass()
            && mc.getRenderViewEntity() != null && frame.facingDirection != null) {
            Vec3d camera = ActiveRenderInfo.projectViewFromEntity(mc.getRenderViewEntity(), mc.getRenderPartialTicks());
            var normal = frame.facingDirection.getDirectionVec();
            if (VisibilityRules.behindHorizontalFace(camera.x, camera.z, frame.posX, frame.posZ,
                normal.getX(), normal.getZ(), 0.05))
                return;
        }
        original.call(renderer, data, noOverlay);
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/item/EntityItemFrame;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$doRender(EntityItemFrame entity, double x, double y, double z, float entityYaw,
        float partialTicks, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().renderSettings.itemFrames) {
            ci.cancel();
        }
    }

    @Inject(method = "renderName(Lnet/minecraft/entity/item/EntityItemFrame;DDD)V", at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$renderName(EntityItemFrame entity, double x, double y, double z, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().renderSettings.itemFrameNameTag) {
            ci.cancel();
        }
    }

    /** Scope LOD after Forge's frame render event and restore state even if rendering throws. */
    @WrapOperation(method = "renderItem(Lnet/minecraft/entity/item/EntityItemFrame;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderItem;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V"))
    private void celeritasExtra$renderLodItem(RenderItem renderer, ItemStack stack,
        ItemCameraTransforms.TransformType transform, Operation<Void> original,
        @Local(argsOnly = true) EntityItemFrame entity) {
        boolean previous = ItemFrameLodState.active;
        Vec3d previousCamera = ItemFrameThreeFaceState.camera;
        ItemFrameThreeFaceState.camera = null;
        int distance = CeleritasExtraClientMod.options().renderSettings.itemFrameLodDistance;
        Entity view = Minecraft.getMinecraft().getRenderManager().renderViewEntity;
        ItemFrameLodState.active = distance > 0 && view != null
            && !ShaderPassCompat.isShadowPass()
            && entity.getDistanceSq(view) > (double) distance * distance;
        var settings = CeleritasExtraClientMod.options().renderSettings;
        if (settings.itemFrameThreeFaceCulling && view != null && stack.getItem() instanceof ItemBlock blockItem
            && blockItem.getBlock().getDefaultState().isOpaqueCube()
            && blockItem.getBlock().getDefaultState().isFullCube()
            && renderer.getClass() == RenderItem.class && entity.getClass() == EntityItemFrame.class
            && !ShaderPassCompat.isShadowPass()
            && entity.getDistanceSq(view) > (double) settings.itemFrameThreeFaceDistance
                * settings.itemFrameThreeFaceDistance) {
            var center = entity.getHangingPosition();
            Vec3d camera = ActiveRenderInfo.projectViewFromEntity(view,
                Minecraft.getMinecraft().getRenderPartialTicks());
            ItemFrameThreeFaceState.camera = ItemFrameThreeFaceState.cameraInFrame(camera,
                center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, entity.rotationYaw,
                entity.getRotation());
        }
        try {
            original.call(renderer, stack, transform);
        } finally {
            ItemFrameLodState.active = previous;
            ItemFrameThreeFaceState.camera = previousCamera;
        }
    }

}
