package com.sumirelabs.celeritasextra.client;

import com.sumirelabs.celeritasextra.client.gui.CeleritasExtraGameOptions.AnimationSettings;

/** Shared animation policy, independent of the atlas implementation and its update loop. */
public final class AnimationControl {

    private AnimationControl() {}

    public static boolean shouldAnimate(AnimationSettings settings, String iconName) {
        return shouldAnimateCategory(settings, classify(iconName));
    }

    /** Classify once per sprite; settings remain live when a category is cached. */
    public static Category classify(String iconName) {
        if (iconName == null)
            return Category.OTHER;
        if (iconName.contains("water_still") || iconName.contains("water_flow"))
            return Category.WATER;
        if (iconName.contains("lava_still") || iconName.contains("lava_flow"))
            return Category.LAVA;
        if (iconName.contains("portal"))
            return Category.PORTAL;
        if (iconName.contains("fire_layer_0") || iconName.contains("fire_layer_1"))
            return Category.FIRE;
        if (iconName.contains("magma") || iconName.contains("sea_lantern")
            || iconName.contains("prismarine") || iconName.contains("kelp"))
            return Category.BLOCK;
        return Category.OTHER;
    }

    public static boolean shouldAnimateCategory(AnimationSettings settings, Category category) {
        if (!settings.animation)
            return false;
        return switch (category) {
            case WATER -> settings.water;
            case LAVA -> settings.lava;
            case PORTAL -> settings.portal;
            case FIRE -> settings.fire;
            case BLOCK -> settings.blockAnimations;
            case OTHER -> true;
        };
    }

    public enum Category {
        WATER, LAVA, PORTAL, FIRE, BLOCK, OTHER
    }

}
