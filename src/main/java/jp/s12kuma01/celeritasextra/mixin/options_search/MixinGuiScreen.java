package jp.s12kuma01.celeritasextra.mixin.options_search;

import jp.s12kuma01.celeritasextra.client.gui.OptionsSearchScreen;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Celeritas inherits these callbacks from GuiScreen; injecting its subclass cannot target them. */
@Mixin(GuiScreen.class)
public class MixinGuiScreen {
    @Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$searchKey(char character, int key, CallbackInfo ci) {
        if ((Object) this instanceof OptionsSearchScreen screen && screen.celeritasExtra$searchKey(character, key)) ci.cancel();
    }
    @Inject(method = "updateScreen", at = @At("HEAD"))
    private void celeritasExtra$tickSearch(CallbackInfo ci) {
        if ((Object) this instanceof OptionsSearchScreen screen) screen.celeritasExtra$tickSearch();
    }
    @Inject(method = "onGuiClosed", at = @At("HEAD"))
    private void celeritasExtra$closeSearch(CallbackInfo ci) {
        if ((Object) this instanceof OptionsSearchScreen screen) screen.celeritasExtra$closeSearch();
    }
}
