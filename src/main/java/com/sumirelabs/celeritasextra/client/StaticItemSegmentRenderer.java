/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client;

import java.util.List;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.item.ItemStack;

/** Bridge from Forge's complete item segment to the scoped RenderItem cache. */
public interface StaticItemSegmentRenderer {

    boolean celeritasExtra$drawCachedSegment(BufferBuilder buffer, List<BakedQuad> quads, int color, ItemStack stack);

}
