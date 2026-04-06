package jp.s12kuma01.celeritasextra.client.gui;

import com.google.common.collect.ImmutableList;
import jp.s12kuma01.celeritasextra.client.particle.ParticleClassRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.taumc.celeritas.api.options.control.ControlValueFormatter;
import org.taumc.celeritas.api.options.control.CyclingControl;
import org.taumc.celeritas.api.options.control.SliderControl;
import org.taumc.celeritas.api.options.control.TickBoxControl;
import org.taumc.celeritas.api.options.structure.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Defines all option pages for Celeritas Extra
 */
public class CeleritasExtraGameOptionPages {

    private static final CeleritasExtraOptionsStorage celeritasExtraOpts = new CeleritasExtraOptionsStorage();

    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter) {
        return booleanOption(translationKey, setter, getter, null, null);
    }

    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            OptionImpact impact) {
        return booleanOption(translationKey, setter, getter, null, impact);
    }

    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            OptionFlag flag) {
        return booleanOption(translationKey, setter, getter, flag, null);
    }

    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            OptionFlag flag,
            OptionImpact impact) {
        var builder = OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                .setName(TextComponent.literal(I18n.format(translationKey)))
                .setTooltip(TextComponent.literal(I18n.format(translationKey + ".tooltip")))
                .setControl(TickBoxControl::new)
                .setBinding(setter, getter);
        if (flag != null) builder.setFlags(flag);
        if (impact != null) builder.setImpact(impact);
        return builder.build();
    }

    private static OptionImpl<CeleritasExtraGameOptions, Integer> sliderOption(
            String translationKey,
            int min, int max, int step,
            ControlValueFormatter formatter,
            BiConsumer<CeleritasExtraGameOptions, Integer> setter,
            Function<CeleritasExtraGameOptions, Integer> getter) {
        return OptionImpl.createBuilder(int.class, celeritasExtraOpts)
                .setName(TextComponent.literal(I18n.format(translationKey)))
                .setTooltip(TextComponent.literal(I18n.format(translationKey + ".tooltip")))
                .setControl(option -> new SliderControl(option, min, max, step, formatter))
                .setBinding(setter, getter)
                .build();
    }

    /**
     * Animation settings page
     */
    public static OptionPage animation() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(booleanOption("celeritasextra.option.animations.all",
                        (opts, v) -> opts.animationSettings.animation = v,
                        opts -> opts.animationSettings.animation,
                        OptionFlag.REQUIRES_ASSET_RELOAD, OptionImpact.MEDIUM))
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(booleanOption("celeritasextra.option.animations.water",
                        (opts, v) -> opts.animationSettings.water = v,
                        opts -> opts.animationSettings.water,
                        OptionFlag.REQUIRES_ASSET_RELOAD))
                .add(booleanOption("celeritasextra.option.animations.lava",
                        (opts, v) -> opts.animationSettings.lava = v,
                        opts -> opts.animationSettings.lava,
                        OptionFlag.REQUIRES_ASSET_RELOAD))
                .add(booleanOption("celeritasextra.option.animations.fire",
                        (opts, v) -> opts.animationSettings.fire = v,
                        opts -> opts.animationSettings.fire,
                        OptionFlag.REQUIRES_ASSET_RELOAD))
                .add(booleanOption("celeritasextra.option.animations.portal",
                        (opts, v) -> opts.animationSettings.portal = v,
                        opts -> opts.animationSettings.portal,
                        OptionFlag.REQUIRES_ASSET_RELOAD))
                .add(booleanOption("celeritasextra.option.animations.block",
                        (opts, v) -> opts.animationSettings.blockAnimations = v,
                        opts -> opts.animationSettings.blockAnimations,
                        OptionFlag.REQUIRES_ASSET_RELOAD))
                .build());

        return new OptionPage(CeleritasExtraOptionPages.ANIMATION, TextComponent.literal(I18n.format("celeritasextra.option.page.animations")), ImmutableList.copyOf(groups));
    }

    /**
     * Particle settings page
     */
    public static OptionPage particle() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(booleanOption("celeritasextra.option.particles.all",
                        (opts, v) -> opts.particleSettings.particles = v,
                        opts -> opts.particleSettings.particles,
                        OptionImpact.HIGH))
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(booleanOption("celeritasextra.option.particles.rain_splash",
                        (opts, v) -> opts.particleSettings.rainSplash = v,
                        opts -> opts.particleSettings.rainSplash))
                .add(booleanOption("celeritasextra.option.particles.block_break",
                        (opts, v) -> opts.particleSettings.blockBreak = v,
                        opts -> opts.particleSettings.blockBreak))
                .add(booleanOption("celeritasextra.option.particles.block_breaking",
                        (opts, v) -> opts.particleSettings.blockBreaking = v,
                        opts -> opts.particleSettings.blockBreaking))
                .build());

        // Scan registered particle factories to discover particle classes
        ParticleClassRegistry.getInstance().scanFactories(Minecraft.getMinecraft().effectRenderer);

        // Dynamic particle class controls - grouped by mod name
        var discovered = ParticleClassRegistry.getInstance().getDiscoveredClasses();
        if (!discovered.isEmpty()) {
            var byMod = new TreeMap<String, List<Map.Entry<String, String>>>();
            for (var entry : discovered.entrySet()) {
                String fullName = entry.getKey();
                String modId = ParticleClassRegistry.getInstance().getModName(fullName);
                if (modId == null) {
                    modId = "unknown";
                }
                byMod.computeIfAbsent(modId, k -> new ArrayList<>()).add(entry);
            }

            for (var modEntry : byMod.entrySet()) {
                var modId = modEntry.getKey();
                var groupBuilder = OptionGroup.createBuilder();
                var classEntries = modEntry.getValue();
                classEntries.sort(Comparator.comparing(Map.Entry::getValue));

                for (var classEntry : classEntries) {
                    var fullClassName = classEntry.getKey();
                    var simpleClassName = classEntry.getValue();
                    String displayName = simpleClassName + " (" + modId + ")";

                    groupBuilder.add(OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                            .setName(TextComponent.literal(displayName))
                            .setTooltip(TextComponent.literal(
                                    I18n.format("celeritasextra.option.particles.other.tooltip", simpleClassName)
                                            + "\n" + fullClassName))
                            .setControl(TickBoxControl::new)
                            .setBinding(
                                    (opts, value) -> ParticleClassRegistry.getInstance().setClassEnabled(fullClassName, value),
                                    opts -> !ParticleClassRegistry.getInstance().isClassDisabled(fullClassName)
                            )
                            .build()
                    );
                }
                groups.add(groupBuilder.build());
            }
        }

        return new OptionPage(CeleritasExtraOptionPages.PARTICLE, TextComponent.literal(I18n.format("celeritasextra.option.page.particles")), ImmutableList.copyOf(groups));
    }

    /**
     * Detail settings page
     */
    public static OptionPage details() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(booleanOption("celeritasextra.option.details.sky",
                        (opts, v) -> opts.detailSettings.sky = v,
                        opts -> opts.detailSettings.sky,
                        OptionFlag.REQUIRES_RENDERER_RELOAD))
                .add(booleanOption("celeritasextra.option.details.stars",
                        (opts, v) -> opts.detailSettings.stars = v,
                        opts -> opts.detailSettings.stars,
                        OptionFlag.REQUIRES_RENDERER_RELOAD))
                .add(booleanOption("celeritasextra.option.details.sun_moon",
                        (opts, v) -> opts.detailSettings.sunMoon = v,
                        opts -> opts.detailSettings.sunMoon,
                        OptionFlag.REQUIRES_RENDERER_RELOAD))
                .add(booleanOption("celeritasextra.option.details.rain_snow",
                        (opts, v) -> opts.detailSettings.rainSnow = v,
                        opts -> opts.detailSettings.rainSnow))
                .add(booleanOption("celeritasextra.option.details.biome_colors",
                        (opts, v) -> opts.detailSettings.biomeColors = v,
                        opts -> opts.detailSettings.biomeColors,
                        OptionFlag.REQUIRES_RENDERER_RELOAD))
                .add(booleanOption("celeritasextra.option.details.void_particles",
                        (opts, v) -> opts.detailSettings.voidParticles = v,
                        opts -> opts.detailSettings.voidParticles))
                .add(booleanOption("celeritasextra.option.details.void_fog",
                        (opts, v) -> opts.detailSettings.voidFog = v,
                        opts -> opts.detailSettings.voidFog))
                .build());

        return new OptionPage(CeleritasExtraOptionPages.DETAILS, TextComponent.literal(I18n.format("celeritasextra.option.page.details")), ImmutableList.copyOf(groups));
    }

    /**
     * Render settings page
     */
    public static OptionPage render() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(booleanOption("celeritasextra.option.render.fog",
                        (opts, v) -> opts.renderSettings.fog = v,
                        opts -> opts.renderSettings.fog))
                .add(sliderOption("celeritasextra.option.render.fog_start",
                        0, 200, 10, ControlValueFormatter.percentage(),
                        (opts, v) -> opts.renderSettings.fogStart = v,
                        opts -> opts.renderSettings.fogStart))
                .add(sliderOption("celeritasextra.option.render.fog_distance",
                        0, 32, 1, ControlValueFormatter.quantityOrDisabled("chunks", "Default"),
                        (opts, v) -> opts.renderSettings.fogDistance = v,
                        opts -> opts.renderSettings.fogDistance))
                .add(OptionImpl.createBuilder(CeleritasExtraGameOptions.FogType.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.render.fog_type")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.render.fog_type.tooltip")))
                        .setControl(option -> new CyclingControl<>(option, CeleritasExtraGameOptions.FogType.class,
                                new TextComponent[]{
                                        TextComponent.literal(CeleritasExtraGameOptions.FogType.DEFAULT.getLocalizedName()),
                                        TextComponent.literal(CeleritasExtraGameOptions.FogType.OFF.getLocalizedName())
                                }))
                        .setBinding((opts, value) -> opts.renderSettings.fogType = value,
                                opts -> opts.renderSettings.fogType)
                        .build())
                .add(booleanOption("celeritasextra.option.render.prevent_shaders",
                        (opts, v) -> opts.renderSettings.preventShaders = v,
                        opts -> opts.renderSettings.preventShaders))
                .add(booleanOption("celeritasextra.option.render.clouds",
                        (opts, v) -> opts.renderSettings.clouds = v,
                        opts -> opts.renderSettings.clouds))
                .add(sliderOption("celeritasextra.option.render.cloud_height",
                        0, 384, 16, ControlValueFormatter.number(),
                        (opts, v) -> opts.renderSettings.cloudHeight = v,
                        opts -> opts.renderSettings.cloudHeight))
                .add(sliderOption("celeritasextra.option.render.cloud_distance",
                        0, 64, 1, ControlValueFormatter.quantityOrDisabled("chunks", "Default"),
                        (opts, v) -> opts.renderSettings.cloudDistance = v,
                        opts -> opts.renderSettings.cloudDistance))
                .add(booleanOption("celeritasextra.option.render.light_updates",
                        (opts, v) -> opts.renderSettings.lightUpdates = v,
                        opts -> opts.renderSettings.lightUpdates,
                        OptionImpact.HIGH))
                .add(booleanOption("celeritasextra.option.render.item_frames",
                        (opts, v) -> opts.renderSettings.itemFrames = v,
                        opts -> opts.renderSettings.itemFrames))
                .add(booleanOption("celeritasextra.option.render.armor_stands",
                        (opts, v) -> opts.renderSettings.armorStands = v,
                        opts -> opts.renderSettings.armorStands))
                .add(booleanOption("celeritasextra.option.render.paintings",
                        (opts, v) -> opts.renderSettings.paintings = v,
                        opts -> opts.renderSettings.paintings))
                .add(booleanOption("celeritasextra.option.render.beacons",
                        (opts, v) -> opts.renderSettings.beacons = v,
                        opts -> opts.renderSettings.beacons))
                .add(booleanOption("celeritasextra.option.render.limit_beacon_beam_height",
                        (opts, v) -> opts.renderSettings.limitBeaconBeamHeight = v,
                        opts -> opts.renderSettings.limitBeaconBeamHeight))
                .add(booleanOption("celeritasextra.option.render.pistons",
                        (opts, v) -> opts.renderSettings.pistons = v,
                        opts -> opts.renderSettings.pistons))
                .add(booleanOption("celeritasextra.option.render.enchanting_books",
                        (opts, v) -> opts.renderSettings.enchantingTableBooks = v,
                        opts -> opts.renderSettings.enchantingTableBooks))
                .add(booleanOption("celeritasextra.option.render.player_name_tag",
                        (opts, v) -> opts.renderSettings.playerNameTag = v,
                        opts -> opts.renderSettings.playerNameTag))
                .add(booleanOption("celeritasextra.option.render.item_frame_name_tag",
                        (opts, v) -> opts.renderSettings.itemFrameNameTag = v,
                        opts -> opts.renderSettings.itemFrameNameTag))
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
                .add(booleanOption("celeritasextra.option.extra.fps",
                        (opts, v) -> opts.extraSettings.showFps = v,
                        opts -> opts.extraSettings.showFps))
                .add(booleanOption("celeritasextra.option.extra.fps_extended",
                        (opts, v) -> opts.extraSettings.showFPSExtended = v,
                        opts -> opts.extraSettings.showFPSExtended))
                .add(booleanOption("celeritasextra.option.extra.coords",
                        (opts, v) -> opts.extraSettings.showCoords = v,
                        opts -> opts.extraSettings.showCoords))
                .add(booleanOption("celeritasextra.option.extra.ignore_reduced_debug_info",
                        (opts, v) -> opts.extraSettings.ignoreReducedDebugInfo = v,
                        opts -> opts.extraSettings.ignoreReducedDebugInfo))
                .add(OptionImpl.createBuilder(CeleritasExtraGameOptions.OverlayCorner.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.extra.overlay_corner")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.extra.overlay_corner.tooltip")))
                        .setControl(option -> new CyclingControl<>(option, CeleritasExtraGameOptions.OverlayCorner.class,
                                new TextComponent[]{
                                        TextComponent.literal(CeleritasExtraGameOptions.OverlayCorner.TOP_LEFT.getLocalizedName()),
                                        TextComponent.literal(CeleritasExtraGameOptions.OverlayCorner.TOP_RIGHT.getLocalizedName()),
                                        TextComponent.literal(CeleritasExtraGameOptions.OverlayCorner.BOTTOM_LEFT.getLocalizedName()),
                                        TextComponent.literal(CeleritasExtraGameOptions.OverlayCorner.BOTTOM_RIGHT.getLocalizedName())
                                }))
                        .setBinding((opts, value) -> opts.extraSettings.overlayCorner = value,
                                opts -> opts.extraSettings.overlayCorner)
                        .build())
                .add(OptionImpl.createBuilder(CeleritasExtraGameOptions.TextContrast.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.extra.text_contrast")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.extra.text_contrast.tooltip")))
                        .setControl(option -> new CyclingControl<>(option, CeleritasExtraGameOptions.TextContrast.class,
                                new TextComponent[]{
                                        TextComponent.literal(CeleritasExtraGameOptions.TextContrast.NONE.getLocalizedName()),
                                        TextComponent.literal(CeleritasExtraGameOptions.TextContrast.BACKGROUND.getLocalizedName()),
                                        TextComponent.literal(CeleritasExtraGameOptions.TextContrast.SHADOW.getLocalizedName())
                                }))
                        .setBinding((opts, value) -> opts.extraSettings.textContrast = value,
                                opts -> opts.extraSettings.textContrast)
                        .build())
                .build());

        // Other settings group
        groups.add(OptionGroup.createBuilder()
                .add(booleanOption("celeritasextra.option.extra.reduced_motion",
                        (opts, v) -> opts.extraSettings.reducedMotion = v,
                        opts -> opts.extraSettings.reducedMotion))
                .add(booleanOption("celeritasextra.option.extra.steady_debug_hud",
                        (opts, v) -> opts.extraSettings.steadyDebugHud = v,
                        opts -> opts.extraSettings.steadyDebugHud))
                .add(sliderOption("celeritasextra.option.extra.steady_debug_hud_refresh",
                        1, 60, 1, ControlValueFormatter.number(),
                        (opts, v) -> opts.extraSettings.steadyDebugHudRefreshInterval = v,
                        opts -> opts.extraSettings.steadyDebugHudRefreshInterval))
                .build());

        // HEI-specific options (only shown when HEI is installed)
        groups.add(OptionGroup.createBuilder()
                .addConditionally(
                        net.minecraftforge.fml.common.Loader.isModLoaded("jei"),
                        () -> booleanOption("celeritasextra.option.extra.hide_hei",
                                (opts, v) -> opts.extraSettings.hideHeiUntilSearch = v,
                                opts -> opts.extraSettings.hideHeiUntilSearch))
                .build());

        return new OptionPage(CeleritasExtraOptionPages.EXTRA, TextComponent.literal(I18n.format("celeritasextra.option.page.extra")), ImmutableList.copyOf(groups));
    }
}
