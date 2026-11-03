package com.sumirelabs.celeritasextra.client;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

/** Bounded CPU-side vertex reuse; draw calls, lighting segments and GL state stay with Forge. */
public final class StaticItemVertexCache {

    public static final StaticItemVertexCache INSTANCE = new StaticItemVertexCache(256, 4 * 1024 * 1024);
    private final int maxEntries, maxBytes;
    private final Map<Key, int[]> entries = new LinkedHashMap<>(32, 0.75f, true);
    private final Key probe = new Key();
    private int retainedBytes;

    StaticItemVertexCache(int maxEntries, int maxBytes) {
        this.maxEntries = maxEntries;
        this.maxBytes = maxBytes;
    }

    public boolean canCache(IBakedModel model, List<BakedQuad> quads, int color, boolean noStackTint) {
        return eligible(model, quads, color, noStackTint);
    }

    public int[] get(IBakedModel model, List<BakedQuad> quads, int color, boolean noStackTint) {
        if (!eligible(model, quads, color, noStackTint))
            return null;
        return getPrepared(model, quads, color);
    }

    /** Lookup after canCache succeeded; avoids repeating the geometry checks on every hit. */
    public int[] getPrepared(IBakedModel model, List<BakedQuad> quads, int color) {
        probe.set(model, quads, color);
        return entries.get(probe);
    }

    public void put(IBakedModel model, List<BakedQuad> quads, int color, boolean noStackTint, int[] vertices) {
        if (!eligible(model, quads, color, noStackTint))
            return;
        int sourceInts = 0;
        for (var quad : quads)
            sourceInts += quad.getVertexData().length;
        long cost = ((long) vertices.length + sourceInts) * Integer.BYTES;
        if (cost > maxBytes)
            return;
        probe.set(model, quads, color);
        // A nested render may have populated the same entry while the caller emitted vertices.
        if (entries.containsKey(probe))
            return;
        while (!entries.isEmpty() && (entries.size() >= maxEntries || retainedBytes + cost > maxBytes)) {
            Iterator<Map.Entry<Key, int[]>> iterator = entries.entrySet().iterator();
            var oldest = iterator.next();
            retainedBytes -= oldest.getKey().sourceBytes + oldest.getValue().length * Integer.BYTES;
            iterator.remove();
        }
        Key key = probe.snapshot();
        entries.put(key, vertices);
        retainedBytes += (int) cost;
    }

    public void clear() {
        entries.clear();
        retainedBytes = 0;
        probe.release();
    }

    public static int[] capture(BufferBuilder buffer, int firstVertex) {
        int stride = buffer.getVertexFormat().getIntegerSize();
        int[] data = new int[(buffer.getVertexCount() - firstVertex) * stride];
        var bytes = buffer.getByteBuffer();
        // The uploader leaves ByteBuffer.position at a vertex attribute offset after a draw.
        // Read absolute vertex offsets without changing the live buffer's position/limit.
        bytes.duplicate().order(bytes.order()).clear().asIntBuffer().position(firstVertex * stride).get(data);
        return data;
    }

    private static boolean eligible(IBakedModel model, List<BakedQuad> quads, int color, boolean noStackTint) {
        if (!StaticItemGeometry.isStaticModel(model) || quads.isEmpty() || quads.size() > 1024)
            return false;
        for (var quad : quads) {
            if (!StaticItemGeometry.isStaticQuad(quad) || quad.getFormat() != DefaultVertexFormats.ITEM
                || quad.getVertexData().length != DefaultVertexFormats.ITEM.getIntegerSize() * 4
                || color == -1 && !noStackTint && quad.hasTintIndex())
                return false;
            var sprite = quad.getSprite();
            // Do not bypass renderer sprite-activation hooks or dynamic UV updates.
            if (sprite != null && (sprite.getClass() != TextureAtlasSprite.class || sprite.hasAnimationMetadata()))
                return false;
        }
        return true;
    }

    private static final class Key {

        IBakedModel model;
        List<BakedQuad> quads;
        int[][] sourceData;
        int color, hash, sourceBytes;
        void set(IBakedModel model, List<BakedQuad> quads, int color) {
            this.model = model;
            this.quads = quads;
            this.color = color;
            hash = 31 * System.identityHashCode(model) + color;
            for (var quad : quads)
                hash = 31 * hash + System.identityHashCode(quad) + Arrays.hashCode(quad.getVertexData());
        }
        Key snapshot() {
            Key key = new Key();
            key.model = model;
            key.color = color;
            key.hash = hash;
            key.quads = List.copyOf(quads);
            key.sourceData = new int[quads.size()][];
            for (int i = 0; i < quads.size(); i++) {
                key.sourceData[i] = quads.get(i).getVertexData().clone();
                key.sourceBytes += key.sourceData[i].length * Integer.BYTES;
            }
            return key;
        }
        int[] data(int index) {
            return sourceData == null ? quads.get(index).getVertexData() : sourceData[index];
        }
        void release() {
            model = null;
            quads = null;
        }
        @Override
        public int hashCode() {
            return hash;
        }
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Key key) || model != key.model || color != key.color
                || quads.size() != key.quads.size())
                return false;
            for (int i = 0; i < quads.size(); i++) {
                if (quads.get(i) != key.quads.get(i) || !Arrays.equals(data(i), key.data(i)))
                    return false;
            }
            return true;
        }

    }

}
