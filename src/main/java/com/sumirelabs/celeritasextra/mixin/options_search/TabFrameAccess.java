package com.sumirelabs.celeritasextra.mixin.options_search;

import org.embeddedt.embeddium.impl.gui.frame.tab.Tab;
import org.embeddedt.embeddium.impl.gui.frame.tab.TabFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

/** Includes Celeritas's action tabs, which are not represented by OptionPage objects. */
@Mixin(value = TabFrame.class, remap = false)
public interface TabFrameAccess {
    @Accessor("tabs")
    Map<String, List<Tab<?>>> celeritasExtra$nativeTabs();
}
