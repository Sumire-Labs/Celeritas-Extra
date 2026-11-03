package com.sumirelabs.celeritasextra.client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

/** Exact vanilla/Forge implementations whose baked geometry is not stack/time dependent. */
public final class StaticItemGeometry {

    private StaticItemGeometry() {}
    public static boolean isStaticModel(IBakedModel model) {
        if (model == null)
            return false;
        Class<?> type = model.getClass();
        return type == SimpleBakedModel.class || type == BakedItemModel.class
            || type == BakedItemModel.BakedGuiItemModel.class;
    }
    public static boolean isStaticQuad(BakedQuad quad) {
        return quad.getClass() == BakedQuad.class || quad.getClass() == UnpackedBakedQuad.class;
    }

}
