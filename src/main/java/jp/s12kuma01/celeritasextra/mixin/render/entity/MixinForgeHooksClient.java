package jp.s12kuma01.celeritasextra.mixin.render.entity;

import jp.s12kuma01.celeritasextra.client.ItemFrameLodState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Item-frame LOD (backport of MoreCulling's "Frame LOD") for the Forge emissive-item render path.
 * <p>
 * {@code ForgeModContainer.allowEmissiveItems} defaults to true, so vanilla item rendering normally
 * flows through {@code ForgeHooksClient.renderLitItem} — which enumerates the same per-face
 * {@code IBakedModel.getQuads} calls as {@code RenderItem.renderModel} — rather than the vanilla loop.
 * Hooking it here (in addition to {@code MixinRenderItem}) makes the LOD effective regardless of the
 * emissive-items setting. Scoped to framed items via {@link ItemFrameLodState#active}.
 */
@Mixin(ForgeHooksClient.class)
public class MixinForgeHooksClient {

    @Redirect(
            method = "renderLitItem(Lnet/minecraft/client/renderer/RenderItem;Lnet/minecraft/client/renderer/block/model/IBakedModel;ILnet/minecraft/item/ItemStack;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/model/IBakedModel;getQuads(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/EnumFacing;J)Ljava/util/List;")
    )
    private static List<BakedQuad> celeritasExtra$lodItemFrameQuads(IBakedModel model, IBlockState state, EnumFacing side, long rand) {
        return ItemFrameLodState.filterLodQuads(model, state, side, rand);
    }
}
