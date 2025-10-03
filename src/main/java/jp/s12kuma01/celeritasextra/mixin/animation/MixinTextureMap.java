package jp.s12kuma01.celeritasextra.mixin.animation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Controls texture animations (water, lava, fire, portal, etc.)
 * This is the 1.12.2 equivalent of MixinSpriteAtlasTexture from 1.20.1
 */
@Mixin(TextureMap.class)
public abstract class MixinTextureMap {

    @Shadow
    @Final
    private List<TextureAtlasSprite> listAnimatedSprites;

    @Unique
    private Map<Supplier<Boolean>, List<String>> celeritasExtra$animatedSprites;

    /**
     * Wrap the updateAnimation call on individual sprites to control which ones should animate
     */
    @WrapOperation(
        method = "updateAnimations",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;updateAnimation()V"
        )
    )
    private void celeritasExtra$controlIndividualAnimations(TextureAtlasSprite sprite, Operation<Void> original) {
        // Check if animations are globally disabled
        if (!CeleritasExtraClientMod.options().animationSettings.animation) {
            return;
        }

        // Check if this specific sprite should animate
        if (celeritasExtra$shouldAnimate(sprite)) {
            original.call(sprite);
        }
    }

    @Unique
    private boolean celeritasExtra$shouldAnimate(TextureAtlasSprite sprite) {
        if (sprite == null) {
            return true;
        }

        String iconName = sprite.getIconName();
        if (iconName == null) {
            return true;
        }

        // Lazy initialize the animated sprites map only once
        if (celeritasExtra$animatedSprites == null) {
            celeritasExtra$animatedSprites = celeritasExtra$constructAnimatedSprites();
        }

        // Quick check: if animation is off globally, no need to check individual textures
        if (!CeleritasExtraClientMod.options().animationSettings.animation) {
            return false;
        }

        // Check each category of animated textures
        for (Map.Entry<Supplier<Boolean>, List<String>> entry : celeritasExtra$animatedSprites.entrySet()) {
            for (String textureName : entry.getValue()) {
                if (iconName.contains(textureName)) {
                    return entry.getKey().get();
                }
            }
        }

        // Default to true for textures not in our list
        return true;
    }

    @Unique
    private Map<Supplier<Boolean>, List<String>> celeritasExtra$constructAnimatedSprites() {
        Map<Supplier<Boolean>, List<String>> map = new HashMap<>();

        // Water textures
        map.put(
            () -> CeleritasExtraClientMod.options().animationSettings.water,
            List.of("water_still", "water_flow")
        );

        // Lava textures
        map.put(
            () -> CeleritasExtraClientMod.options().animationSettings.lava,
            List.of("lava_still", "lava_flow")
        );

        // Portal textures
        map.put(
            () -> CeleritasExtraClientMod.options().animationSettings.portal,
            List.of("portal")
        );

        // Fire textures
        map.put(
            () -> CeleritasExtraClientMod.options().animationSettings.fire,
            List.of("fire_layer_0", "fire_layer_1")
        );

        // Block animations (various animated blocks)
        map.put(
            () -> CeleritasExtraClientMod.options().animationSettings.blockAnimations,
            List.of(
                "magma",
                "sea_lantern",
                "prismarine",
                "kelp"
            )
        );

        return map;
    }
}
