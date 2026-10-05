package jp.s12kuma01.celeritasextra.compat.nothirium;

import jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraGameOptions;
import jp.s12kuma01.celeritasextra.client.gui.Translations;
import jp.s12kuma01.celeritasextra.client.particle.ParticleClassRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/** Adapts the existing Forge configuration to renderer-independent video option pages. */
final class ForgeExtraConfigElements {
    private final CeleritasExtraGameOptions options;
    private final List<IConfigElement> categories = new ArrayList<>();
    private CeleritasExtraGameOptions.ScreenMode screenMode;
    private CeleritasExtraGameOptions.VerticalSyncOption vsync;
    private final CeleritasExtraGameOptions.ScreenMode originalScreenMode;
    private final CeleritasExtraGameOptions.VerticalSyncOption originalVsync;

    ForgeExtraConfigElements(CeleritasExtraGameOptions options) {
        this.options = options;
        var mc = Minecraft.getMinecraft();
        var registry = ParticleClassRegistry.getInstance();
        registry.scanFactories(mc.effectRenderer);
        var dimensions = new TreeSet<>(List.of(-1, 0, 1));
        dimensions.addAll(Arrays.asList(DimensionManager.getStaticDimensionIDs()));
        dimensions.addAll(options.renderSettings.dimensionFogOverrides.keySet());
        if (mc.world != null) dimensions.add(mc.world.provider.getDimension());
        dimensions.forEach(options.renderSettings::dimensionFog);
        options.writeChanges();
        options.applyForgeConfiguration();
        originalScreenMode = screenMode = CeleritasExtraGameOptions.ScreenMode.getCurrent(options);
        originalVsync = vsync = CeleritasExtraGameOptions.VerticalSyncOption.getCurrent(options);

        addCategory("animation", "sodium-extra.option.animations");
        addCategory("particle", "options.particles");
        addCategory("detail", "celeritasextra.option.page.sky_weather");
        addCategory("render", "celeritasextra.gui.render_settings");
        addCategory("extra", "celeritasextra.gui.misc_settings");

        var window = new ArrayList<IConfigElement>();
        var modes = CeleritasExtraGameOptions.ScreenMode.values();
        window.add(new DummyConfigElement("screenMode", "WINDOWED", ConfigGuiType.STRING,
                "sodium.extras.options.screen.title", Arrays.stream(modes).map(Enum::name).toArray(String[]::new),
                Arrays.stream(modes).map(CeleritasExtraGameOptions.ScreenMode::getLocalizedName).toArray(String[]::new)) {
            { value = screenMode.name(); }
            @Override public void set(Object selected) {
                value = selected;
                screenMode = CeleritasExtraGameOptions.ScreenMode.valueOf(selected.toString());
            }
        });
        var syncModes = CeleritasExtraGameOptions.VerticalSyncOption.getAvailableOptions();
        window.add(new DummyConfigElement("vsync", "OFF", ConfigGuiType.STRING, "options.vsync",
                Arrays.stream(syncModes).map(Enum::name).toArray(String[]::new),
                Arrays.stream(syncModes).map(CeleritasExtraGameOptions.VerticalSyncOption::getLocalizedName).toArray(String[]::new)) {
            { value = vsync.name(); }
            @Override public void set(Object selected) {
                value = selected;
                vsync = CeleritasExtraGameOptions.VerticalSyncOption.valueOf(selected.toString());
            }
        });
        categories.add(new DummyCategoryElement("window", "celeritasextra.gui.window_settings", window));

        var particleElements = new ArrayList<IConfigElement>();
        registry.getDiscoveredClasses().entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).forEach(entry -> {
            particleElements.add(new DummyConfigElement(entry.getValue() + " (" + registry.getModName(entry.getKey()) + ")",
                    100, ConfigGuiType.INTEGER, "", 0, 100) {
                { value = registry.getSpawnPercentage(entry.getKey()); }
                @Override public String getComment() {
                    return Translations.format("celeritasextra.option.particle_spawn_percentage.tooltip", entry.getValue())
                            + "\n" + entry.getKey();
                }
                @Override public void set(Object selected) {
                    value = selected;
                    registry.setSpawnPercentage(entry.getKey(), ((Number) selected).intValue());
                    options.forgeConfiguration().get("particle_classes", "disabledClasses", new String[0])
                            .set(registry.getDisabledClassesArray());
                    options.forgeConfiguration().get("particle_classes", "spawnPercentages", new String[0])
                            .set(registry.getSpawnPercentagesArray());
                }
            });
        });
        if (!particleElements.isEmpty()) categories.add(new DummyCategoryElement("particleClasses",
                "celeritasextra.gui.particle_classes", particleElements));
        var fogElements = new ArrayList<IConfigElement>();
        for (int dimension : dimensions) {
            String name = switch (dimension) {
                case -1 -> Translations.format("options.dimensions.minecraft.the_nether");
                case 0 -> Translations.format("options.dimensions.minecraft.overworld");
                case 1 -> Translations.format("options.dimensions.minecraft.the_end");
                default -> DimensionManager.isDimensionRegistered(dimension)
                        ? DimensionManager.getProviderType(dimension).getName() : Integer.toString(dimension);
            };
            var category = options.forgeConfiguration().getCategory("dimension_fog_" + dimension);
            category.get("override").setLanguageKey("celeritasextra.gui.fog_override");
            category.get("fog").setLanguageKey("sodium-extra.option.fog_type.atmospheric");
            category.get("start").setLanguageKey("sodium-extra.option.fog_start");
            category.get("distance").setLanguageKey("sodium-extra.option.fog_distance");
            fogElements.add(new DummyCategoryElement(name + " (" + dimension + ")", "",
                    new ConfigElement(category).getChildElements()));
        }
        categories.add(new DummyCategoryElement("dimensionFog", "celeritasextra.option.page.dimension_fog", fogElements));
        arrangeVideoPages(window, particleElements, fogElements);
    }

    private void arrangeVideoPages(List<IConfigElement> window, List<IConfigElement> particles, List<IConfigElement> fogDimensions) {
        var animation = categoryEntries("animation");
        var particle = categoryEntries("particle");
        var detail = categoryEntries("detail");
        var render = categoryEntries("render");
        var extra = categoryEntries("extra");
        var clouds = take(render, "clouds", "modernClouds", "cloudHeight", "cloudDistance", "cloudScale", "cloudTranslucency");
        var fog = take(render, "fog", "fogStart", "fogDistance");
        fog.addAll(take(detail, "voidFog"));
        fog.add(new DummyCategoryElement("dimensionFog", "celeritasextra.option.page.dimension_fog", fogDimensions));
        var entities = take(render, "itemFrames", "armorStands", "paintings", "playerNameTag", "itemFrameNameTag",
                "entityRenderDistance", "itemFrameLodDistance", "mapBackFaceCulling", "entityDistanceExemptions");
        var overlay = take(extra, "showFps", "showFPSExtended", "showCoords", "showMemory", "ignoreReducedDebugInfo",
                "overlayCorner", "textContrast");
        window.addAll(take(extra, "menuFpsLimit", "inactiveFpsLimit", "minimizedFpsLimit"));
        extra.addAll(take(render, "preventShaders"));
        if (!particles.isEmpty()) particle.add(new DummyCategoryElement("particleClasses", "celeritasextra.gui.particle_classes", particles));
        categories.clear();
        page("animation", "sodium-extra.option.animations", animation);
        page("particle", "options.particles", particle);
        page("sky", "celeritasextra.option.page.sky_weather", detail);
        page("clouds", "celeritasextra.option.page.clouds", clouds);
        page("fog", "celeritasextra.option.page.fog", fog);
        page("entities", "celeritasextra.option.page.entities", entities);
        page("blocks", "celeritasextra.option.page.block_rendering", render);
        page("overlay", "celeritasextra.option.page.overlay", overlay);
        page("misc", "celeritasextra.gui.misc_settings", extra);
        page("window", "celeritasextra.gui.window_settings", window);
    }

    private List<IConfigElement> categoryEntries(String name) {
        return new ArrayList<>(categories.stream().filter(category -> category.getName().equals(name))
                .findFirst().orElseThrow().getChildElements());
    }

    private static List<IConfigElement> take(List<IConfigElement> source, String... names) {
        var selected = new ArrayList<IConfigElement>();
        for (String name : names) {
            var element = source.stream().filter(entry -> entry.getName().equals(name)).findFirst().orElse(null);
            if (element != null) { selected.add(element); source.remove(element); }
        }
        return selected;
    }

    private void page(String name, String key, List<IConfigElement> entries) {
        categories.add(new DummyCategoryElement(name, key, entries));
    }

    private void addCategory(String category, String languageKey) {
        var source = options.forgeConfiguration().getCategory(category);
        var children = new ArrayList<IConfigElement>();
        for (Property property : source.getOrderedValues()) {
            if (property.getName().equals("sunMoon") || property.getName().equals("useAdaptiveSync")) continue;
            String key = ForgeExtraTranslations.key(category, property.getName());
            if (key != null) property.setLanguageKey(key);
            if (category.equals("render") && property.getName().equals("cloudTranslucency")) {
                children.add(enumElement(property, CeleritasExtraGameOptions.CloudTranslucency.values(),
                        Arrays.stream(CeleritasExtraGameOptions.CloudTranslucency.values())
                                .map(CeleritasExtraGameOptions.CloudTranslucency::getLocalizedName).toArray(String[]::new)));
            } else if (category.equals("extra") && property.getName().equals("overlayCorner")) {
                children.add(enumElement(property, CeleritasExtraGameOptions.OverlayCorner.values(),
                        Arrays.stream(CeleritasExtraGameOptions.OverlayCorner.values())
                                .map(CeleritasExtraGameOptions.OverlayCorner::getLocalizedName).toArray(String[]::new)));
            } else if (category.equals("extra") && property.getName().equals("textContrast")) {
                children.add(enumElement(property, CeleritasExtraGameOptions.TextContrast.values(),
                        Arrays.stream(CeleritasExtraGameOptions.TextContrast.values())
                                .map(CeleritasExtraGameOptions.TextContrast::getLocalizedName).toArray(String[]::new)));
            } else {
                children.add(new ConfigElement(property));
            }
        }
        categories.add(new DummyCategoryElement(category, languageKey, children));
    }

    List<IConfigElement> categories() { return categories; }

    static IConfigElement enumElement(Property property, Enum<?>[] values, String[] labels) {
        return new ConfigElement(property) {
            @Override public ConfigGuiType getType() { return ConfigGuiType.STRING; }
            @Override public Object get() { return values[property.getInt()].name(); }
            @Override public Object getDefault() { return values[Integer.parseInt(property.getDefault())].name(); }
            @Override public String[] getValidValues() { return Arrays.stream(values).map(Enum::name).toArray(String[]::new); }
            @Override public String[] getValidValuesDisplay() { return labels; }
            @Override public void set(Object selected) {
                for (Enum<?> value : values) {
                    if (value.name().equals(selected.toString())) {
                        property.set(value.ordinal());
                        return;
                    }
                }
                throw new IllegalArgumentException("Unknown enum value: " + selected);
            }
        };
    }

    void apply() {
        boolean oldModernClouds = options.renderSettings.modernClouds;
        boolean oldSky = options.detailSettings.sky;
        boolean oldStars = options.detailSettings.stars;
        int oldStarCount = options.detailSettings.totalStars;
        boolean oldBiomeColors = options.detailSettings.biomeColors;
        options.applyForgeConfiguration();
        if (vsync != originalVsync) CeleritasExtraGameOptions.VerticalSyncOption.apply(options, vsync);
        if (screenMode != originalScreenMode) CeleritasExtraGameOptions.ScreenMode.apply(options, screenMode);
        options.writeChanges();
        var mc = Minecraft.getMinecraft();
        if (oldModernClouds != options.renderSettings.modernClouds) {
            mc.refreshResources();
        } else if (mc.world != null && (oldSky != options.detailSettings.sky || oldStars != options.detailSettings.stars
                || oldStarCount != options.detailSettings.totalStars || oldBiomeColors != options.detailSettings.biomeColors)) {
            mc.renderGlobal.loadRenderers();
        }
    }
}
