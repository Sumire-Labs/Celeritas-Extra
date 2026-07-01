package jp.s12kuma01.celeritasextra.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;

import java.util.Collections;
import java.util.List;

/**
 * Shared render-thread state for the item-frame LOD feature (backport of MoreCulling's "Frame LOD").
 * <p>
 * While a framed item is farther than the configured distance, {@link #active} is set for the duration
 * of that frame's content render. The RenderItem / ForgeHooksClient mixins consult
 * {@link #filterLodQuads} to drop the four side faces of the held 3D (block) model, keeping only the
 * front/back faces the viewer can actually see through the frame — this cuts the vertices submitted for
 * far-away framed block items. Flat sprite items have nothing to cull and are left untouched; maps are
 * handled separately (culled at the same distance in MixinRenderItemFrame).
 * <p>
 * Client render thread only, so a plain static flag is safe without synchronization.
 */
public final class ItemFrameLodState {

    /** True only while rendering the content of a framed item that is beyond the LOD distance. */
    public static boolean active = false;

    private ItemFrameLodState() {
    }

    /**
     * Returns the model's quads for the given face, dropping the four side faces (EAST/WEST/UP/DOWN)
     * when item-frame LOD is active for a 3D (block) model. The general (null) bucket and the
     * front/back (NORTH/SOUTH) faces are always kept; flat sprite items are returned unchanged.
     * <p>
     * NORTH/SOUTH is the front/back axis for a framed item: vanilla block models use a {@code fixed}
     * display transform with zero rotation, so a framed block's viewer-facing face is its model-local
     * SOUTH face (and the away face NORTH). This matches MoreCulling's SOUTH_NORTH selection.
     */
    public static List<BakedQuad> filterLodQuads(IBakedModel model, IBlockState state, EnumFacing side, long rand) {
        List<BakedQuad> quads = model.getQuads(state, side, rand);
        if (!active || side == null || !model.isGui3d()) {
            return quads;
        }
        if (side == EnumFacing.NORTH || side == EnumFacing.SOUTH) {
            return quads;
        }
        return Collections.emptyList();
    }
}
