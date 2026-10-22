package com.sumirelabs.celeritasextra.mixin.item_cache;

import net.minecraft.client.renderer.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BufferBuilder.class)
public interface BufferBuilderOffsets {
    @Accessor("xOffset") double celeritasExtra$xOffset();
    @Accessor("yOffset") double celeritasExtra$yOffset();
    @Accessor("zOffset") double celeritasExtra$zOffset();
}
