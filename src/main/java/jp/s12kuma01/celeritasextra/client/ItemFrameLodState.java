package jp.s12kuma01.celeritasextra.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Render-thread state scoped to the vanilla item call inside an item frame. */
public final class ItemFrameLodState {
    public static boolean active;

    private ItemFrameLodState() {
    }

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
        return filtered == null ? quads : filtered.isEmpty() ? Collections.emptyList() : filtered;
    }
}
