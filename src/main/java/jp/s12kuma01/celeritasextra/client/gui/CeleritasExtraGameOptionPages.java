package jp.s12kuma01.celeritasextra.client.gui;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
import org.taumc.celeritas.api.options.control.ControlValueFormatter;
import org.taumc.celeritas.api.options.control.CyclingControl;
import org.taumc.celeritas.api.options.control.SliderControl;
import org.taumc.celeritas.api.options.control.TickBoxControl;
import org.taumc.celeritas.api.options.structure.OptionFlag;
import org.taumc.celeritas.api.options.structure.OptionGroup;
import org.taumc.celeritas.api.options.structure.OptionImpl;
import org.taumc.celeritas.api.options.structure.OptionPage;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines all option pages for Celeritas Extra
 */
public class CeleritasExtraGameOptionPages {

    private static final CeleritasExtraOptionsStorage celeritasExtraOpts = new CeleritasExtraOptionsStorage();

    /**
     * Animation settings page
     */
    public static OptionPage animation() {
        List<OptionGroup> groups = new ArrayList<>();

        // All animations toggle
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.animations.all")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.animations.all.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.animation = value,
                                   opts -> opts.animationSettings.animation)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .build());

        // Individual animation controls
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.animations.water")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.animations.water.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.water = value,
                                   opts -> opts.animationSettings.water)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.animations.lava")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.animations.lava.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.lava = value,
                                   opts -> opts.animationSettings.lava)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.animations.fire")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.animations.fire.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.fire = value,
                                   opts -> opts.animationSettings.fire)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.animations.portal")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.animations.portal.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.portal = value,
                                   opts -> opts.animationSettings.portal)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.animations.block")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.animations.block.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.blockAnimations = value,
                                   opts -> opts.animationSettings.blockAnimations)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .build());

        return new OptionPage(CeleritasExtraOptionPages.ANIMATION, TextComponent.literal(I18n.format("celeritasextra.option.page.animations")), ImmutableList.copyOf(groups));
    }

    /**
     * Particle settings page
     */
    public static OptionPage particle() {
        List<OptionGroup> groups = new ArrayList<>();

        // All particles toggle
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.particles.all")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.particles.all.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleSettings.particles = value,
                                   opts -> opts.particleSettings.particles)
                        .build()
                )
                .build());

        // Individual particle controls
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.particles.rain_splash")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.particles.rain_splash.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleSettings.rainSplash = value,
                                   opts -> opts.particleSettings.rainSplash)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.particles.block_break")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.particles.block_break.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleSettings.blockBreak = value,
                                   opts -> opts.particleSettings.blockBreak)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.particles.block_breaking")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.particles.block_breaking.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleSettings.blockBreaking = value,
                                   opts -> opts.particleSettings.blockBreaking)
                        .build()
                )
                .build());

        // NOTE: Individual particle type control is currently disabled due to mixin implementation issues
        // Only basic particle controls (rain splash, block break, block breaking) are available

        return new OptionPage(CeleritasExtraOptionPages.PARTICLE, TextComponent.literal(I18n.format("celeritasextra.option.page.particles")), ImmutableList.copyOf(groups));
    }

    /**
     * Detail settings page
     */
    public static OptionPage details() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.details.sky")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.details.sky.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.sky = value,
                                   opts -> opts.detailSettings.sky)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.details.stars")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.details.stars.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.stars = value,
                                   opts -> opts.detailSettings.stars)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.details.sun_moon")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.details.sun_moon.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.sunMoon = value,
                                   opts -> opts.detailSettings.sunMoon)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.details.rain_snow")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.details.rain_snow.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.rainSnow = value,
                                   opts -> opts.detailSettings.rainSnow)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.details.biome_colors")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.details.biome_colors.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.biomeColors = value,
                                   opts -> opts.detailSettings.biomeColors)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .build());

        return new OptionPage(CeleritasExtraOptionPages.DETAILS, TextComponent.literal(I18n.format("celeritasextra.option.page.details")), ImmutableList.copyOf(groups));
    }

    /**
     * Render settings page
     */
    public static OptionPage render() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.render.fog")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.render.fog.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.fog = value,
                                   opts -> opts.renderSettings.fog)
                        .build()
                )
                .add(OptionImpl.createBuilder(int.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.render.fog_start")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.render.fog_start.tooltip")))
                        .setControl(option -> new SliderControl(option, 0, 200, 10, ControlValueFormatter.percentage()))
                        .setBinding((opts, value) -> opts.renderSettings.fogStart = value,
                                   opts -> opts.renderSettings.fogStart)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.render.prevent_shaders")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.render.prevent_shaders.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.preventShaders = value,
                                   opts -> opts.renderSettings.preventShaders)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.render.clouds")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.render.clouds.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.clouds = value,
                                   opts -> opts.renderSettings.clouds)
                        .build()
                )
                .add(OptionImpl.createBuilder(int.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.render.cloud_height")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.render.cloud_height.tooltip")))
                        .setControl(option -> new SliderControl(option, 0, 384, 16, ControlValueFormatter.number()))
                        .setBinding((opts, value) -> opts.renderSettings.cloudHeight = value,
                                   opts -> opts.renderSettings.cloudHeight)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.render.item_frames")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.render.item_frames.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.itemFrames = value,
                                   opts -> opts.renderSettings.itemFrames)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.render.armor_stands")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.render.armor_stands.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.armorStands = value,
                                   opts -> opts.renderSettings.armorStands)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.render.paintings")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.render.paintings.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.paintings = value,
                                   opts -> opts.renderSettings.paintings)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.render.beacons")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.render.beacons.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.beacons = value,
                                   opts -> opts.renderSettings.beacons)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.render.limit_beacon_beam_height")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.render.limit_beacon_beam_height.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.limitBeaconBeamHeight = value,
                                   opts -> opts.renderSettings.limitBeaconBeamHeight)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.render.pistons")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.render.pistons.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.pistons = value,
                                   opts -> opts.renderSettings.pistons)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.render.enchanting_books")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.render.enchanting_books.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.enchantingTableBooks = value,
                                   opts -> opts.renderSettings.enchantingTableBooks)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.render.player_name_tag")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.render.player_name_tag.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.playerNameTag = value,
                                   opts -> opts.renderSettings.playerNameTag)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.render.item_frame_name_tag")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.render.item_frame_name_tag.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.itemFrameNameTag = value,
                                   opts -> opts.renderSettings.itemFrameNameTag)
                        .build()
                )
                .build());

        return new OptionPage(CeleritasExtraOptionPages.RENDER, TextComponent.literal(I18n.format("celeritasextra.option.page.render")), ImmutableList.copyOf(groups));
    }

    /**
     * Extra settings page
     */
    public static OptionPage extra() {
        List<OptionGroup> groups = new ArrayList<>();

        // Overlay settings group
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.extra.fps")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.extra.fps.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.extraSettings.showFps = value,
                                   opts -> opts.extraSettings.showFps)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.extra.fps_extended")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.extra.fps_extended.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.extraSettings.showFPSExtended = value,
                                   opts -> opts.extraSettings.showFPSExtended)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.extra.coords")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.extra.coords.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.extraSettings.showCoords = value,
                                   opts -> opts.extraSettings.showCoords)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.extra.ignore_reduced_debug_info")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.extra.ignore_reduced_debug_info.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.extraSettings.ignoreReducedDebugInfo = value,
                                   opts -> opts.extraSettings.ignoreReducedDebugInfo)
                        .build()
                )
                .add(OptionImpl.createBuilder(CeleritasExtraGameOptions.OverlayCorner.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.extra.overlay_corner")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.extra.overlay_corner.tooltip")))
                        .setControl(option -> new CyclingControl<>(option, CeleritasExtraGameOptions.OverlayCorner.class,
                                CeleritasExtraGameOptions.OverlayCorner.values()))
                        .setBinding((opts, value) -> opts.extraSettings.overlayCorner = value,
                                   opts -> opts.extraSettings.overlayCorner)
                        .build()
                )
                .add(OptionImpl.createBuilder(CeleritasExtraGameOptions.TextContrast.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.extra.text_contrast")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.extra.text_contrast.tooltip")))
                        .setControl(option -> new CyclingControl<>(option, CeleritasExtraGameOptions.TextContrast.class,
                                CeleritasExtraGameOptions.TextContrast.values()))
                        .setBinding((opts, value) -> opts.extraSettings.textContrast = value,
                                   opts -> opts.extraSettings.textContrast)
                        .build()
                )
                .build());

        // Other settings group
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.extra.reduced_motion")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.extra.reduced_motion.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.extraSettings.reducedMotion = value,
                                   opts -> opts.extraSettings.reducedMotion)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.extra.steady_debug_hud")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.extra.steady_debug_hud.tooltip")))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.extraSettings.steadyDebugHud = value,
                                   opts -> opts.extraSettings.steadyDebugHud)
                        .build()
                )
                .add(OptionImpl.createBuilder(int.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.extra.steady_debug_hud_refresh")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.extra.steady_debug_hud_refresh.tooltip")))
                        .setControl(option -> new SliderControl(option, 1, 60, 1, ControlValueFormatter.number()))
                        .setBinding((opts, value) -> opts.extraSettings.steadyDebugHudRefreshInterval = value,
                                   opts -> opts.extraSettings.steadyDebugHudRefreshInterval)
                        .build()
                )
                .build());

        return new OptionPage(CeleritasExtraOptionPages.EXTRA, TextComponent.literal(I18n.format("celeritasextra.option.page.extra")), ImmutableList.copyOf(groups));
    }
}
