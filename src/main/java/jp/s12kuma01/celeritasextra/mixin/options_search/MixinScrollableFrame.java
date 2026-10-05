package jp.s12kuma01.celeritasextra.mixin.options_search;

import jp.s12kuma01.celeritasextra.client.gui.ShiftScrollDispatcher;
import org.embeddedt.embeddium.impl.gui.frame.AbstractFrame;
import org.embeddedt.embeddium.impl.gui.frame.ScrollableFrame;
import org.embeddedt.embeddium.impl.gui.frame.components.ScrollBarComponent;
import org.embeddedt.embeddium.impl.gui.framework.InteractionContext;
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ScrollableFrame.class, remap = false)
public abstract class MixinScrollableFrame extends AbstractFrame {
    @Shadow private ScrollBarComponent horizontalScrollBar;
    @Shadow private ScrollBarComponent verticalScrollBar;
    @Shadow private Dim2i viewPortDimension;

    protected MixinScrollableFrame(Dim2i dim, boolean outline) { super(dim, outline); }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$sliderFirst(InteractionContext context, double x, double y,
                                          double horizontal, double vertical, CallbackInfoReturnable<Boolean> cir) {
        if (ShiftScrollDispatcher.dispatch((ScrollableFrame) (Object) this, viewPortDimension,
                horizontalScrollBar == null ? 0 : horizontalScrollBar.getOffset(),
                verticalScrollBar == null ? 0 : verticalScrollBar.getOffset(),
                context, x, y, horizontal, vertical)) cir.setReturnValue(true);
    }
}
