package jp.s12kuma01.celeritasextra.client;

import jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraGameOptions.AnimationSettings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnimationControlTest {
    @Test void masterToggleStopsAllSpritesIncludingUnknownAndUnnamedSprites() {
        var settings = new AnimationSettings();
        settings.animation = false;
        for (String name : List.of("minecraft:blocks/water_still", "minecraft:blocks/lava_flow", "mod:custom")) {
            assertFalse(AnimationControl.shouldAnimate(settings, name));
        }
        assertFalse(AnimationControl.shouldAnimate(settings, null));
    }

    @Test void categoryTogglesPreserveOtherAnimationsAndRespondToLiveChanges() {
        var settings = new AnimationSettings();
        settings.water = false;
        assertFalse(AnimationControl.shouldAnimate(settings, "minecraft:blocks/water_still"));
        assertFalse(AnimationControl.shouldAnimate(settings, "minecraft:blocks/water_flow"));
        assertTrue(AnimationControl.shouldAnimate(settings, "minecraft:blocks/lava_still"));
        settings.water = true;
        assertTrue(AnimationControl.shouldAnimate(settings, "minecraft:blocks/water_still"));
        settings.lava = settings.fire = settings.portal = settings.blockAnimations = false;
        for (String name : List.of("lava_still", "lava_flow", "fire_layer_0", "fire_layer_1", "portal",
                "magma", "sea_lantern", "prismarine", "kelp")) {
            assertFalse(AnimationControl.shouldAnimate(settings, "minecraft:blocks/" + name), name);
        }
        assertTrue(AnimationControl.shouldAnimate(settings, "mod:custom_animation"));
        assertTrue(AnimationControl.shouldAnimate(settings, null));
    }
}
