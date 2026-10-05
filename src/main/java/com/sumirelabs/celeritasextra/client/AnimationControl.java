package com.sumirelabs.celeritasextra.client;

import com.sumirelabs.celeritasextra.client.gui.CeleritasExtraGameOptions.AnimationSettings;

/** Shared animation policy, independent of the atlas implementation and its update loop. */
public final class AnimationControl {
    private AnimationControl() { }

    public static boolean shouldAnimate(AnimationSettings settings, String iconName) {
        if (!settings.animation) return false;
        if (iconName == null) return true;
        if (iconName.contains("water_still") || iconName.contains("water_flow")) return settings.water;
        if (iconName.contains("lava_still") || iconName.contains("lava_flow")) return settings.lava;
        if (iconName.contains("portal")) return settings.portal;
        if (iconName.contains("fire_layer_0") || iconName.contains("fire_layer_1")) return settings.fire;
        if (iconName.contains("magma") || iconName.contains("sea_lantern")
                || iconName.contains("prismarine") || iconName.contains("kelp")) return settings.blockAnimations;
        return true;
    }
}
