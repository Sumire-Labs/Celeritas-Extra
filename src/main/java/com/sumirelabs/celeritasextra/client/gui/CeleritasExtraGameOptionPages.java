package com.sumirelabs.celeritasextra.client.gui;

import com.google.common.collect.ImmutableList;
import com.sumirelabs.celeritasextra.CeleritasExtraMod;
import com.sumirelabs.celeritasextra.client.particle.ParticleClassRegistry;
import com.sumirelabs.celeritasextra.client.render.cloud.ModernCloudAssets;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.DimensionManager;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.taumc.celeritas.api.options.control.ControlValueFormatter;
import org.taumc.celeritas.api.options.control.CyclingControl;
import org.taumc.celeritas.api.options.control.SliderControl;
import org.taumc.celeritas.api.options.control.TickBoxControl;
import org.taumc.celeritas.api.options.OptionIdentifier;
import org.taumc.celeritas.api.options.structure.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * Builds the Celeritas Extra option pages shown in the Celeritas video-settings UI.
 * <p>
 * The page factories assemble purpose-specific pages from the
 * reusable {@link #booleanOption} and {@link #sliderOption} builders. Options bind directly to the
 * fields of {@link CeleritasExtraGameOptions} through {@link CeleritasExtraOptionsStorage}, and
 * dependent controls use {@code enabled} predicates so sub-options grey out while their parent is off.
 */
public class CeleritasExtraGameOptionPages {

    private static OptionGroup.Builder group(String id) {
        return OptionGroup.createBuilder().setId(OptionIdentifier.create("celeritasextra", id));
    }

    private record OptionContext(String label, String scope) {
        private static final OptionContext NONE = new OptionContext("", "");
    }

    private static <T> OptionIdentifier<T> optionId(String key, Class<T> type, OptionContext context) {
        return OptionIdentifier.create("celeritasextra", context.scope() + key.toLowerCase(Locale.ROOT).replace('$', '/'), type);
    }

    private static final CeleritasExtraOptionsStorage celeritasExtraOpts = new CeleritasExtraOptionsStorage();

    /**
     * Plain toggle with no flag, performance impact, or enable gate.
     */
    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter) {
        return booleanOption(translationKey, setter, getter, null, null, null);
    }

    /**
     * Toggle carrying a performance {@link OptionImpact} hint.
     */
    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            OptionImpact impact) {
        return booleanOption(translationKey, setter, getter, null, impact, null);
    }

    /**
     * Toggle carrying an {@link OptionFlag} (e.g. an asset or renderer reload on change).
     */
    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            OptionFlag flag) {
        return booleanOption(translationKey, setter, getter, flag, null, null);
    }

    /**
     * Toggle carrying both an {@link OptionFlag} and a performance {@link OptionImpact} hint.
     */
    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            OptionFlag flag,
            OptionImpact impact) {
        return booleanOption(translationKey, setter, getter, flag, impact, null);
    }

    /**
     * Enable-gated toggle: greyed out (live) while {@code enabled} returns false.
     */
    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            BooleanSupplier enabled) {
        return booleanOption(translationKey, setter, getter, null, null, enabled);
    }

    /**
     * Enable-gated toggle carrying a performance-impact hint.
     */
    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            OptionImpact impact,
            BooleanSupplier enabled) {
        return booleanOption(translationKey, setter, getter, null, impact, enabled);
    }

    /**
     * Enable-gated toggle with a flag (e.g. asset reload).
     */
    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            OptionFlag flag,
            BooleanSupplier enabled) {
        return booleanOption(translationKey, setter, getter, flag, null, enabled);
    }

    /**
     * Canonical toggle builder that every other {@code booleanOption} overload delegates to.
     * <p>
     * Binds a {@link TickBoxControl} to the given getter/setter, taking the localized name and tooltip
     * from {@code translationKey} (the tooltip uses its {@code .tooltip} suffix) and applying the
     * optional flag, impact, and enable predicate when present.
     *
     * @param translationKey lang key for the option name and, via its {@code .tooltip} suffix, its tooltip
     * @param flag           optional {@link OptionFlag} applied when the value changes, or {@code null}
     * @param impact         optional performance-impact hint, or {@code null}
     * @param enabled        optional predicate; when it returns false the control is shown greyed out
     * @return the built option
     */
    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            OptionFlag flag,
            OptionImpact impact,
            BooleanSupplier enabled) {
        return booleanOption(translationKey, Translations.tooltipKey(translationKey), setter, getter, flag, impact, enabled);
    }

    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey, String tooltipKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            OptionFlag flag, OptionImpact impact, BooleanSupplier enabled) {
        return booleanOption(translationKey, tooltipKey, setter, getter, flag, impact, enabled, OptionContext.NONE);
    }

    private static OptionImpl<CeleritasExtraGameOptions, Boolean> booleanOption(
            String translationKey, String tooltipKey,
            BiConsumer<CeleritasExtraGameOptions, Boolean> setter,
            Function<CeleritasExtraGameOptions, Boolean> getter,
            OptionFlag flag, OptionImpact impact, BooleanSupplier enabled, OptionContext context) {
        var builder = OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                .setId(optionId(translationKey, boolean.class, context))
                .setName(TextComponent.literal(contextualText(context, translationKey, " · ")))
                .setTooltip(TextComponent.literal(contextualText(context, tooltipKey, "\n")))
                .setControl(TickBoxControl::new)
                .setBinding(setter, getter);
        if (flag != null) builder.setFlags(flag);
        if (impact != null) builder.setImpact(impact);
        if (enabled != null) builder.setEnabledPredicate(enabled);
        return builder.build();
    }

    /**
     * Integer slider with no enable gate or performance-impact hint.
     */
    private static OptionImpl<CeleritasExtraGameOptions, Integer> sliderOption(
            String translationKey,
            int min, int max, int step,
            ControlValueFormatter formatter,
            BiConsumer<CeleritasExtraGameOptions, Integer> setter,
            Function<CeleritasExtraGameOptions, Integer> getter) {
        return sliderOption(translationKey, min, max, step, formatter, setter, getter, null, null);
    }

    /**
     * Integer slider gated by an {@code enabled} predicate.
     */
    private static OptionImpl<CeleritasExtraGameOptions, Integer> sliderOption(
            String translationKey,
            int min, int max, int step,
            ControlValueFormatter formatter,
            BiConsumer<CeleritasExtraGameOptions, Integer> setter,
            Function<CeleritasExtraGameOptions, Integer> getter,
            BooleanSupplier enabled) {
        return sliderOption(translationKey, min, max, step, formatter, setter, getter, enabled, null);
    }

    /**
     * Canonical integer-slider builder that the other {@code sliderOption} overloads delegate to.
     * <p>
     * Binds a {@link SliderControl} over the inclusive {@code [min, max]} range (in increments of
     * {@code step}) to the given getter/setter, taking the localized name and tooltip from
     * {@code translationKey} and applying the optional enable predicate and impact hint when present.
     *
     * @param translationKey lang key for the option name and, via its {@code .tooltip} suffix, its tooltip
     * @param min            inclusive minimum slider value
     * @param max            inclusive maximum slider value
     * @param step           increment between selectable values
     * @param formatter      renders the current value for display
     * @param enabled        optional predicate; when it returns false the slider is shown greyed out
     * @param impact         optional performance-impact hint
     * @return the built option
     */
    private static OptionImpl<CeleritasExtraGameOptions, Integer> sliderOption(
            String translationKey,
            int min, int max, int step,
            ControlValueFormatter formatter,
            BiConsumer<CeleritasExtraGameOptions, Integer> setter,
            Function<CeleritasExtraGameOptions, Integer> getter,
            BooleanSupplier enabled,
            OptionImpact impact) {
        return sliderOption(translationKey, min, max, step, formatter, setter, getter,
                enabled, impact, null);
    }

    private static OptionImpl<CeleritasExtraGameOptions, Integer> sliderOption(
            String translationKey,
            int min, int max, int step,
            ControlValueFormatter formatter,
            BiConsumer<CeleritasExtraGameOptions, Integer> setter,
            Function<CeleritasExtraGameOptions, Integer> getter,
            BooleanSupplier enabled,
            OptionImpact impact,
            OptionFlag flag) {
        return sliderOption(translationKey, min, max, step, formatter, setter, getter, enabled, impact, flag, OptionContext.NONE);
    }

    private static OptionImpl<CeleritasExtraGameOptions, Integer> sliderOption(
            String translationKey, int min, int max, int step, ControlValueFormatter formatter,
            BiConsumer<CeleritasExtraGameOptions, Integer> setter,
            Function<CeleritasExtraGameOptions, Integer> getter,
            BooleanSupplier enabled, OptionImpact impact, OptionFlag flag, OptionContext context) {
        var builder = OptionImpl.createBuilder(int.class, celeritasExtraOpts)
                .setId(optionId(translationKey, int.class, context))
                .setName(TextComponent.literal(contextualText(context, translationKey, " · ")))
                .setTooltip(TextComponent.literal(contextualText(context, Translations.tooltipKey(translationKey), "\n")))
                .setControl(option -> new SliderControl(option, min, max, step, formatter))
                .setBinding(setter, getter);
        if (enabled != null) builder.setEnabledPredicate(enabled);
        if (impact != null) builder.setImpact(impact);
        if (flag != null) builder.setFlags(flag);
        return builder.build();
    }

    private static String contextualText(OptionContext context, String key, String separator) {
        return context.label().isEmpty() ? Translations.format(key) : context.label() + separator + Translations.format(key);
    }

    private static ControlValueFormatter quantityOrDefault(String unitKey, String zeroKey) {
        return value -> TextComponent.literal(value == 0 ? Translations.format(zeroKey) : Translations.format(unitKey, value));
    }

    /**
     * Builds the Animations page: a master toggle gating per-type animation switches (water, lava,
     * fire, portal, and block animations), all flagged to reload assets when changed.
     *
     * @return the assembled animations option page
     */
    public static OptionPage animation() {
        List<OptionGroup> groups = new ArrayList<>();

        OptionImpl<CeleritasExtraGameOptions, Boolean> allAnimations = booleanOption("gui.all", "sodium-extra.option.animations_all.tooltip",
                (opts, v) -> opts.animationSettings.animation = v,
                opts -> opts.animationSettings.animation,
                OptionFlag.REQUIRES_ASSET_RELOAD, OptionImpact.MEDIUM, null, new OptionContext("", "animation/"));
        BooleanSupplier animationsOn = () -> allAnimations.getValue();

        groups.add(group("groups/animation/0")
                .add(allAnimations)
                .build());

        groups.add(group("groups/animation/1")
                .add(booleanOption("tile.water.name",
                        (opts, v) -> opts.animationSettings.water = v,
                        opts -> opts.animationSettings.water,
                        OptionFlag.REQUIRES_ASSET_RELOAD, animationsOn))
                .add(booleanOption("tile.lava.name",
                        (opts, v) -> opts.animationSettings.lava = v,
                        opts -> opts.animationSettings.lava,
                        OptionFlag.REQUIRES_ASSET_RELOAD, animationsOn))
                .add(booleanOption("tile.fire.name",
                        (opts, v) -> opts.animationSettings.fire = v,
                        opts -> opts.animationSettings.fire,
                        OptionFlag.REQUIRES_ASSET_RELOAD, animationsOn))
                .add(booleanOption("tile.portal.name",
                        (opts, v) -> opts.animationSettings.portal = v,
                        opts -> opts.animationSettings.portal,
                        OptionFlag.REQUIRES_ASSET_RELOAD, animationsOn))
                .add(booleanOption("sodium-extra.option.block_animations",
                        (opts, v) -> opts.animationSettings.blockAnimations = v,
                        opts -> opts.animationSettings.blockAnimations,
                        OptionFlag.REQUIRES_ASSET_RELOAD, animationsOn))
                .build());

        return new OptionPage(CeleritasExtraOptionPages.ANIMATION, TextComponent.literal(Translations.format("sodium-extra.option.animations")), ImmutableList.copyOf(groups));
    }

    /**
     * Builds the Particles page: the master and built-in particle toggles, followed by dynamically
     * discovered per-class toggles grouped by owning mod.
     * <p>
     * Particle-class discovery is re-run defensively here and wrapped in a guard, so a discovery
     * failure cannot abort the page build or drop the static toggles above it.
     *
     * @return the assembled particles option page
     */
    public static OptionPage particle() {
        List<OptionGroup> groups = new ArrayList<>();

        OptionImpl<CeleritasExtraGameOptions, Boolean> allParticles = booleanOption("gui.all", "sodium-extra.option.particles_all.tooltip",
                (opts, v) -> opts.particleSettings.particles = v,
                opts -> opts.particleSettings.particles,
                null, OptionImpact.HIGH, null, new OptionContext("", "particle/"));
        BooleanSupplier particlesOn = () -> allParticles.getValue();

        groups.add(group("groups/particle/0")
                .add(allParticles)
                .build());

        groups.add(group("groups/particle/1")
                .add(booleanOption("subtitles.weather.rain",
                        (opts, v) -> opts.particleSettings.rainSplash = v,
                        opts -> opts.particleSettings.rainSplash,
                        particlesOn))
                .add(booleanOption("subtitles.block.generic.break",
                        (opts, v) -> opts.particleSettings.blockBreak = v,
                        opts -> opts.particleSettings.blockBreak,
                        particlesOn))
                .add(booleanOption("subtitles.block.generic.hit",
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
                    var groupBuilder = group("groups/particle/mod/" + modId);
                    var classEntries = modEntry.getValue();
                    classEntries.sort(Comparator.comparing(Map.Entry::getValue));

                    for (var classEntry : classEntries) {
                        var fullClassName = classEntry.getKey();
                        var simpleClassName = classEntry.getValue();
                        String displayName = simpleClassName + " (" + modId + ")";

                        groupBuilder.add(OptionImpl.createBuilder(int.class, celeritasExtraOpts)
                                .setId(optionId("particle/" + fullClassName, int.class, OptionContext.NONE))
                                .setName(TextComponent.literal(displayName))
                                .setTooltip(TextComponent.literal(
                                        Translations.format("celeritasextra.option.particle_spawn_percentage.tooltip", simpleClassName)
                                                + "\n" + fullClassName))
                                .setControl(option -> new SliderControl(option, 0, 100, 1, ControlValueFormatter.percentage()))
                                .setBinding(
                                        (opts, value) -> registry.setSpawnPercentage(fullClassName, value),
                                        opts -> registry.getSpawnPercentage(fullClassName)
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

        return new OptionPage(CeleritasExtraOptionPages.PARTICLE, TextComponent.literal(Translations.format("options.particles")), ImmutableList.copyOf(groups));
    }

    public static OptionPage sky() {
        List<OptionGroup> groups = new ArrayList<>();

        OptionImpl<CeleritasExtraGameOptions, Boolean> starsOption = booleanOption("sodium-extra.option.stars",
                (opts, v) -> opts.detailSettings.stars = v,
                opts -> opts.detailSettings.stars,
                OptionFlag.REQUIRES_RENDERER_RELOAD);
        BooleanSupplier starsOn = () -> starsOption.getValue();

        groups.add(group("groups/sky/0")
                .add(booleanOption("sodium-extra.option.sky",
                        (opts, v) -> opts.detailSettings.sky = v,
                        opts -> opts.detailSettings.sky,
                        OptionFlag.REQUIRES_RENDERER_RELOAD))
                .add(booleanOption("sodium-extra.option.sun",
                        (opts, v) -> opts.detailSettings.sun = v,
                        opts -> opts.detailSettings.sun))
                .add(booleanOption("sodium-extra.option.moon",
                        (opts, v) -> opts.detailSettings.moon = v,
                        opts -> opts.detailSettings.moon))
                .add(starsOption)
                .add(sliderOption("options.total_stars",
                        500, 32000, 500, ControlValueFormatter.number(),
                        (opts, v) -> opts.detailSettings.totalStars = v,
                        opts -> opts.detailSettings.totalStars,
                        starsOn, OptionImpact.MEDIUM, OptionFlag.REQUIRES_RENDERER_RELOAD))
                .build());

        groups.add(group("groups/sky/1")
                .add(booleanOption("soundCategory.weather",
                        (opts, v) -> opts.detailSettings.rainSnow = v,
                        opts -> opts.detailSettings.rainSnow))
                .add(booleanOption("sodium-extra.option.sky_colors",
                        (opts, v) -> opts.detailSettings.skyColors = v,
                        opts -> opts.detailSettings.skyColors))
                .add(booleanOption("sodium-extra.option.biome_colors",
                        (opts, v) -> opts.detailSettings.biomeColors = v,
                        opts -> opts.detailSettings.biomeColors,
                        OptionFlag.REQUIRES_RENDERER_RELOAD))
                .build());

        return new OptionPage(CeleritasExtraOptionPages.SKY, TextComponent.literal(Translations.format("celeritasextra.option.page.sky_weather")), ImmutableList.copyOf(groups));
    }

    public static OptionPage clouds() {
        List<OptionGroup> groups = new ArrayList<>();

        OptionImpl<CeleritasExtraGameOptions, Boolean> cloudsOption = booleanOption("options.renderClouds",
                (opts, v) -> opts.renderSettings.clouds = v,
                opts -> opts.renderSettings.clouds);
        BooleanSupplier cloudsOn = () -> cloudsOption.getValue();

        OptionImpl<CeleritasExtraGameOptions, Boolean> modernCloudsOption = booleanOption(
                "celeritasextra.option.modern_clouds",
                (opts, v) -> opts.renderSettings.modernClouds = v,
                opts -> opts.renderSettings.modernClouds,
                OptionFlag.REQUIRES_ASSET_RELOAD,
                () -> cloudsOn.getAsBoolean() && ModernCloudAssets.isAvailable());

        groups.add(group("groups/clouds/0")
                .add(cloudsOption)
                .add(modernCloudsOption)
                .build());

        groups.add(group("groups/clouds/1")
                .add(sliderOption("sodium-extra.option.cloud_height",
                        CeleritasExtraGameOptions.RenderSettings.USE_WORLD_CLOUD_HEIGHT, 384, 16,
                        v -> TextComponent.literal(v < 0 ? Translations.format("generator.default") : Translations.format("sodium-extra.units.blocks", v)),
                        (opts, v) -> opts.renderSettings.cloudHeight = v,
                        opts -> opts.renderSettings.cloudHeight,
                        cloudsOn))
                .add(sliderOption("sodium-extra.option.cloud_distance",
                        0, 128, 1, quantityOrDefault("options.chunks", "generator.default"),
                        (opts, v) -> opts.renderSettings.cloudDistance = v,
                        opts -> opts.renderSettings.cloudDistance,
                        cloudsOn, OptionImpact.HIGH))
                .add(sliderOption("options.cloud_scale",
                        CeleritasExtraGameOptions.RenderSettings.CLOUD_SCALE_MIN,
                        CeleritasExtraGameOptions.RenderSettings.CLOUD_SCALE_MAX,
                        1, v -> TextComponent.literal(String.format(Locale.ROOT, "%.2fx",
                                (float) v / CeleritasExtraGameOptions.RenderSettings.CLOUD_SCALE_VANILLA)),
                        (opts, v) -> opts.renderSettings.cloudScale = v,
                        opts -> opts.renderSettings.cloudScale,
                        cloudsOn))
                .add(OptionImpl.createBuilder(CeleritasExtraGameOptions.CloudTranslucency.class, celeritasExtraOpts)
                        .setId(optionId("cloud_translucency", CeleritasExtraGameOptions.CloudTranslucency.class, OptionContext.NONE))
                        .setName(TextComponent.literal(Translations.format("options.mode_cloud_translucency")))
                        .setTooltip(TextComponent.literal(Translations.format("options.mode_cloud_translucency.tooltip")))
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
                .build());

        return new OptionPage(CeleritasExtraOptionPages.CLOUDS, TextComponent.literal(Translations.format("celeritasextra.option.page.clouds")), ImmutableList.copyOf(groups));
    }

    public static OptionPage fog() {
        List<OptionGroup> groups = new ArrayList<>();

        OptionImpl<CeleritasExtraGameOptions, Boolean> fogOption = booleanOption("sodium-extra.option.fog_type.atmospheric",
                (opts, v) -> opts.renderSettings.fog = v,
                opts -> opts.renderSettings.fog);
        BooleanSupplier fogOn = () -> fogOption.getValue();

        groups.add(group("groups/fog/0")
                .add(fogOption)
                .add(sliderOption("sodium-extra.option.fog_start",
                        0, 200, 10, ControlValueFormatter.percentage(),
                        (opts, v) -> opts.renderSettings.fogStart = v,
                        opts -> opts.renderSettings.fogStart,
                        fogOn))
                .add(sliderOption("sodium-extra.option.fog_distance",
                        0, 32, 1, quantityOrDefault("options.chunks", "generator.default"),
                        (opts, v) -> opts.renderSettings.fogDistance = v,
                        opts -> opts.renderSettings.fogDistance,
                        fogOn))
                .build());

        groups.add(group("groups/fog/1")
                .add(booleanOption("options.void_fog",
                        (opts, v) -> opts.detailSettings.voidFog = v,
                        opts -> opts.detailSettings.voidFog))
                .build());

        return new OptionPage(CeleritasExtraOptionPages.FOG, TextComponent.literal(Translations.format("celeritasextra.option.page.fog")), ImmutableList.copyOf(groups));
    }

    public static OptionPage dimensionFog() {
        List<OptionGroup> groups = new ArrayList<>();

        // Include vanilla, registered mod dimensions, saved overrides, and the current server dimension.
        Set<Integer> dimensions = new TreeSet<>(List.of(-1, 0, 1));
        dimensions.addAll(Arrays.asList(DimensionManager.getStaticDimensionIDs()));
        dimensions.addAll(celeritasExtraOpts.getData().renderSettings.dimensionFogOverrides.keySet());
        if (Minecraft.getMinecraft().world != null) dimensions.add(Minecraft.getMinecraft().world.provider.getDimension());
        for (int dimension : dimensions) {
            String dimensionName = switch (dimension) {
                case -1 -> Translations.format("options.dimensions.minecraft.the_nether");
                case 0 -> Translations.format("options.dimensions.minecraft.overworld");
                case 1 -> Translations.format("options.dimensions.minecraft.the_end");
                default -> DimensionManager.isDimensionRegistered(dimension)
                        ? DimensionManager.getProviderType(dimension).getName() : Integer.toString(dimension);
            };
            dimensionName += " (" + dimension + ")";
            var context = new OptionContext(dimensionName, "dimension/" + dimension + "/");
            var override = OptionImpl.createBuilder(boolean.class, celeritasExtraOpts)
                    .setId(optionId("fog_override", boolean.class, context))
                    .setName(TextComponent.literal(Translations.format("celeritasextra.option.dimension_fog_override", dimensionName)))
                    .setTooltip(TextComponent.literal(Translations.format("celeritasextra.option.dimension_fog_override.tooltip")))
                    .setControl(TickBoxControl::new)
                    .setBinding((opts, value) -> opts.renderSettings.dimensionFog(dimension).override = value,
                            opts -> opts.renderSettings.dimensionFog(dimension).override)
                    .build();
            BooleanSupplier overriding = override::getValue;
            var enabled = booleanOption("sodium-extra.option.fog_type.atmospheric", "sodium-extra.option.fog_type.atmospheric.tooltip",
                    (opts, value) -> opts.renderSettings.dimensionFog(dimension).fog = value,
                    opts -> opts.renderSettings.dimensionFog(dimension).fog, null, null, overriding, context);
            BooleanSupplier customFogOn = () -> override.getValue() && enabled.getValue();
            groups.add(group("groups/dimension_fog/" + dimension).add(override).add(enabled)
                    .add(sliderOption("sodium-extra.option.fog_start", 0, 200, 10, ControlValueFormatter.percentage(),
                            (opts, value) -> opts.renderSettings.dimensionFog(dimension).start = value,
                            opts -> opts.renderSettings.dimensionFog(dimension).start, customFogOn, null, null, context))
                    .add(sliderOption("sodium-extra.option.fog_distance", 0, 32, 1,
                            quantityOrDefault("options.chunks", "generator.default"),
                            (opts, value) -> opts.renderSettings.dimensionFog(dimension).distance = value,
                            opts -> opts.renderSettings.dimensionFog(dimension).distance, customFogOn, null, null, context)).build());
        }
        return new OptionPage(CeleritasExtraOptionPages.DIMENSION_FOG, TextComponent.literal(Translations.format("celeritasextra.option.page.dimension_fog")), ImmutableList.copyOf(groups));
    }

    public static OptionPage entities() {
        List<OptionGroup> groups = new ArrayList<>();

        OptionImpl<CeleritasExtraGameOptions, Boolean> itemFramesOption = booleanOption("item.frame.name",
                (opts, v) -> opts.renderSettings.itemFrames = v,
                opts -> opts.renderSettings.itemFrames);
        BooleanSupplier itemFramesOn = () -> itemFramesOption.getValue();

        groups.add(group("groups/entities/0")
                .add(itemFramesOption)
                .add(sliderOption("moreculling.config.option.itemFrameLODRange",
                        0, 256, 1, quantityOrDefault("sodium-extra.units.blocks", "options.off"),
                        (opts, v) -> opts.renderSettings.itemFrameLodDistance = v,
                        opts -> opts.renderSettings.itemFrameLodDistance,
                        itemFramesOn, OptionImpact.LOW))
                .add(booleanOption("moreculling.config.option.itemFrameMapCulling",
                        (opts, v) -> opts.renderSettings.mapBackFaceCulling = v,
                        opts -> opts.renderSettings.mapBackFaceCulling, itemFramesOn))
                .add(booleanOption("sodium-extra.option.item_frame_name_tag",
                        (opts, v) -> opts.renderSettings.itemFrameNameTag = v,
                        opts -> opts.renderSettings.itemFrameNameTag,
                        itemFramesOn))
                .build());

        groups.add(group("groups/entities/1")
                .add(sliderOption("celeritasextra.option.entity_render_distance",
                        0, 256, 1, quantityOrDefault("sodium-extra.units.blocks", "generator.default"),
                        (opts, v) -> opts.renderSettings.entityRenderDistance = v,
                        opts -> opts.renderSettings.entityRenderDistance))
                .add(booleanOption("entity.ArmorStand.name",
                        (opts, v) -> opts.renderSettings.armorStands = v,
                        opts -> opts.renderSettings.armorStands))
                .add(booleanOption("entity.Painting.name",
                        (opts, v) -> opts.renderSettings.paintings = v,
                        opts -> opts.renderSettings.paintings))
                .add(booleanOption("sodium-extra.option.player_name_tag",
                        (opts, v) -> opts.renderSettings.playerNameTag = v,
                        opts -> opts.renderSettings.playerNameTag))
                .build());

        return new OptionPage(CeleritasExtraOptionPages.ENTITIES, TextComponent.literal(Translations.format("celeritasextra.option.page.entities")), ImmutableList.copyOf(groups));
    }

    public static OptionPage blocks() {
        List<OptionGroup> groups = new ArrayList<>();

        OptionImpl<CeleritasExtraGameOptions, Boolean> beaconsOption = booleanOption("sodium-extra.option.beacon_beam",
                (opts, v) -> opts.renderSettings.beacons = v,
                opts -> opts.renderSettings.beacons);
        BooleanSupplier beaconsOn = () -> beaconsOption.getValue();

        groups.add(group("groups/blocks/0")
                .add(sliderOption("celeritasextra.option.tile_entity_render_distance",
                        0, 256, 1, quantityOrDefault("sodium-extra.units.blocks", "generator.default"),
                        (opts, v) -> opts.renderSettings.tileEntityRenderDistance = v,
                        opts -> opts.renderSettings.tileEntityRenderDistance))
                .add(booleanOption("moreculling.config.option.signTextCulling",
                        (opts, v) -> opts.renderSettings.signTextCulling = v,
                        opts -> opts.renderSettings.signTextCulling))
                .build());

        groups.add(group("groups/blocks/1")
                .add(beaconsOption)
                .add(booleanOption("sodium-extra.option.limit_beacon_beam_height",
                        (opts, v) -> opts.renderSettings.limitBeaconBeamHeight = v,
                        opts -> opts.renderSettings.limitBeaconBeamHeight,
                        beaconsOn))
                .build());

        groups.add(group("groups/blocks/2")
                .add(booleanOption("tile.pistonBase.name",
                        (opts, v) -> opts.renderSettings.pistons = v,
                        opts -> opts.renderSettings.pistons))
                .add(booleanOption("sodium-extra.option.enchanting_table_book",
                        (opts, v) -> opts.renderSettings.enchantingTableBooks = v,
                        opts -> opts.renderSettings.enchantingTableBooks))
                .build());

        groups.add(group("groups/blocks/3")
                .add(booleanOption("sodium-extra.option.light_updates",
                        (opts, v) -> opts.renderSettings.lightUpdates = v,
                        opts -> opts.renderSettings.lightUpdates,
                        OptionImpact.HIGH))
                .build());

        return new OptionPage(CeleritasExtraOptionPages.BLOCKS, TextComponent.literal(Translations.format("celeritasextra.option.page.block_rendering")), ImmutableList.copyOf(groups));
    }

    public static OptionPage overlay() {
        List<OptionGroup> groups = new ArrayList<>();

        OptionImpl<CeleritasExtraGameOptions, Boolean> showFpsOption = booleanOption("sodium-extra.option.show_fps",
                (opts, v) -> opts.extraSettings.showFps = v,
                opts -> opts.extraSettings.showFps);
        BooleanSupplier fpsOn = () -> showFpsOption.getValue();

        OptionImpl<CeleritasExtraGameOptions, Boolean> steadyHudOption = booleanOption("sodium-extra.option.steady_debug_hud",
                (opts, v) -> opts.extraSettings.steadyDebugHud = v,
                opts -> opts.extraSettings.steadyDebugHud);
        BooleanSupplier steadyHudOn = () -> steadyHudOption.getValue();

        groups.add(group("groups/overlay/0")
                .add(showFpsOption)
                .add(booleanOption("sodium-extra.option.show_fps_extended",
                        (opts, v) -> opts.extraSettings.showFPSExtended = v,
                        opts -> opts.extraSettings.showFPSExtended,
                        fpsOn))
                .add(booleanOption("celeritasextra.option.show_memory",
                        (opts, v) -> opts.extraSettings.showMemory = v,
                        opts -> opts.extraSettings.showMemory))
                .build());

        groups.add(group("groups/overlay/1")
                .add(booleanOption("sodium-extra.option.show_coordinates",
                        (opts, v) -> opts.extraSettings.showCoords = v,
                        opts -> opts.extraSettings.showCoords))
                .add(booleanOption("celeritasextra.option.ignore_reduced_debug_info",
                        (opts, v) -> opts.extraSettings.ignoreReducedDebugInfo = v,
                        opts -> opts.extraSettings.ignoreReducedDebugInfo))
                .build());

        groups.add(group("groups/overlay/2")
                .add(OptionImpl.createBuilder(CeleritasExtraGameOptions.OverlayCorner.class, celeritasExtraOpts)
                        .setId(optionId("overlay_corner", CeleritasExtraGameOptions.OverlayCorner.class, OptionContext.NONE))
                        .setName(TextComponent.literal(Translations.format("sodium-extra.option.overlay_corner")))
                        .setTooltip(TextComponent.literal(Translations.format("sodium-extra.option.overlay_corner.tooltip")))
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
                        .setId(optionId("text_contrast", CeleritasExtraGameOptions.TextContrast.class, OptionContext.NONE))
                        .setName(TextComponent.literal(Translations.format("sodium-extra.option.text_contrast")))
                        .setTooltip(TextComponent.literal(Translations.format("sodium-extra.option.text_contrast.tooltip")))
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

        groups.add(group("groups/overlay/3")
                .add(steadyHudOption)
                .add(sliderOption("sodium-extra.option.steady_debug_hud_refresh_interval",
                        CeleritasExtraGameOptions.ExtraSettings.STEADY_DEBUG_HUD_REFRESH_MIN,
                        CeleritasExtraGameOptions.ExtraSettings.STEADY_DEBUG_HUD_REFRESH_MAX,
                        1, v -> TextComponent.literal(Translations.format("sodium-extra.units.ticks", v)),
                        (opts, v) -> opts.extraSettings.steadyDebugHudRefreshInterval = v,
                        opts -> opts.extraSettings.steadyDebugHudRefreshInterval,
                        steadyHudOn))
                .build());

        return new OptionPage(CeleritasExtraOptionPages.OVERLAY, TextComponent.literal(Translations.format("celeritasextra.option.page.overlay")), ImmutableList.copyOf(groups));
    }

    public static OptionPage misc() {
        List<OptionGroup> groups = new ArrayList<>();

        OptionImpl<CeleritasExtraGameOptions, Boolean> toastsOption = booleanOption("sodium-extra.option.toasts",
                (opts, v) -> opts.extraSettings.toasts = v,
                opts -> opts.extraSettings.toasts);
        BooleanSupplier toastsOn = () -> toastsOption.getValue();

        groups.add(group("groups/misc/0")
                .add(sliderOption("celeritasextra.option.menu_fps_limit", 0, 240, 1,
                        quantityOrDefault("sodium-extra.overlay.fps", "celeritasextra.option.menu_fps_limit.default"),
                        (opts, value) -> opts.extraSettings.menuFpsLimit = value,
                        opts -> opts.extraSettings.menuFpsLimit))
                .add(sliderOption("celeritasextra.option.inactive_fps_limit", 0, 120, 1,
                        quantityOrDefault("sodium-extra.overlay.fps", "options.off"),
                        (opts, value) -> opts.extraSettings.inactiveFpsLimit = value,
                        opts -> opts.extraSettings.inactiveFpsLimit))
                .add(sliderOption("celeritasextra.option.minimized_fps_limit", 0, 120, 1,
                        quantityOrDefault("sodium-extra.overlay.fps", "options.off"),
                        (opts, value) -> opts.extraSettings.minimizedFpsLimit = value,
                        opts -> opts.extraSettings.minimizedFpsLimit)).build());

        groups.add(group("groups/misc/1")
                .add(toastsOption)
                .add(booleanOption("sodium-extra.option.advancement_toast",
                        (opts, v) -> opts.extraSettings.toastAdvancement = v,
                        opts -> opts.extraSettings.toastAdvancement,
                        toastsOn))
                .add(booleanOption("sodium-extra.option.recipe_toast",
                        (opts, v) -> opts.extraSettings.toastRecipe = v,
                        opts -> opts.extraSettings.toastRecipe,
                        toastsOn))
                .add(booleanOption("sodium-extra.option.tutorial_toast",
                        (opts, v) -> opts.extraSettings.toastTutorial = v,
                        opts -> opts.extraSettings.toastTutorial,
                        toastsOn))
                .add(booleanOption("sodium-extra.option.system_toast",
                        (opts, v) -> opts.extraSettings.toastSystem = v,
                        opts -> opts.extraSettings.toastSystem,
                        toastsOn))
                .build());

        groups.add(group("groups/misc/2")
                .add(booleanOption("celeritasextra.option.mod_name_tooltip",
                        (opts, v) -> opts.extraSettings.modNameTooltip = v,
                        opts -> opts.extraSettings.modNameTooltip))
                .add(booleanOption("sodium-extra.option.prevent_shaders",
                        (opts, v) -> opts.renderSettings.preventShaders = v,
                        opts -> opts.renderSettings.preventShaders))
                .build());

        return new OptionPage(CeleritasExtraOptionPages.MISC, TextComponent.literal(Translations.format("sodium-extra.option.extras")), ImmutableList.copyOf(groups));
    }

}
