/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client;

import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.util.vector.Vector3f;

import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;

import static org.junit.jupiter.api.Assertions.*;

class ItemFrameThreeFaceStateTest {

    @Test
    void actualForgePerspectiveRotationDisablesTheUnrotatedBranch() {
        var quads = Arrays.stream(EnumFacing.values()).map(this::quad).toList();
        var model = model(quads, ItemTransformVec3f.DEFAULT);
        ItemFrameThreeFaceState.context = frame(0, 1, 1, 1, 10, 4);
        var matrix = new javax.vecmath.Matrix4f();
        matrix.rotY((float) Math.PI / 4);
        ItemFrameThreeFaceState.observePerspective(matrix, ItemTransformVec3f.DEFAULT);
        assertSame(quads, ItemFrameThreeFaceState.filter(model, null, quads));
        matrix.setIdentity();
        ItemFrameThreeFaceState.observePerspective(matrix, ItemTransformVec3f.DEFAULT);
        assertEquals(3, ItemFrameThreeFaceState.filter(model, null, quads).size());
    }

    @AfterEach
    void reset() {
        ItemFrameThreeFaceState.context = null;
        ItemFrameLodState.active = false;
        ItemFrameLodState.clearCache();
    }
    private ItemFrameThreeFaceState.Context frame(int rotation, double x, double y, double z, double distance,
        int range) {
        return new ItemFrameThreeFaceState.Context(EnumFacing.NORTH, rotation, x, y, z, distance, range);
    }
    @Test
    void allOctantsKeepThreeDirectionsAndReuseFilteredLists() {
        var source = new ArrayList<>(Arrays.stream(EnumFacing.values()).map(this::quad).toList());
        var model = model(source, ItemTransformVec3f.DEFAULT);
        for (int x : new int[]{-1, 1})
            for (int y : new int[]{-1, 1})
                for (int z : new int[]{-1, 1}) {
                    ItemFrameThreeFaceState.context = frame(0, x, y, z, 10, 4);
                    var result = ItemFrameThreeFaceState.filter(model, null, source);
                    assertEquals(3, result.size());
                    assertTrue(
                        result.stream().anyMatch(q -> q.getFace() == (x > 0 ? EnumFacing.EAST : EnumFacing.WEST)));
                    assertTrue(result.stream().anyMatch(q -> q.getFace() == (y > 0 ? EnumFacing.UP : EnumFacing.DOWN)));
                    assertTrue(
                        result.stream().anyMatch(q -> q.getFace() == (z > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH)));
                    assertSame(result, ItemFrameThreeFaceState.filter(model, null, source));
                }
        var replacement = quad(EnumFacing.SOUTH);
        source.set(EnumFacing.SOUTH.ordinal(), replacement);
        assertTrue(ItemFrameThreeFaceState.filter(model, null, source).contains(replacement));
    }
    @Test
    void matchesUpstreamDistanceAndRotationGates() {
        var quads = Arrays.stream(EnumFacing.values()).map(this::quad).toList();
        var model = model(quads, ItemTransformVec3f.DEFAULT);
        assertSame(quads, ItemFrameThreeFaceState.filter(model, null, quads));
        for (var context : List.of(frame(0, 1, 1, 1, 3, 2), frame(0, 1, 1, 1, 4, 4), frame(1, 1, 1, 1, 10, 4))) {
            ItemFrameThreeFaceState.context = context;
            assertSame(quads, ItemFrameThreeFaceState.filter(model, null, quads));
        }
        ItemFrameThreeFaceState.context = frame(2, 1, 1, 1, 10, 4);
        assertEquals(3, ItemFrameThreeFaceState.filter(model, null, quads).size());
        var rotated = new ItemTransformVec3f(new Vector3f(0, 180, 0), new Vector3f(), new Vector3f(1, 1, 1));
        assertSame(quads, ItemFrameThreeFaceState.filter(model(quads, rotated), null, quads));
    }
    @Test
    void upstreamBranchFiltersByFaceWithoutOpaqueCubeOrExteriorPlaneRestrictions() {
        var quads = Arrays.stream(EnumFacing.values()).map(this::quad).toList();
        var unusual = new ItemTransformVec3f(new Vector3f(), new Vector3f(0.2f, 0, 0), new Vector3f(1.2f, 1.2f, 1.2f));
        ItemFrameThreeFaceState.context = frame(0, 0, 0, 10, 10, 4);
        assertEquals(List.of(EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.WEST),
            ItemFrameThreeFaceState.filter(model(quads, unusual), null, quads).stream().map(BakedQuad::getFace)
                .toList());
    }
    @Test
    void flatItemLodTakesPriorityOverThreeFaceFiltering() {
        var quads = Arrays.stream(EnumFacing.values()).map(this::quad).toList();
        var faces = new EnumMap<EnumFacing, List<BakedQuad>>(EnumFacing.class);
        for (var face : EnumFacing.values())
            faces.put(face, List.of());
        var flat = new SimpleBakedModel(quads, faces, false, false, null, ItemCameraTransforms.DEFAULT,
            ItemOverrideList.NONE);
        ItemFrameThreeFaceState.context = frame(0, 1, 1, 1, 100, 4);
        ItemFrameLodState.active = true;
        assertEquals(List.of(EnumFacing.SOUTH),
            ItemFrameLodState.filterLodQuads(flat, null, null, 0).stream().map(BakedQuad::getFace).toList());
    }
    @Test
    void everyFacingAndQuarterTurnProducesThreeDirections() {
        for (var facing : EnumFacing.values())
            for (int rotation = 0; rotation < 8; rotation += 2) {
                var context = new ItemFrameThreeFaceState.Context(facing, rotation, 1, -1, 1, 10, 4);
                assertEquals(3, Integer.bitCount(ItemFrameThreeFaceState.faceMask(context)));
            }
    }
    private IBakedModel model(List<BakedQuad> quads, ItemTransformVec3f fixed) {
        var faces = new EnumMap<EnumFacing, List<BakedQuad>>(EnumFacing.class);
        for (var face : EnumFacing.values())
            faces.put(face, List.of());
        var none = ItemTransformVec3f.DEFAULT;
        return new SimpleBakedModel(quads, faces, false, true, null,
            new ItemCameraTransforms(none, none, none, none, none, none, none, fixed), ItemOverrideList.NONE);
    }
    private BakedQuad quad(EnumFacing face) {
        return new BakedQuad(new int[28], -1, face, null, true, DefaultVertexFormats.ITEM);
    }

}
