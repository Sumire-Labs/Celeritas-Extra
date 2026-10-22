package com.sumirelabs.celeritasextra.mixin.animation;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sumirelabs.celeritasextra.client.AnimationControl;
import com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Filters sprite ticks without injecting into TextureMap's update loop, which LoliASM overwrites.
 * Naughthirium can keep selecting visible sprites and clearing their active flags as usual.
 */
@Mixin(TextureAtlasSprite.class)
public abstract class MixinTextureAtlasSprite {
    @Unique private AnimationControl.Category celeritasExtra$animationCategory;

    @WrapMethod(method = "updateAnimation()V")
    private void celeritasExtra$controlAnimation(Operation<Void> original) {
        if (celeritasExtra$animationCategory == null) {
            celeritasExtra$animationCategory = AnimationControl.classify(((TextureAtlasSprite) (Object) this).getIconName());
        }
        if (AnimationControl.shouldAnimateCategory(CeleritasExtraClientMod.options().animationSettings, celeritasExtra$animationCategory)) {
            original.call();
        }
    }
}
