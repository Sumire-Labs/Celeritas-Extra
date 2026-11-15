/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Vector3f;

import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import static org.junit.jupiter.api.Assertions.*;

class ItemFrameThreeFaceStateTest {

    @AfterEach
    void reset() {
        ItemFrameThreeFaceState.camera = null;
        ItemFrameThreeFaceState.clearCache();
    }

    @Test
    void octantsSelectOnlyTheThreeVisibleFacesAndReuseGeometry() {
        var quads = new ArrayList<>(Arrays.stream(EnumFacing.values()).map(this::quad).toList());
        var model = model(quads, ItemTransformVec3f.DEFAULT);
        for (int x : new int[]{-1, 1})
            for (int y : new int[]{-1, 1})
                for (int z : new int[]{-1, 1}) {
                    ItemFrameThreeFaceState.camera = new Vec3d(x * 10, y * 10, z * 10);
                    var result = ItemFrameThreeFaceState.filter(model, null, quads);
                    assertEquals(3, result.size());
                    assertTrue(
                        result.stream().anyMatch(q -> q.getFace() == (x < 0 ? EnumFacing.WEST : EnumFacing.EAST)));
                    assertTrue(result.stream().anyMatch(q -> q.getFace() == (y < 0 ? EnumFacing.DOWN : EnumFacing.UP)));
                    assertTrue(
                        result.stream().anyMatch(q -> q.getFace() == (z < 0 ? EnumFacing.NORTH : EnumFacing.SOUTH)));
                    assertSame(result, ItemFrameThreeFaceState.filter(model, null, quads));
                }
        assertEquals(6, quads.size());
        quads.set(EnumFacing.SOUTH.ordinal(), quad(EnumFacing.SOUTH));
        var changed = ItemFrameThreeFaceState.filter(model, null, quads);
        assertSame(quads.get(EnumFacing.SOUTH.ordinal()), changed.stream()
            .filter(q -> q.getFace() == EnumFacing.SOUTH).findFirst().orElseThrow());
    }

    @Test
    void cameraInsideAnAxisExtentKeepsBothFacesAndDisabledStateDoesNothing() {
        var quads = Arrays.stream(EnumFacing.values()).map(this::quad).toList();
        var model = model(quads, ItemTransformVec3f.DEFAULT);
        assertSame(quads, ItemFrameThreeFaceState.filter(model, null, quads));
        ItemFrameThreeFaceState.camera = new Vec3d(0, 0, 10);
        assertEquals(5, ItemFrameThreeFaceState.filter(model, null, quads).size());
        ItemFrameThreeFaceState.camera = Vec3d.ZERO;
        assertSame(quads, ItemFrameThreeFaceState.filter(model, null, quads));
    }

    @Test
    void specialTransformsAndInteriorGeometryKeepOriginalRendering() {
        var quads = Arrays.stream(EnumFacing.values()).map(this::quad).toList();
        ItemFrameThreeFaceState.camera = new Vec3d(10, 10, 10);
        for (var transform : List.of(
            transform(45, 1, 0), transform(0, -1, 0), transform(0, 0, 0), transform(0, 1, 0.1f)))
            assertSame(quads, ItemFrameThreeFaceState.filter(model(quads, transform), null, quads));
        var interior = new ArrayList<>(quads);
        var data = quad(EnumFacing.SOUTH).getVertexData().clone();
        data[2] = Float.floatToRawIntBits(0.5f);
        interior.add(new BakedQuad(data, -1, EnumFacing.SOUTH, null, true, DefaultVertexFormats.ITEM));
        assertSame(interior,
            ItemFrameThreeFaceState.filter(model(interior, ItemTransformVec3f.DEFAULT), null, interior));
    }

    @Test
    void vanillaFixedHalfScaleAndHalfTurnSelectOppositeDepthFace() {
        var quads = Arrays.stream(EnumFacing.values()).map(this::quad).toList();
        ItemFrameThreeFaceState.camera = new Vec3d(10, 10, 10);
        var result = ItemFrameThreeFaceState.filter(model(quads, transform(180, 0.5f, 0)), null, quads);
        assertEquals(List.of(EnumFacing.UP, EnumFacing.NORTH, EnumFacing.WEST),
            result.stream().map(BakedQuad::getFace).toList());
    }

    @Test
    void frameRotationsInvertTheActualRenderTransform() {
        for (int yaw = 0; yaw < 360; yaw += 90) {
            for (int rotation = 0; rotation < 8; rotation += 2) {
                double ry = Math.toRadians(180 - yaw), rz = Math.toRadians(rotation * 45);
                double x = 3, y = 4, z = 5;
                double frameX = (Math.cos(rz) * x - Math.sin(rz) * y) * 0.5;
                double frameY = (Math.sin(rz) * x + Math.cos(rz) * y) * 0.5;
                double frameZ = z * 0.5 + 0.4375;
                var view = new Vec3d(10 + Math.cos(ry) * frameX + Math.sin(ry) * frameZ,
                    20 + frameY, 30 - Math.sin(ry) * frameX + Math.cos(ry) * frameZ);
                var actual = ItemFrameThreeFaceState.cameraInFrame(view, 10, 20, 30, yaw, rotation);
                assertEquals(x, actual.x, 0.00001);
                assertEquals(y, actual.y, 0.00001);
                assertEquals(z, actual.z, 0.00001);
            }
        }
        assertNull(ItemFrameThreeFaceState.cameraInFrame(Vec3d.ZERO, 0, 0, 0, 0, 1));
    }

    private ItemTransformVec3f transform(float yaw, float scale, float translation) {
        return new ItemTransformVec3f(new Vector3f(0, yaw, 0), new Vector3f(translation, 0, 0),
            new Vector3f(scale, scale, scale));
    }

    private IBakedModel model(List<BakedQuad> quads, ItemTransformVec3f fixed) {
        var faces = new EnumMap<EnumFacing, List<BakedQuad>>(EnumFacing.class);
        for (var face : EnumFacing.values())
            faces.put(face, List.of());
        var none = ItemTransformVec3f.DEFAULT;
        return new SimpleBakedModel(quads, faces, false, true, null,
            new ItemCameraTransforms(none, none, none, none, none, none, none, fixed), new ItemOverrideList(List.of()));
    }

    private BakedQuad quad(EnumFacing face) {
        int[] data = new int[DefaultVertexFormats.ITEM.getIntegerSize() * 4];
        int axis = switch (face.getAxis()) {
            case X -> 0;
            case Y -> 1;
            case Z -> 2;
        };
        for (int vertex = 0; vertex < 4; vertex++) {
            float[] point = {0, 0, 0};
            point[axis] = face.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 1 : 0;
            point[(axis + 1) % 3] = vertex < 2 ? 0 : 1;
            point[(axis + 2) % 3] = vertex == 0 || vertex == 3 ? 0 : 1;
            for (int coordinate = 0; coordinate < 3; coordinate++)
                data[vertex * 7 + coordinate] = Float.floatToRawIntBits(point[coordinate]);
        }
        return new BakedQuad(data, -1, face, null, true, DefaultVertexFormats.ITEM);
    }

}
