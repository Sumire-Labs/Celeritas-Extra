package jp.s12kuma01.celeritasextra.client.gui;

import com.google.common.collect.ImmutableList;
import jp.s12kuma01.celeritasextra.CeleritasExtraMod;
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
import java.util.function.BooleanSupplier;
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
        return booleanOption(translationKey, setter, getter, null, null, null);
    }

    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            OptionImpact impact) {
        return booleanOption(translationKey, setter, getter, null, impact, null);
    }

    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            OptionFlag flag) {
        return booleanOption(translationKey, setter, getter, flag, null, null);
    }

    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            OptionFlag flag,
            OptionImpact impact) {
        return booleanOption(translationKey, setter, getter, flag, impact, null);
    }

    /** Enable-gated toggle: greyed out (live) while {@code enabled} returns false. */
    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            BooleanSupplier enabled) {
        return booleanOption(translationKey, setter, getter, null, null, enabled);
    }

    /** Enable-gated toggle with a flag (e.g. asset reload). */
    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            OptionFlag flag,
            BooleanSupplier enabled) {
        return booleanOption(translationKey, setter, getter, flag, null, enabled);
    }

    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            OptionFlag flag,
            OptionImpact impact,
            BooleanSupplier enabled) {
        var builder = OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                .setName(TextComponent.literal(I18n.format(translationKey)))
                .setTooltip(TextComponent.literal(I18n.format(translationKey + ".tooltip")))
                .setControl(TickBoxControl::new)
                .setBinding(setter, getter);
        if (flag != null) builder.setFlags(flag);
        if (impact != null) builder.setImpact(impact);
        if (enabled != null) builder.setEnabledPredicate(enabled);
        return builder.build();
    }

    private static OptionImpl<CeleritasExtraGameOptions, Integer> sliderOption(
            String translationKey,
            int min, int max, int step,
            ControlValueFormatter formatter,
            BiConsumer<CeleritasExtraGameOptions, Integer> setter,
            Function<CeleritasExtraGameOptions, Integer> getter) {
        return sliderOption(translationKey, min, max, step, formatter, setter, getter, null, null);
    }

    private static OptionImpl<CeleritasExtraGameOptions, Integer> sliderOption(
            String translationKey,
            int min, int max, int step,
            ControlValueFormatter formatter,
            BiConsumer<CeleritasExtraGameOptions, Integer> setter,
            Function<CeleritasExtraGameOptions, Integer> getter,
            BooleanSupplier enabled) {
        return sliderOption(translationKey, min, max, step, formatter, setter, getter, enabled, null);
    }

    private static OptionImpl<CeleritasExtraGameOptions, Integer> sliderOption(
            String translationKey,
            int min, int max, int step,
            ControlValueFormatter formatter,
            BiConsumer<CeleritasExtraGameOptions, Integer> setter,
            Function<CeleritasExtraGameOptions, Integer> getter,
            BooleanSupplier enabled,
            OptionImpact impact) {
        var builder = OptionImpl.createBuilder(int.class, celeritasExtraOpts)
                .setName(TextComponent.literal(I18n.format(translationKey)))
                .setTooltip(TextComponent.literal(I18n.format(translationKey + ".tooltip")))
                .setControl(option -> new SliderControl(option, min, max, step, formatter))
                .setBinding(setter, getter);
        if (enabled != null) builder.setEnabledPredicate(enabled);
        if (impact != null) builder.setImpact(impact);
        return builder.build();
    }

    /**
     * Animation settings page
     */
    public static OptionPage animation() {
        List<OptionGroup> groups = new ArrayList<>();

        OptionImpl<CeleritasExtraGameOptions, Boolean> allAnimations = booleanOption("celeritasextra.option.animations.all",
                (opts, v) -> opts.animationSettings.animation = v,
                opts -> opts.animationSettings.animation,
                OptionFlag.REQUIRES_ASSET_RELOAD, OptionImpact.MEDIUM);
        BooleanSupplier animationsOn = () -> allAnimations.getValue();

        groups.add(OptionGroup.createBuilder()
                .add(allAnimations)
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(booleanOption("celeritasextra.option.animations.water",
                        (opts, v) -> opts.animationSettings.water = v,
                        opts -> opts.animationSettings.water,
                        OptionFlag.REQUIRES_ASSET_RELOAD, animationsOn))
                .add(booleanOption("celeritasextra.option.animations.lava",
                        (opts, v) -> opts.animationSettings.lava = v,
                        opts -> opts.animationSettings.lava,
                        OptionFlag.REQUIRES_ASSET_RELOAD, animationsOn))
                .add(booleanOption("celeritasextra.option.animations.fire",
                        (opts, v) -> opts.animationSettings.fire = v,
                        opts -> opts.animationSettings.fire,
                        OptionFlag.REQUIRES_ASSET_RELOAD, animationsOn))
                .add(booleanOption("celeritasextra.option.animations.portal",
                        (opts, v) -> opts.animationSettings.portal = v,
                        opts -> opts.animationSettings.portal,
                        OptionFlag.REQUIRES_ASSET_RELOAD, animationsOn))
                .add(booleanOption("celeritasextra.option.animations.block",
                        (opts, v) -> opts.animationSettings.blockAnimations = v,
                        opts -> opts.animationSettings.blockAnimations,
                        OptionFlag.REQUIRES_ASSET_RELOAD, animationsOn))
                .build());

        return new OptionPage(CeleritasExtraOptionPages.ANIMATION, TextComponent.literal(I18n.format("celeritasextra.option.page.animations")), ImmutableList.copyOf(groups));
    }

    /**
     * Particle settings page
     */
    public static OptionPage particle() {
        List<OptionGroup> groups = new ArrayList<>();

        OptionImpl<CeleritasExtraGameOptions, Boolean> allParticles = booleanOption("celeritasextra.option.particles.all",
                (opts, v) -> opts.particleSettings.particles = v,
                opts -> opts.particleSettings.particles,
                OptionImpact.HIGH);
        BooleanSupplier particlesOn = () -> allParticles.getValue();

        groups.add(OptionGroup.createBuilder()
                .add(allParticles)
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(booleanOption("celeritasextra.option.particles.rain_splash",
                        (opts, v) -> opts.particleSettings.rainSplash = v,
                        opts -> opts.particleSettings.rainSplash,
                        particlesOn))
                .add(booleanOption("celeritasextra.option.particles.block_break",
                        (opts, v) -> opts.particleSettings.blockBreak = v,
                        opts -> opts.particleSettings.blockBreak,
                        particlesOn))
                .add(booleanOption("celeritasextra.option.particles.block_breaking",
                        (opts, v) -> opts.particleSettings.blockBreaking = v,
                        opts -> opts.particleSettings.blockBreaking,
                        particlesOn))
                .build());

        // Dynamic per-class particle controls, grouped by mod. Discovery normally happens at
        // world load; re-scanning here is a defensive supplement. Guarded so a discovery failure
        // can never abort the page build and drop the static toggles above (or later pages).
        try {
            ParticleClassRegistry registry = ParticleClassRegistry.getInstance();
            registry.scanFactories(Minecraft.getMinecraft().effectRenderer);

            var discovered = registry.getDiscoveredClasses();
            if (!discovered.isEmpty()) {
                var byMod = new TreeMap<String, List<Map.Entry<String, String>>>();
                for (var entry : discovered.entrySet()) {
                    String fullName = entry.getKey();
                    String modId = registry.getModName(fullName);
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
                                        (opts, value) -> registry.setClassEnabled(fullClassName, value),
                                        opts -> !registry.isClassDisabled(fullClassName)
                                )
                                .setEnabledPredicate(particlesOn)
                                .build()
                        );
                    }
                    groups.add(groupBuilder.build());
                }
            }
        } catch (Throwable t) {
            CeleritasExtraMod.LOGGER.warn("Failed to build dynamic particle toggles", t);
        }

        return new OptionPage(CeleritasExtraOptionPages.PARTICLE, TextComponent.literal(I18n.format("celeritasextra.option.page.particles")), ImmutableList.copyOf(groups));
    }

    /**
     * Detail settings page
     */
    public static OptionPage details() {
        List<OptionGroup> groups = new ArrayList<>();

        OptionImpl<CeleritasExtraGameOptions, Boolean> starsOption = booleanOption("celeritasextra.option.details.stars",
                (opts, v) -> opts.detailSettings.stars = v,
                opts -> opts.detailSettings.stars,
                OptionFlag.REQUIRES_RENDERER_RELOAD);
        BooleanSupplier starsOn = () -> starsOption.getValue();

        groups.add(OptionGroup.createBuilder()
                .add(booleanOption("celeritasextra.option.details.sky",
                        (opts, v) -> opts.detailSettings.sky = v,
                        opts -> opts.detailSettings.sky,
                        OptionFlag.REQUIRES_RENDERER_RELOAD))
                .add(starsOption)
                .add(sliderOption("celeritasextra.option.details.total_stars",
                        500, 32000, 500, ControlValueFormatter.number(),
                        (opts, v) -> opts.detailSettings.totalStars = v,
                        opts -> opts.detailSettings.totalStars,
                        starsOn, OptionImpact.MEDIUM))
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

        OptionImpl<CeleritasExtraGameOptions, Boolean> fogOption = booleanOption("celeritasextra.option.render.fog",
                (opts, v) -> opts.renderSettings.fog = v,
                opts -> opts.renderSettings.fog);
        BooleanSupplier fogOn = () -> fogOption.getValue();

        OptionImpl<CeleritasExtraGameOptions, Boolean> cloudsOption = booleanOption("celeritasextra.option.render.clouds",
                (opts, v) -> opts.renderSettings.clouds = v,
                opts -> opts.renderSettings.clouds);
        BooleanSupplier cloudsOn = () -> cloudsOption.getValue();

        OptionImpl<CeleritasExtraGameOptions, Boolean> beaconsOption = booleanOption("celeritasextra.option.render.beacons",
                (opts, v) -> opts.renderSettings.beacons = v,
                opts -> opts.renderSettings.beacons);
        BooleanSupplier beaconsOn = () -> beaconsOption.getValue();

        OptionImpl<CeleritasExtraGameOptions, Boolean> itemFramesOption = booleanOption("celeritasextra.option.render.item_frames",
                (opts, v) -> opts.renderSettings.itemFrames = v,
                opts -> opts.renderSettings.itemFrames);
        BooleanSupplier itemFramesOn = () -> itemFramesOption.getValue();

        groups.add(OptionGroup.createBuilder()
                .add(fogOption)
                .add(sliderOption("celeritasextra.option.render.fog_start",
                        0, 200, 10, ControlValueFormatter.percentage(),
                        (opts, v) -> opts.renderSettings.fogStart = v,
                        opts -> opts.renderSettings.fogStart,
                        fogOn))
                .add(sliderOption("celeritasextra.option.render.fog_distance",
                        0, 32, 1, ControlValueFormatter.quantityOrDisabled("chunks", "Default"),
                        (opts, v) -> opts.renderSettings.fogDistance = v,
                        opts -> opts.renderSettings.fogDistance,
                        fogOn))
                .add(booleanOption("celeritasextra.option.render.prevent_shaders",
                        (opts, v) -> opts.renderSettings.preventShaders = v,
                        opts -> opts.renderSettings.preventShaders))
                .add(cloudsOption)
                .add(sliderOption("celeritasextra.option.render.cloud_height",
                        0, 384, 16, v -> TextComponent.literal(v + " blocks"),
                        (opts, v) -> opts.renderSettings.cloudHeight = v,
                        opts -> opts.renderSettings.cloudHeight,
                        cloudsOn))
                .add(sliderOption("celeritasextra.option.render.cloud_distance",
                        0, 128, 1, ControlValueFormatter.quantityOrDisabled("chunks", "Default"),
                        (opts, v) -> opts.renderSettings.cloudDistance = v,
                        opts -> opts.renderSettings.cloudDistance,
                        cloudsOn, OptionImpact.HIGH))
                .add(sliderOption("celeritasextra.option.render.cloud_scale",
                        1, 4, 1, v -> TextComponent.literal(String.format(Locale.ROOT, "%.2fx", v / 4.0f)),
                        (opts, v) -> opts.renderSettings.cloudScale = v,
                        opts -> opts.renderSettings.cloudScale,
                        cloudsOn))
                .add(OptionImpl.createBuilder(CeleritasExtraGameOptions.CloudTranslucency.class, celeritasExtraOpts)
                        .setName(TextComponent.literal(I18n.format("celeritasextra.option.render.cloud_translucency")))
                        .setTooltip(TextComponent.literal(I18n.format("celeritasextra.option.render.cloud_translucency.tooltip")))
                        .setControl(option -> new CyclingControl<>(option, CeleritasExtraGameOptions.CloudTranslucency.class,
                                new TextComponent[]{
                                        TextComponent.literal(CeleritasExtraGameOptions.CloudTranslucency.DEFAULT.getLocalizedName()),
                                        TextComponent.literal(CeleritasExtraGameOptions.CloudTranslucency.ALWAYS.getLocalizedName()),
                                        TextComponent.literal(CeleritasExtraGameOptions.CloudTranslucency.NEVER.getLocalizedName())
                                }))
                        .setBinding((opts, value) -> opts.renderSettings.cloudTranslucency = value,
                                opts -> opts.renderSettings.cloudTranslucency)
                        .setEnabledPredicate(cloudsOn)
                        .build())
                .add(booleanOption("celeritasextra.option.render.light_updates",
                        (opts, v) -> opts.renderSettings.lightUpdates = v,
                        opts -> opts.renderSettings.lightUpdates,
                        OptionImpact.HIGH))
                .add(itemFramesOption)
                .add(booleanOption("celeritasextra.option.render.armor_stands",
                        (opts, v) -> opts.renderSettings.armorStands = v,
                        opts -> opts.renderSettings.armorStands))
                .add(booleanOption("celeritasextra.option.render.paintings",
                        (opts, v) -> opts.renderSettings.paintings = v,
                        opts -> opts.renderSettings.paintings))
                .add(beaconsOption)
                .add(booleanOption("celeritasextra.option.render.limit_beacon_beam_height",
                        (opts, v) -> opts.renderSettings.limitBeaconBeamHeight = v,
                        opts -> opts.renderSettings.limitBeaconBeamHeight,
                        beaconsOn))
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
                        opts -> opts.renderSettings.itemFrameNameTag,
                        itemFramesOn))
                .build());

        return new OptionPage(CeleritasExtraOptionPages.RENDER, TextComponent.literal(I18n.format("celeritasextra.option.page.render")), ImmutableList.copyOf(groups));
    }

    /**
     * Extra settings page
     */
    public static OptionPage extra() {
        List<OptionGroup> groups = new ArrayList<>();

        // Overlay settings group
        OptionImpl<CeleritasExtraGameOptions, Boolean> showFpsOption = booleanOption("celeritasextra.option.extra.fps",
                (opts, v) -> opts.extraSettings.showFps = v,
                opts -> opts.extraSettings.showFps);
        BooleanSupplier fpsOn = () -> showFpsOption.getValue();

        groups.add(OptionGroup.createBuilder()
                .add(showFpsOption)
                .add(booleanOption("celeritasextra.option.extra.fps_extended",
                        (opts, v) -> opts.extraSettings.showFPSExtended = v,
                        opts -> opts.extraSettings.showFPSExtended,
                        fpsOn))
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
        OptionImpl<CeleritasExtraGameOptions, Boolean> steadyHudOption = booleanOption("celeritasextra.option.extra.steady_debug_hud",
                (opts, v) -> opts.extraSettings.steadyDebugHud = v,
                opts -> opts.extraSettings.steadyDebugHud);
        BooleanSupplier steadyHudOn = () -> steadyHudOption.getValue();

        groups.add(OptionGroup.createBuilder()
                .add(booleanOption("celeritasextra.option.extra.reduced_motion",
                        (opts, v) -> opts.extraSettings.reducedMotion = v,
                        opts -> opts.extraSettings.reducedMotion))
                .add(steadyHudOption)
                .add(sliderOption("celeritasextra.option.extra.steady_debug_hud_refresh",
                        1, 60, 1, v -> TextComponent.literal(v + " ticks"),
                        (opts, v) -> opts.extraSettings.steadyDebugHudRefreshInterval = v,
                        opts -> opts.extraSettings.steadyDebugHudRefreshInterval,
                        steadyHudOn))
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
