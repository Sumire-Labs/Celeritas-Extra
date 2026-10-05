package com.sumire.celeritasextra.client.gui;

import net.minecraft.client.resources.I18n;
import java.util.Map;

/** Resolves upstream tooltip keys and legacy .lang newline escapes. */
public final class Translations {
    private static final Map<String, String> TOOLTIPS = Map.ofEntries(
            Map.entry("tile.water.name", "sodium-extra.option.animate_water.tooltip"),
            Map.entry("tile.lava.name", "sodium-extra.option.animate_lava.tooltip"),
            Map.entry("tile.fire.name", "sodium-extra.option.animate_fire.tooltip"),
            Map.entry("tile.portal.name", "sodium-extra.option.animate_portal.tooltip"),
            Map.entry("subtitles.weather.rain", "sodium-extra.option.rain_splash.tooltip"),
            Map.entry("subtitles.block.generic.break", "sodium-extra.option.block_break.tooltip"),
            Map.entry("subtitles.block.generic.hit", "sodium-extra.option.block_breaking.tooltip"),
            Map.entry("soundCategory.weather", "sodium-extra.option.rain_snow.tooltip"),
            Map.entry("options.renderClouds", "celeritasextra.option.clouds.tooltip"),
            Map.entry("item.frame.name", "sodium-extra.option.item_frames.tooltip"),
            Map.entry("entity.ArmorStand.name", "sodium-extra.option.armor_stands.tooltip"),
            Map.entry("entity.Painting.name", "sodium-extra.option.paintings.tooltip"),
            Map.entry("tile.pistonBase.name", "sodium-extra.option.piston.tooltip")
    );
    private Translations() {}

    public static String tooltipKey(String nameKey) {
        return TOOLTIPS.getOrDefault(nameKey, nameKey + ".tooltip");
    }

    public static String format(String key, Object... arguments) {
        return decodeNewlines(I18n.format(key, arguments));
    }

    public static String decodeNewlines(String value) {
        return value.replace("\\n", "\n");
    }
}
