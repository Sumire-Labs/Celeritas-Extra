package com.sumirelabs.celeritasextra.mixin.options_search;

import com.sumirelabs.celeritasextra.client.gui.SliderScroll;
import org.embeddedt.embeddium.impl.gui.framework.InteractionContext;
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.taumc.celeritas.api.options.control.ControlElement;
import org.taumc.celeritas.api.options.structure.Option;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "org.taumc.celeritas.api.options.control.SliderControl$Button", remap = false)
public abstract class MixinSliderScroll extends ControlElement<Integer> {
    @Shadow @Final private int min;
    @Shadow @Final private int max;
    @Shadow @Final private int interval;

    protected MixinSliderScroll(Option<Integer> option, Dim2i dim) { super(option, dim); }

    @Override public boolean mouseScrolled(InteractionContext context, double x, double y, double horizontal, double vertical) {
        if (!context.isSpecialKeyDown(InteractionContext.SpecialKey.SHIFT) || !isMouseOver(x, y)
                || !option.isAvailable() || vertical == 0 || !Double.isFinite(vertical)) return false;
        option.setValue(SliderScroll.adjust(option.getValue(), min, max, interval, vertical));
        // Consume even at the boundary so the page does not scroll underneath the slider.
        return true;
    }
}
