package com.sumirelabs.celeritasextra.mixin.item_cache;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IRegistryDelegate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Map;

@Mixin(ItemColors.class)
public interface ItemColorsAccess {
    @Accessor("itemColorMap") Map<IRegistryDelegate<Item>, IItemColor> celeritasExtra$itemColorMap();
}
