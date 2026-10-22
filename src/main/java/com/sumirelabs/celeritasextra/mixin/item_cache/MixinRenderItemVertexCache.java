package com.sumirelabs.celeritasextra.mixin.item_cache;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod;
import com.sumirelabs.celeritasextra.client.StaticItemVertexCache;
import com.sumirelabs.celeritasextra.client.StaticItemGeometry;
import com.sumirelabs.celeritasextra.compat.RendererCompat;
import com.sumirelabs.celeritasextra.compat.ShaderPassCompat;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.color.ItemColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import java.util.List;

/** Reuses emitted vertex bytes without replacing Forge's emissive/light-segmentation renderer. */
@Mixin(RenderItem.class)
public class MixinRenderItemVertexCache {
    @Unique private IBakedModel celeritasExtra$staticModel;
    @Shadow @Final private ItemColors itemColors;

    @WrapMethod(method = "renderModel(Lnet/minecraft/client/renderer/block/model/IBakedModel;ILnet/minecraft/item/ItemStack;)V")
    private void celeritasExtra$scopeModel(IBakedModel model, int color, ItemStack stack, Operation<Void> original) {
        IBakedModel previous = celeritasExtra$staticModel;
        celeritasExtra$staticModel = CeleritasExtraClientMod.options().renderSettings.itemVertexCache
                && !RendererCompat.hasNativeItemCache() && !ShaderPassCompat.hasIris()
                && ((Object) this).getClass() == RenderItem.class && StaticItemGeometry.isStaticModel(model) ? model : null;
        try { original.call(model, color, stack); }
        finally { celeritasExtra$staticModel = previous; }
    }

    @WrapMethod(method = "renderQuads")
    private void celeritasExtra$reuseVertices(BufferBuilder buffer, List<BakedQuad> quads, int color,
                                              ItemStack stack, Operation<Void> original) {
        IBakedModel model = celeritasExtra$staticModel;
        if (model == null || buffer.getClass() != BufferBuilder.class
                || buffer.getVertexFormat() != DefaultVertexFormats.ITEM || buffer.getDrawMode() != 7
                || buffer.isColorDisabled() || !(buffer instanceof BufferBuilderOffsets offsets)
                || offsets.celeritasExtra$xOffset() != 0 || offsets.celeritasExtra$yOffset() != 0 || offsets.celeritasExtra$zOffset() != 0) {
            original.call(buffer, quads, color, stack);
            return;
        }
        var cache = StaticItemVertexCache.INSTANCE;
        // Forge marks every generated item layer as tinted, including ordinary swords.
        // No registered handler means colorMultiplier is always -1; registered handlers stay live.
        boolean noStackTint = stack.isEmpty() || itemColors.getClass() == ItemColors.class && itemColors instanceof ItemColorsAccess access
                && !access.celeritasExtra$itemColorMap().containsKey(stack.getItem().delegate);
        if (!cache.canCache(model, quads, color, noStackTint)) {
            original.call(buffer, quads, color, stack);
            return;
        }
        int[] cached = cache.getPrepared(model, quads, color);
        if (cached != null) { buffer.addVertexData(cached); return; }
        int start = buffer.getVertexCount();
        original.call(buffer, quads, color, stack);
        int added = buffer.getVertexCount() - start;
        if (added != quads.size() * 4 || quads.isEmpty() || quads.size() > 1024
                || buffer.getVertexFormat() != DefaultVertexFormats.ITEM) return;
        int[] data = StaticItemVertexCache.capture(buffer, start);
        cache.put(model, quads, color, noStackTint, data);
    }
}
