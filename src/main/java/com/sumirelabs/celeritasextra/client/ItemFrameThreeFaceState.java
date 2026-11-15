/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.animation.AnimationItemOverrideList;

/** Camera position in the item frame's centered, half-scale coordinate system. Render thread only. */
public final class ItemFrameThreeFaceState {

    public static Vec3d camera;
    private static final Map<IBakedModel, Boolean> FORGE_TRANSFORMS = new WeakHashMap<>();
    private static final Map<Key, Cached> CACHE = new LinkedHashMap<>(32, 0.75f, true) {

        @Override
        protected boolean removeEldestEntry(Map.Entry<Key, Cached> entry) {
            return size() > 256;
        }

    };

    private ItemFrameThreeFaceState() {}

    public static void clearCache() {
        CACHE.clear();
        FORGE_TRANSFORMS.clear();
    }

    /** Inverse of RenderItemFrame's translation, Y rotation, Z rotation and 0.5 scale. */
    public static Vec3d cameraInFrame(Vec3d view, double centerX, double centerY, double centerZ,
        float yaw, int rotation) {
        if ((rotation & 1) != 0 || !Float.isFinite(yaw) || yaw % 90 != 0)
            return null;
        double angle = Math.toRadians(180 - yaw);
        double dx = view.x - centerX, dy = view.y - centerY, dz = view.z - centerZ;
        double x = Math.cos(angle) * dx - Math.sin(angle) * dz;
        double z = Math.sin(angle) * dx + Math.cos(angle) * dz - 0.4375;
        angle = Math.toRadians(rotation * 45);
        return new Vec3d((Math.cos(angle) * x + Math.sin(angle) * dy) * 2,
            (-Math.sin(angle) * x + Math.cos(angle) * dy) * 2, z * 2);
    }

    public static List<BakedQuad> filter(IBakedModel model, EnumFacing side, List<BakedQuad> quads) {
        if (camera == null || quads.isEmpty() || quads.size() > 1024
            || !supportedModel(model) || !model.isGui3d() || model.isBuiltInRenderer()
            || (model.getOverrides().getClass() != ItemOverrideList.class
                && model.getOverrides().getClass() != AnimationItemOverrideList.class)
            || !model.getOverrides().getOverrides().isEmpty())
            return quads;
        var transform = model.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.FIXED);
        if (transform.rotation.x != 0 || transform.rotation.z != 0 || !Float.isFinite(transform.rotation.y)
            || transform.rotation.y % 90 != 0 || transform.translation.x != 0
            || transform.translation.y != 0 || transform.translation.z != 0
            || !validScale(transform.scale.x) || !validScale(transform.scale.y) || !validScale(transform.scale.z))
            return quads;
        double angle = Math.toRadians(transform.rotation.y);
        double x = (Math.cos(angle) * camera.x - Math.sin(angle) * camera.z) / transform.scale.x;
        double y = camera.y / transform.scale.y;
        double z = (Math.sin(angle) * camera.x + Math.cos(angle) * camera.z) / transform.scale.z;
        int mask = axisMask(x, EnumFacing.WEST, EnumFacing.EAST)
            | axisMask(y, EnumFacing.DOWN, EnumFacing.UP) | axisMask(z, EnumFacing.NORTH, EnumFacing.SOUTH);
        // Only cull planar exterior quads. Interior, slanted and malformed geometry stays live.
        for (var quad : quads)
            if (!exteriorQuad(quad))
                return quads;
        var key = new Key(model, side, mask);
        var cached = CACHE.get(key);
        if (cached != null && cached.matches(quads))
            return cached.result;
        List<BakedQuad> result = new ArrayList<>(quads.size());
        for (var quad : quads)
            if ((mask & (1 << quad.getFace().ordinal())) != 0)
                result.add(quad);
        if (result.size() == quads.size())
            return quads;
        result = List.copyOf(result);
        CACHE.put(key, new Cached(quads.toArray(BakedQuad[]::new), result));
        return result;
    }

    private static boolean supportedModel(IBakedModel model) {
        if (model.getClass() == SimpleBakedModel.class)
            return true;
        if (!model.getClass().getName().equals("net.minecraftforge.client.model.ModelLoader$VanillaModelWrapper$1"))
            return false;
        // Forge can supply a separate perspective matrix. Only accept the ordinary JSON fixed transform.
        return FORGE_TRANSFORMS.computeIfAbsent(model, value -> {
            var actual = value.handlePerspective(ItemCameraTransforms.TransformType.FIXED);
            var expected = PerspectiveMapWrapper.handlePerspective(value,
                PerspectiveMapWrapper.getTransforms(value.getItemCameraTransforms()),
                ItemCameraTransforms.TransformType.FIXED);
            return actual.getLeft() == value && (actual.getRight() == null
                ? expected.getRight() == null
                : expected.getRight() != null && actual.getRight().epsilonEquals(expected.getRight(), 0.00001f));
        });
    }

    private static boolean validScale(float value) {
        return Float.isFinite(value) && value > 0 && value <= 1;
    }

    private static int axisMask(double coordinate, EnumFacing negative, EnumFacing positive) {
        if (!Double.isFinite(coordinate) || Math.abs(coordinate) <= 0.5001)
            return (1 << negative.ordinal()) | (1 << positive.ordinal());
        return 1 << (coordinate < 0 ? negative : positive).ordinal();
    }

    private static boolean exteriorQuad(BakedQuad quad) {
        if (!StaticItemGeometry.isStaticQuad(quad) || quad.getFace() == null
            || quad.getFormat() != DefaultVertexFormats.ITEM)
            return false;
        int[] data = quad.getVertexData();
        int stride = DefaultVertexFormats.ITEM.getIntegerSize();
        if (data.length != stride * 4)
            return false;
        int axis = switch (quad.getFace().getAxis()) {
            case X -> 0;
            case Y -> 1;
            case Z -> 2;
        };
        float plane = quad.getFace().getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 1 : 0;
        for (int vertex = 0; vertex < 4; vertex++) {
            for (int coordinate = 0; coordinate < 3; coordinate++) {
                float value = Float.intBitsToFloat(data[vertex * stride + coordinate]);
                if (!Float.isFinite(value) || value < 0 || value > 1
                    || coordinate == axis && Math.abs(value - plane) > 0.0001f)
                    return false;
            }
        }
        return true;
    }

    private record Key(IBakedModel model, EnumFacing side, int mask) {
    }

    private record Cached(BakedQuad[] source, List<BakedQuad> result) {

        boolean matches(List<BakedQuad> quads) {
            if (source.length != quads.size())
                return false;
            for (int i = 0; i < source.length; i++)
                if (source[i] != quads.get(i))
                    return false;
            return true;
        }

    }

}
