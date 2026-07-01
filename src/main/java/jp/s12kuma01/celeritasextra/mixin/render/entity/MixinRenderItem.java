package jp.s12kuma01.celeritasextra.mixin.render.entity;

import jp.s12kuma01.celeritasextra.client.ItemFrameLodState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Item-frame LOD (backport of MoreCulling's "Frame LOD") for the vanilla item render path.
 * <p>
 * Filters the held item model's quads by face in RenderItem.renderModel. The effect is scoped to
 * framed items via {@link ItemFrameLodState#active}, so all other item rendering (inventory, hand,
 * GUI, dropped items) passes through unchanged — the redirect just does a cheap flag read.
 * <p>
 * Note: with Forge's {@code allowEmissiveItems} enabled (the default) item rendering flows through
 * {@code ForgeHooksClient.renderLitItem} instead, which is covered by {@code MixinForgeHooksClient}.
 */
@Mixin(RenderItem.class)
public class MixinRenderItem {

    @Redirect(
            method = "renderModel(Lnet/minecraft/client/renderer/block/model/IBakedModel;ILnet/minecraft/item/ItemStack;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/model/IBakedModel;getQuads(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/EnumFacing;J)Ljava/util/List;")
    )
    private List<BakedQuad> celeritasExtra$lodItemFrameQuads(IBakedModel model, IBlockState state, EnumFacing side, long rand) {
        return ItemFrameLodState.filterLodQuads(model, state, side, rand);
    }
}
