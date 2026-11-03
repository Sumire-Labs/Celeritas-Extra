package com.sumirelabs.celeritasextra.mixin.item_cache;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.BufferBuilder;

@Mixin(BufferBuilder.class)
public interface BufferBuilderOffsets {

    @Accessor("xOffset")
    double celeritasExtra$xOffset();
    @Accessor("yOffset")
    double celeritasExtra$yOffset();
    @Accessor("zOffset")
    double celeritasExtra$zOffset();

}
