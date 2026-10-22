package com.sumirelabs.celeritasextra.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/** Render-thread state scoped to the vanilla item call inside an item frame. */
public final class ItemFrameLodState {
    public static boolean active;
    private static final int MAX_MODELS = 128, MAX_QUADS = 1024;
    private static final Map<IBakedModel, CachedBucket[]> CACHE = new LinkedHashMap<>(32, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<IBakedModel, CachedBucket[]> entry) {
            return size() > MAX_MODELS;
        }
    };

    private ItemFrameLodState() {
    }

    public static void clearCache() { CACHE.clear(); }

    /**
     * MoreCulling's distance LOD targets non-block items: remove sprite edge faces and,
     * when the fixed transform fits against the frame backing, the hidden back face.
     * Generated items store these faces in the general (null) bucket, so inspecting
     * BakedQuad#getFace is essential. 3D blocks belong to the separate 3-face feature.
     */
    public static List<BakedQuad> filterLodQuads(IBakedModel model, IBlockState state, EnumFacing side, long rand) {
        List<BakedQuad> quads = model.getQuads(state, side, rand);
        if (!active || model.isGui3d() || model.isBuiltInRenderer() || quads.isEmpty()) {
            return quads;
        }

        ItemTransformVec3f transform = model.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.FIXED);
        // Unusual resource-pack/mod transforms retain their full geometry when the
        // depth axis no longer lies perpendicular to the frame.
        if (transform.rotation.x % 180 != 0 || transform.rotation.y % 180 != 0
                || transform.scale.x == 0 || transform.scale.y == 0 || transform.scale.z == 0) {
            return quads;
        }
        boolean cullBack = transform.translation.x == 0 && transform.translation.y == 0 && transform.translation.z == 0
                && Math.abs(transform.scale.x) <= 1 && Math.abs(transform.scale.y) <= 1 && Math.abs(transform.scale.z) <= 1;
        boolean flipped = ((int) (transform.rotation.x / 180) % 2 != 0)
                ^ ((int) (transform.rotation.y / 180) % 2 != 0) ^ (transform.scale.z < 0);
        EnumFacing front = flipped ? EnumFacing.NORTH : EnumFacing.SOUTH;
        int policy = (cullBack ? 1 : 0) | (flipped ? 2 : 0);
        boolean cacheable = StaticItemGeometry.isStaticModel(model) && state == null && quads.size() <= MAX_QUADS;
        int bucket = side == null ? 6 : side.ordinal();
        CachedBucket[] buckets = cacheable ? CACHE.get(model) : null;
        CachedBucket cached = buckets == null ? null : buckets[bucket];
        if (cached != null && cached.matches(quads, policy)) return cached.result;

        List<BakedQuad> filtered = null;
        for (int i = 0; i < quads.size(); i++) {
            BakedQuad quad = quads.get(i);
            EnumFacing face = quad.getFace();
            boolean keep = face == front || (!cullBack && (face == EnumFacing.NORTH || face == EnumFacing.SOUTH));
            if (!keep && filtered == null) {
                filtered = new ArrayList<>(quads.size());
                filtered.addAll(quads.subList(0, i));
            } else if (keep && filtered != null) {
                filtered.add(quad);
            }
        }
        List<BakedQuad> result = filtered == null ? quads : filtered.isEmpty() ? Collections.emptyList() : filtered;
        if (cacheable && quads.stream().allMatch(StaticItemGeometry::isStaticQuad)) {
            if (buckets == null) {
                buckets = new CachedBucket[7];
                CACHE.put(model, buckets);
            }
            buckets[bucket] = new CachedBucket(quads, policy, result);
        }
        return result;
    }

    private static final class CachedBucket {
        final List<BakedQuad> source, result;
        final BakedQuad[] snapshot;
        final int policy;
        CachedBucket(List<BakedQuad> source, int policy, List<BakedQuad> result) {
            this.source = source;
            this.policy = policy;
            this.result = result;
            snapshot = source.toArray(BakedQuad[]::new);
        }
        boolean matches(List<BakedQuad> quads, int policy) {
            if (this.policy != policy || source != quads || snapshot.length != quads.size()) return false;
            // Detect in-place list edits too, without allocating a new filtered list.
            for (int i = 0; i < snapshot.length; i++) if (snapshot[i] != quads.get(i)) return false;
            return true;
        }
    }
}
