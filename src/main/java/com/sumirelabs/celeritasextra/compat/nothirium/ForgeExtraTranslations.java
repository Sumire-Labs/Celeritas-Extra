package com.sumirelabs.celeritasextra.compat.nothirium;

import java.util.Map;

/** Reuses the option labels already translated for the renderer-native screens. */
final class ForgeExtraTranslations {
    private ForgeExtraTranslations() { }
    private static final Map<String, String> KEYS = Map.ofEntries(
            Map.entry("animation.animation", "gui.all"),
            Map.entry("animation.water", "tile.water.name"),
            Map.entry("animation.lava", "tile.lava.name"),
            Map.entry("animation.fire", "tile.fire.name"),
            Map.entry("animation.portal", "tile.portal.name"),
            Map.entry("animation.blockAnimations", "sodium-extra.option.block_animations"),
            Map.entry("particle.particles", "gui.all"),
            Map.entry("particle.rainSplash", "subtitles.weather.rain"),
            Map.entry("particle.blockBreak", "subtitles.block.generic.break"),
            Map.entry("particle.blockBreaking", "subtitles.block.generic.hit"),
            Map.entry("detail.stars", "sodium-extra.option.stars"),
            Map.entry("detail.sky", "sodium-extra.option.sky"),
            Map.entry("detail.sun", "sodium-extra.option.sun"),
            Map.entry("detail.moon", "sodium-extra.option.moon"),
            Map.entry("detail.totalStars", "options.total_stars"),
            Map.entry("detail.rainSnow", "soundCategory.weather"),
            Map.entry("detail.skyColors", "sodium-extra.option.sky_colors"),
            Map.entry("detail.biomeColors", "sodium-extra.option.biome_colors"),
            Map.entry("render.clouds", "options.renderClouds"),
            Map.entry("render.modernClouds", "celeritasextra.option.modern_clouds"),
            Map.entry("render.cloudHeight", "sodium-extra.option.cloud_height"),
            Map.entry("render.cloudDistance", "sodium-extra.option.cloud_distance"),
            Map.entry("render.cloudScale", "options.cloud_scale"),
            Map.entry("render.fog", "sodium-extra.option.fog_type.atmospheric"),
            Map.entry("render.fogStart", "sodium-extra.option.fog_start"),
            Map.entry("render.fogDistance", "sodium-extra.option.fog_distance"),
            Map.entry("detail.voidFog", "options.void_fog"),
            Map.entry("render.itemFrames", "item.frame.name"),
            Map.entry("render.itemFrameLodDistance", "moreculling.config.option.itemFrameLODRange"),
            Map.entry("render.mapBackFaceCulling", "moreculling.config.option.itemFrameMapCulling"),
            Map.entry("render.itemFrameNameTag", "sodium-extra.option.item_frame_name_tag"),
            Map.entry("render.entityRenderDistance", "celeritasextra.option.entity_render_distance"),
            Map.entry("render.entityDistanceExemptions", "celeritasextra.gui.entity_exemptions"),
            Map.entry("render.tileEntityDistanceExemptions", "celeritasextra.gui.tile_entity_exemptions"),
            Map.entry("render.armorStands", "entity.ArmorStand.name"),
            Map.entry("render.paintings", "entity.Painting.name"),
            Map.entry("render.playerNameTag", "sodium-extra.option.player_name_tag"),
            Map.entry("render.beacons", "sodium-extra.option.beacon_beam"),
            Map.entry("render.tileEntityRenderDistance", "celeritasextra.option.tile_entity_render_distance"),
            Map.entry("render.signTextCulling", "moreculling.config.option.signTextCulling"),
            Map.entry("render.limitBeaconBeamHeight", "sodium-extra.option.limit_beacon_beam_height"),
            Map.entry("render.pistons", "tile.pistonBase.name"),
            Map.entry("render.enchantingTableBooks", "sodium-extra.option.enchanting_table_book"),
            Map.entry("render.lightUpdates", "sodium-extra.option.light_updates"),
            Map.entry("extra.showFps", "sodium-extra.option.show_fps"),
            Map.entry("extra.steadyDebugHud", "sodium-extra.option.steady_debug_hud"),
            Map.entry("extra.showFPSExtended", "sodium-extra.option.show_fps_extended"),
            Map.entry("extra.showMemory", "celeritasextra.option.show_memory"),
            Map.entry("extra.showCoords", "sodium-extra.option.show_coordinates"),
            Map.entry("extra.ignoreReducedDebugInfo", "celeritasextra.option.ignore_reduced_debug_info"),
            Map.entry("extra.steadyDebugHudRefreshInterval", "sodium-extra.option.steady_debug_hud_refresh_interval"),
            Map.entry("extra.toasts", "sodium-extra.option.toasts"),
            Map.entry("extra.menuFpsLimit", "celeritasextra.option.menu_fps_limit"),
            Map.entry("extra.inactiveFpsLimit", "celeritasextra.option.inactive_fps_limit"),
            Map.entry("extra.minimizedFpsLimit", "celeritasextra.option.minimized_fps_limit"),
            Map.entry("extra.toastAdvancement", "sodium-extra.option.advancement_toast"),
            Map.entry("extra.toastRecipe", "sodium-extra.option.recipe_toast"),
            Map.entry("extra.toastTutorial", "sodium-extra.option.tutorial_toast"),
            Map.entry("extra.toastSystem", "sodium-extra.option.system_toast"),
            Map.entry("extra.modNameTooltip", "celeritasextra.option.mod_name_tooltip"),
            Map.entry("render.preventShaders", "sodium-extra.option.prevent_shaders"),
            Map.entry("render.cloudTranslucency", "options.mode_cloud_translucency"),
            Map.entry("extra.overlayCorner", "sodium-extra.option.overlay_corner"),
            Map.entry("extra.textContrast", "sodium-extra.option.text_contrast")
    );

    static String key(String category, String property) {
        return KEYS.get(category + "." + property);
    }
}
