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

import javax.vecmath.Matrix4f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.util.EnumFacing;

/** MoreCulling's three-direction selection, connected to the 1.12.2 item render scope. */
public final class ItemFrameThreeFaceState {

    public record Context(EnumFacing facing, int rotation, double x, double y, double z, double distance, int range,
        boolean unrotated) {

        public Context(EnumFacing facing, int rotation, double x, double y, double z, double distance, int range) {
            this(facing, rotation, x, y, z, distance, range, true);
        }

    }

    public static Context context;
    private static final Map<Key, Cached> CACHE = new LinkedHashMap<>(32, 0.75f, true) {

        @Override
        protected boolean removeEldestEntry(Map.Entry<Key, Cached> entry) {
            return size() > 256;
        }

    };

    private ItemFrameThreeFaceState() {}

    public static void clearCache() {
        CACHE.clear();
    }

    public static List<BakedQuad> filter(IBakedModel model, EnumFacing side, List<BakedQuad> quads) {
        Context frame = context;
        if (frame == null || frame.facing() == null || !frame.unrotated() || quads.isEmpty()
            || model.isGui3d() && frame.distance() <= 3
            || frame.distance() <= frame.range() || (frame.rotation() & 1) != 0)
            return quads;
        var transform = model.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.FIXED);
        // Upstream's experimental 3-face branch accepts only unrotated model transforms.
        if (transform.rotation.x != 0 || transform.rotation.y != 0 || transform.rotation.z != 0)
            return quads;
        int mask = faceMask(frame);
        var key = new Key(model, side, mask);
        var cached = CACHE.get(key);
        if (cached != null && cached.matches(quads))
            return cached.result;
        List<BakedQuad> result = new ArrayList<>(quads.size());
        for (var quad : quads) {
            if (quad.getFace() == null)
                return quads;
            if ((mask & (1 << quad.getFace().ordinal())) != 0)
                result.add(quad);
        }
        if (result.size() == quads.size())
            return quads;
        result = List.copyOf(result);
        if (quads.size() <= 1024)
            CACHE.put(key, new Cached(quads.toArray(BakedQuad[]::new), result));
        return result;
    }

    /** Reads the actual Forge perspective, as the modern renderer reads its layer transform. */
    public static void observePerspective(Matrix4f matrix, ItemTransformVec3f fixed) {
        Context frame = context;
        if (frame == null)
            return;
        boolean aligned = matrix == null || (Math.abs(matrix.m01) < 0.00001f && Math.abs(matrix.m02) < 0.00001f
            && Math.abs(matrix.m10) < 0.00001f && Math.abs(matrix.m12) < 0.00001f
            && Math.abs(matrix.m20) < 0.00001f && Math.abs(matrix.m21) < 0.00001f
            && matrix.m00 * fixed.scale.x >= 0 && matrix.m11 * fixed.scale.y >= 0 && matrix.m22 * fixed.scale.z >= 0);
        context = new Context(frame.facing(), frame.rotation(), frame.x(), frame.y(), frame.z(), frame.distance(),
            frame.range(), aligned);
    }

    public static int faceMask(Context frame) {
        return bit(frame.facing(), frame.rotation(), frame.x() > 0 ? EnumFacing.EAST : EnumFacing.WEST)
            | bit(frame.facing(), frame.rotation(), frame.y() > 0 ? EnumFacing.UP : EnumFacing.DOWN)
            | bit(frame.facing(), frame.rotation(), frame.z() > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH);
    }

    private static int bit(EnumFacing facing, int rotation, EnumFacing worldFace) {
        var v = worldFace.getDirectionVec();
        int x = v.getX(), y = v.getY(), z = v.getZ();
        int a, b, c;
        switch (facing) {
            case NORTH -> {
                a = x;
                b = y;
                c = z;
            }
            case SOUTH -> {
                a = -x;
                b = y;
                c = -z;
            }
            case WEST -> {
                a = -z;
                b = y;
                c = x;
            }
            case EAST -> {
                a = z;
                b = y;
                c = -x;
            }
            case DOWN -> {
                a = -x;
                b = z;
                c = y;
            }
            case UP -> {
                a = -x;
                b = -z;
                c = -y;
            }
            default -> throw new IllegalArgumentException("Unknown frame facing");
        }
        for (int i = 0; i < Math.floorMod(rotation / 2, 4); i++) {
            int old = a;
            a = b;
            b = -old;
        }
        return 1 << EnumFacing.getFacingFromVector(a, b, c).ordinal();
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
