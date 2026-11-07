package com.sumirelabs.celeritasextra.mixin.render.entity;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.sumirelabs.celeritasextra.client.ItemFrameLodState;
import com.sumirelabs.celeritasextra.client.StaticItemSegmentRenderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;

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

    @Redirect(method = "renderLitItem(Lnet/minecraft/client/renderer/RenderItem;Lnet/minecraft/client/renderer/block/model/IBakedModel;ILnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/IBakedModel;getQuads(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/EnumFacing;J)Ljava/util/List;"))
    private static List<BakedQuad> celeritasExtra$lodItemFrameQuads(IBakedModel model, IBlockState state,
        EnumFacing side, long rand) {
        return ItemFrameLodState.filterLodQuads(model, state, side, rand);
    }

    @WrapOperation(method = "drawSegment", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderItem;renderQuads(Lnet/minecraft/client/renderer/BufferBuilder;Ljava/util/List;ILnet/minecraft/item/ItemStack;)V"))
    private static void celeritasExtra$drawResidentSegment(RenderItem renderer, BufferBuilder buffer,
        List<BakedQuad> quads, int color, ItemStack stack, Operation<Void> original,
        @Share("residentItemSegment") LocalBooleanRef drawn) {
        if (renderer instanceof StaticItemSegmentRenderer resident
            && resident.celeritasExtra$drawCachedSegment(buffer, quads, color, stack)) {
            drawn.set(true);
            return;
        }
        original.call(renderer, buffer, quads, color, stack);
    }

    @WrapOperation(method = "drawSegment", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;draw()V"))
    private static void celeritasExtra$skipCompletedDraw(Tessellator tessellator, Operation<Void> original,
        @Share("residentItemSegment") LocalBooleanRef drawn) {
        if (!drawn.get())
            original.call(tessellator);
    }

}
