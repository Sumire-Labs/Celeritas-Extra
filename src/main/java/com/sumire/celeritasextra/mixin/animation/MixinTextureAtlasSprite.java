package com.sumire.celeritasextra.mixin.animation;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sumire.celeritasextra.client.AnimationControl;
import com.sumire.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Filters sprite ticks without injecting into TextureMap's update loop, which LoliASM overwrites.
 * Naughthirium can keep selecting visible sprites and clearing their active flags as usual.
 */
@Mixin(TextureAtlasSprite.class)
public abstract class MixinTextureAtlasSprite {
    @WrapMethod(method = "updateAnimation()V")
    private void celeritasExtra$controlAnimation(Operation<Void> original) {
        String iconName = ((TextureAtlasSprite) (Object) this).getIconName();
        if (AnimationControl.shouldAnimate(CeleritasExtraClientMod.options().animationSettings, iconName)) {
            original.call();
        }
    }
}
