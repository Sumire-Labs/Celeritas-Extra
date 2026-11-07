package com.sumirelabs.celeritasextra.client;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

import static org.junit.jupiter.api.Assertions.*;

class StaticItemVertexCacheTest {

    @Test
    void forgeGeneratedItemsAndUnpackedQuadsReuseTheirActualEmittedBytes() {
        var format = DefaultVertexFormats.ITEM;
        var quad = new UnpackedBakedQuad(new float[4][format.getElementCount()][4], -1,
            EnumFacing.SOUTH, null, true, format);
        var quads = List.<BakedQuad>of(quad);
        var model = new BakedItemModel(ImmutableList.copyOf(quads), null, ImmutableMap.of(), ItemOverrideList.NONE,
            false);
        var cache = new StaticItemVertexCache(8, 4096);
        var buffer = new BufferBuilder(256);
        buffer.begin(7, format);
        LightUtil.renderQuadColor(buffer, quad, -1);
        int[] original = StaticItemVertexCache.capture(buffer, 0);
        buffer.finishDrawing();
        cache.put(model, quads, -1, true, original);
        assertSame(original, cache.get(model, quads, -1, true));
        buffer.begin(7, format);
        buffer.addVertexData(cache.get(model, quads, -1, true));
        assertArrayEquals(original, StaticItemVertexCache.capture(buffer, 0));
        buffer.finishDrawing();
    }
    private BakedQuad quad(int tint) {
        int[] data = new int[28];
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * 7;
            data[offset] = Float.floatToRawIntBits(vertex % 2);
            data[offset + 1] = Float.floatToRawIntBits(vertex / 2);
            data[offset + 3] = -1;
            data[offset + 6] = 0x007f0000;
        }
        return new BakedQuad(data, tint, EnumFacing.SOUTH, null, true, DefaultVertexFormats.ITEM);
    }

    private SimpleBakedModel model(List<BakedQuad> quads) {
        var faces = new EnumMap<EnumFacing, List<BakedQuad>>(EnumFacing.class);
        for (var face : EnumFacing.values())
            faces.put(face, List.of());
        return new SimpleBakedModel(quads, faces, false, false, null, ItemCameraTransforms.DEFAULT,
            ItemOverrideList.NONE);
    }

    @Test
    void replayPreservesForgeVertexBytesAndDoesNotCaptureEarlierVertices() {
        var quads = List.of(quad(-1), quad(-1));
        var model = model(quads);
        var cache = new StaticItemVertexCache(8, 4096);
        var buffer = new BufferBuilder(256);
        buffer.begin(7, DefaultVertexFormats.ITEM);
        LightUtil.renderQuadColor(buffer, quads.get(0), -1);
        int start = buffer.getVertexCount();
        for (var quad : quads)
            LightUtil.renderQuadColor(buffer, quad, 0xff804020);
        buffer.getByteBuffer().position(24).limit(64);
        int[] expected = StaticItemVertexCache.capture(buffer, start);
        assertEquals(24, buffer.getByteBuffer().position());
        assertEquals(64, buffer.getByteBuffer().limit());
        assertEquals(2 * 4 * DefaultVertexFormats.ITEM.getIntegerSize(), expected.length);
        cache.put(model, quads, 0xff804020, false, expected);
        buffer.finishDrawing();
        buffer.begin(7, DefaultVertexFormats.ITEM);
        buffer.addVertexData(cache.get(model, quads, 0xff804020, false));
        assertArrayEquals(expected, StaticItemVertexCache.capture(buffer, 0));
        assertEquals(8, buffer.getVertexCount());
        buffer.finishDrawing();
    }

    @Test
    void rebuiltListsHitButTintAndMutatedGeometryDoNotReuseOldBytes() {
        var quad = quad(-1);
        var quads = List.of(quad);
        var model = model(quads);
        var cache = new StaticItemVertexCache(8, 4096);
        int[] vertices = new int[28];
        cache.put(model, quads, -1, false, vertices);
        assertSame(vertices, cache.get(model, new ArrayList<>(quads), -1, false));
        assertNull(cache.get(model, quads, 0xff00ff00, false));
        quad.getVertexData()[0] = Float.floatToRawIntBits(2);
        assertNull(cache.get(model, quads, -1, false));
        var tinted = List.of(quad(0));
        var tintedModel = model(tinted);
        cache.put(tintedModel, tinted, -1, false, vertices);
        assertNull(cache.get(tintedModel, tinted, -1, false));
        // A generated layer with no handler can be white-cached; registering a handler must bypass it.
        cache.put(tintedModel, tinted, -1, true, vertices);
        assertSame(vertices, cache.get(tintedModel, tinted, -1, true));
        assertNull(cache.get(tintedModel, tinted, -1, false));
    }

    @Test
    void capacityAndByteLimitsEvictAndReloadClearsEntries() {
        var quads = List.of(quad(-1));
        var first = model(quads);
        var second = model(quads);
        var third = model(quads);
        int[] vertices = new int[28];
        var cache = new StaticItemVertexCache(2, 4096);
        cache.put(first, quads, -1, true, vertices);
        cache.put(second, quads, -1, true, vertices);
        assertNotNull(cache.get(first, quads, -1, true));
        cache.put(third, quads, -1, true, vertices);
        assertNull(cache.get(second, quads, -1, true));
        cache.clear();
        assertNull(cache.get(first, quads, -1, true));
        // CPU vertices and their key would fit, but not with the reserved GPU copy.
        var tiny = new StaticItemVertexCache(8, 300);
        tiny.put(first, quads, -1, true, vertices);
        assertNull(tiny.get(first, quads, -1, true));
    }

    @Test
    void customQuadImplementationsAreNotFrozen() {
        var custom = new BakedQuad(new int[28], -1, EnumFacing.SOUTH, null, true, DefaultVertexFormats.ITEM) {
        };
        var quads = List.<BakedQuad>of(custom);
        var cache = new StaticItemVertexCache(8, 4096);
        var model = model(quads);
        cache.put(model, quads, -1, true, new int[28]);
        assertNull(cache.get(model, quads, -1, true));
    }

}
