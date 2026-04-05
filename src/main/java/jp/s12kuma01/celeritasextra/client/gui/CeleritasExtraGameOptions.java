package jp.s12kuma01.celeritasextra.client.gui;

import jp.s12kuma01.celeritasextra.CeleritasExtraMod;
import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.particle.ParticleClassRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Configuration;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Main configuration class for Celeritas Extra
 * Uses Forge Configuration for .cfg file format
 */
public class CeleritasExtraGameOptions {

    private static final String CAT_ANIMATION = "animation";
    private static final String CAT_PARTICLE = "particle";
    private static final String CAT_PARTICLE_CLASSES = "particle_classes";
    private static final String CAT_DETAIL = "detail";
    private static final String CAT_RENDER = "render";
    private static final String CAT_EXTRA = "extra";

    public final AnimationSettings animationSettings = new AnimationSettings();
    public final ParticleSettings particleSettings = new ParticleSettings();
    public final DetailSettings detailSettings = new DetailSettings();
    public final RenderSettings renderSettings = new RenderSettings();
    public final ExtraSettings extraSettings = new ExtraSettings();
    private final List<BooleanProperty> booleanProperties = Arrays.asList(
            // Animation settings
            new BooleanProperty(CAT_ANIMATION, "animation", true, "Enable/disable all animations",
                    v -> animationSettings.animation = v, () -> animationSettings.animation),
            new BooleanProperty(CAT_ANIMATION, "water", true, "Enable/disable water animations",
                    v -> animationSettings.water = v, () -> animationSettings.water),
            new BooleanProperty(CAT_ANIMATION, "lava", true, "Enable/disable lava animations",
                    v -> animationSettings.lava = v, () -> animationSettings.lava),
            new BooleanProperty(CAT_ANIMATION, "fire", true, "Enable/disable fire animations",
                    v -> animationSettings.fire = v, () -> animationSettings.fire),
            new BooleanProperty(CAT_ANIMATION, "portal", true, "Enable/disable portal animations",
                    v -> animationSettings.portal = v, () -> animationSettings.portal),
            new BooleanProperty(CAT_ANIMATION, "blockAnimations", true, "Enable/disable block animations",
                    v -> animationSettings.blockAnimations = v, () -> animationSettings.blockAnimations),
            // Particle settings
            new BooleanProperty(CAT_PARTICLE, "particles", true, "Enable/disable all particles",
                    v -> particleSettings.particles = v, () -> particleSettings.particles),
            new BooleanProperty(CAT_PARTICLE, "rainSplash", true, "Enable/disable rain splash particles",
                    v -> particleSettings.rainSplash = v, () -> particleSettings.rainSplash),
            new BooleanProperty(CAT_PARTICLE, "blockBreak", true, "Enable/disable block break particles",
                    v -> particleSettings.blockBreak = v, () -> particleSettings.blockBreak),
            new BooleanProperty(CAT_PARTICLE, "blockBreaking", true, "Enable/disable block breaking particles",
                    v -> particleSettings.blockBreaking = v, () -> particleSettings.blockBreaking),
            // Detail settings
            new BooleanProperty(CAT_DETAIL, "sky", true, "Enable/disable sky rendering",
                    v -> detailSettings.sky = v, () -> detailSettings.sky),
            new BooleanProperty(CAT_DETAIL, "stars", true, "Enable/disable star rendering",
                    v -> detailSettings.stars = v, () -> detailSettings.stars),
            new BooleanProperty(CAT_DETAIL, "sunMoon", true, "Enable/disable sun and moon rendering",
                    v -> detailSettings.sunMoon = v, () -> detailSettings.sunMoon),
            new BooleanProperty(CAT_DETAIL, "rainSnow", true, "Enable/disable rain and snow rendering",
                    v -> detailSettings.rainSnow = v, () -> detailSettings.rainSnow),
            new BooleanProperty(CAT_DETAIL, "biomeColors", true, "Enable/disable biome-specific colors",
                    v -> detailSettings.biomeColors = v, () -> detailSettings.biomeColors),
            // Render settings
            new BooleanProperty(CAT_RENDER, "fog", true, "Enable/disable fog rendering",
                    v -> renderSettings.fog = v, () -> renderSettings.fog),
            new BooleanProperty(CAT_RENDER, "clouds", true, "Enable/disable cloud rendering",
                    v -> renderSettings.clouds = v, () -> renderSettings.clouds),
            new BooleanProperty(CAT_RENDER, "lightUpdates", true, "Enable/disable light updates",
                    v -> renderSettings.lightUpdates = v, () -> renderSettings.lightUpdates),
            new BooleanProperty(CAT_RENDER, "itemFrames", true, "Enable/disable item frame rendering",
                    v -> renderSettings.itemFrames = v, () -> renderSettings.itemFrames),
            new BooleanProperty(CAT_RENDER, "armorStands", true, "Enable/disable armor stand rendering",
                    v -> renderSettings.armorStands = v, () -> renderSettings.armorStands),
            new BooleanProperty(CAT_RENDER, "paintings", true, "Enable/disable painting rendering",
                    v -> renderSettings.paintings = v, () -> renderSettings.paintings),
            new BooleanProperty(CAT_RENDER, "pistons", true, "Enable/disable piston animation rendering",
                    v -> renderSettings.pistons = v, () -> renderSettings.pistons),
            new BooleanProperty(CAT_RENDER, "beacons", true, "Enable/disable beacon beam rendering",
                    v -> renderSettings.beacons = v, () -> renderSettings.beacons),
            new BooleanProperty(CAT_RENDER, "limitBeaconBeamHeight", false, "Limit beacon beam to world height",
                    v -> renderSettings.limitBeaconBeamHeight = v, () -> renderSettings.limitBeaconBeamHeight),
            new BooleanProperty(CAT_RENDER, "enchantingTableBooks", true, "Enable/disable enchanting table book rendering",
                    v -> renderSettings.enchantingTableBooks = v, () -> renderSettings.enchantingTableBooks),
            new BooleanProperty(CAT_RENDER, "playerNameTag", true, "Enable/disable player name tag rendering",
                    v -> renderSettings.playerNameTag = v, () -> renderSettings.playerNameTag),
            new BooleanProperty(CAT_RENDER, "itemFrameNameTag", true, "Enable/disable item frame name tag rendering",
                    v -> renderSettings.itemFrameNameTag = v, () -> renderSettings.itemFrameNameTag),
            new BooleanProperty(CAT_RENDER, "preventShaders", false, "Prevent accidental shader activation",
                    v -> renderSettings.preventShaders = v, () -> renderSettings.preventShaders),
            // Extra settings
            new BooleanProperty(CAT_EXTRA, "showFps", false, "Show FPS overlay",
                    v -> extraSettings.showFps = v, () -> extraSettings.showFps),
            new BooleanProperty(CAT_EXTRA, "showFPSExtended", true, "Show extended FPS info (high/avg/low)",
                    v -> extraSettings.showFPSExtended = v, () -> extraSettings.showFPSExtended),
            new BooleanProperty(CAT_EXTRA, "showCoords", false, "Show coordinates overlay",
                    v -> extraSettings.showCoords = v, () -> extraSettings.showCoords),
            new BooleanProperty(CAT_EXTRA, "ignoreReducedDebugInfo", false, "Ignore reduced debug info gamerule",
                    v -> extraSettings.ignoreReducedDebugInfo = v, () -> extraSettings.ignoreReducedDebugInfo),
            new BooleanProperty(CAT_EXTRA, "reducedMotion", false, "Reduce motion effects for accessibility",
                    v -> extraSettings.reducedMotion = v, () -> extraSettings.reducedMotion),
            new BooleanProperty(CAT_EXTRA, "steadyDebugHud", false, "Reduce F3 debug screen update frequency",
                    v -> extraSettings.steadyDebugHud = v, () -> extraSettings.steadyDebugHud),
            new BooleanProperty(CAT_EXTRA, "useAdaptiveSync", false, "Enable adaptive VSync (swap interval -1)",
                    v -> extraSettings.useAdaptiveSync = v, () -> extraSettings.useAdaptiveSync)
    );
    private final List<IntProperty> intProperties = Arrays.asList(
            new IntProperty(CAT_RENDER, "fogStart", 100, 0, 200, "Fog start distance percentage (100 = default)",
                    v -> renderSettings.fogStart = v, () -> renderSettings.fogStart),
            new IntProperty(CAT_RENDER, "fogDistance", 0, 0, 32, "Fog distance in chunks (0 = use render distance)",
                    v -> renderSettings.fogDistance = v, () -> renderSettings.fogDistance),
            new IntProperty(CAT_RENDER, "cloudHeight", 192, 0, 384, "Cloud height",
                    v -> renderSettings.cloudHeight = v, () -> renderSettings.cloudHeight),
            new IntProperty(CAT_EXTRA, "steadyDebugHudRefreshInterval", 20, 1, 60, "F3 debug screen refresh interval in ticks",
                    v -> extraSettings.steadyDebugHudRefreshInterval = v, () -> extraSettings.steadyDebugHudRefreshInterval)
    );
    private Configuration config;

    public static CeleritasExtraGameOptions load(File file) {
        CeleritasExtraGameOptions options = new CeleritasExtraGameOptions();
        options.config = new Configuration(file);

        try {
            options.config.load();
            options.loadFromConfig();
        } catch (Exception e) {
            CeleritasExtraMod.LOGGER.error("Could not load config, falling back to defaults!", e);
        } finally {
            if (options.config.hasChanged()) {
                options.config.save();
            }
        }

        return options;
    }

    private void loadFromConfig() {
        booleanProperties.forEach(p -> p.load(config));
        intProperties.forEach(p -> p.load(config));

        // Enum properties
        renderSettings.fogType = FogType.values()[config.getInt("fogType", CAT_RENDER, 0, 0, FogType.values().length - 1, "Fog type (0 = Default, 1 = Off)")];
        extraSettings.overlayCorner = OverlayCorner.values()[config.getInt("overlayCorner", CAT_EXTRA, 0, 0, OverlayCorner.values().length - 1, "Overlay corner position (0=TopLeft, 1=TopRight, 2=BottomLeft, 3=BottomRight)")];
        extraSettings.textContrast = TextContrast.values()[config.getInt("textContrast", CAT_EXTRA, 2, 0, TextContrast.values().length - 1, "Text contrast mode (0=None, 1=Background, 2=Shadow)")];

        // Particle class filter
        String[] disabledClasses = config.getStringList("disabledClasses", CAT_PARTICLE_CLASSES,
                new String[0], "List of disabled particle class names");
        ParticleClassRegistry.getInstance().loadDisabledClasses(disabledClasses);

        String[] discoveredClasses = config.getStringList("discoveredClasses", CAT_PARTICLE_CLASSES,
                new String[0], "Cached list of discovered particle classes (auto-populated)");
        ParticleClassRegistry.getInstance().loadDiscoveredClasses(discoveredClasses);
    }

    public void writeChanges() {
        booleanProperties.forEach(p -> p.save(config));
        intProperties.forEach(p -> p.save(config));

        // Enum properties
        config.get(CAT_RENDER, "fogType", 0).set(renderSettings.fogType.ordinal());
        config.get(CAT_EXTRA, "overlayCorner", 0).set(extraSettings.overlayCorner.ordinal());
        config.get(CAT_EXTRA, "textContrast", 2).set(extraSettings.textContrast.ordinal());

        // Particle class filter
        config.get(CAT_PARTICLE_CLASSES, "disabledClasses", new String[0])
                .set(ParticleClassRegistry.getInstance().getDisabledClassesArray());
        config.get(CAT_PARTICLE_CLASSES, "discoveredClasses", new String[0])
                .set(ParticleClassRegistry.getInstance().getDiscoveredClassesArray());

        config.save();
    }

    /**
     * Overlay corner positions for FPS/coordinate display
     */
    public enum OverlayCorner {
        TOP_LEFT("celeritasextra.option.overlay_corner.top_left"),
        TOP_RIGHT("celeritasextra.option.overlay_corner.top_right"),
        BOTTOM_LEFT("celeritasextra.option.overlay_corner.bottom_left"),
        BOTTOM_RIGHT("celeritasextra.option.overlay_corner.bottom_right");

        private final String translationKey;

        OverlayCorner(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getLocalizedName() {
            return I18n.format(this.translationKey);
        }
    }

    /**
     * Text contrast options for overlay readability
     */
    public enum TextContrast {
        NONE("celeritasextra.option.text_contrast.none"),
        BACKGROUND("celeritasextra.option.text_contrast.background"),
        SHADOW("celeritasextra.option.text_contrast.shadow");

        private final String translationKey;

        TextContrast(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getLocalizedName() {
            return I18n.format(this.translationKey);
        }
    }

    /**
     * Vertical sync options
     */
    public enum VerticalSyncOption {
        OFF("celeritasextra.option.vertical_sync.off"),
        ON("celeritasextra.option.vertical_sync.on"),
        ADAPTIVE("celeritasextra.option.vertical_sync.adaptive");

        private final String translationKey;

        VerticalSyncOption(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getLocalizedName() {
            return I18n.format(this.translationKey);
        }

        /**
         * Returns only the VSync options supported by the current GPU/driver.
         * ADAPTIVE requires GLX_EXT_swap_control_tear or WGL_EXT_swap_control_tear.
         */
        public static VerticalSyncOption[] getAvailableOptions() {
            boolean adaptiveSupported = GLFW.glfwExtensionSupported("GLX_EXT_swap_control_tear")
                    || GLFW.glfwExtensionSupported("WGL_EXT_swap_control_tear");
            if (adaptiveSupported) {
                return values();
            } else {
                return new VerticalSyncOption[]{OFF, ON};
            }
        }
    }

    /**
     * Gets the current VSync mode from the two underlying booleans.
     */
    public static VerticalSyncOption getVerticalSyncOption(CeleritasExtraGameOptions opts) {
        if (opts.extraSettings.useAdaptiveSync) {
            return VerticalSyncOption.ADAPTIVE;
        } else if (Minecraft.getMinecraft().gameSettings.enableVsync) {
            return VerticalSyncOption.ON;
        } else {
            return VerticalSyncOption.OFF;
        }
    }

    /**
     * Sets the VSync mode by updating the two underlying booleans and applying immediately.
     */
    public static void setVerticalSyncOption(CeleritasExtraGameOptions opts, VerticalSyncOption value) {
        Minecraft mc = Minecraft.getMinecraft();
        switch (value) {
            case OFF -> {
                opts.extraSettings.useAdaptiveSync = false;
                mc.gameSettings.enableVsync = false;
            }
            case ON -> {
                opts.extraSettings.useAdaptiveSync = false;
                mc.gameSettings.enableVsync = true;
            }
            case ADAPTIVE -> {
                opts.extraSettings.useAdaptiveSync = true;
                mc.gameSettings.enableVsync = true;
            }
        }
        // Trigger VSync update - our mixin intercepts this when adaptive is enabled
        Display.setVSyncEnabled(mc.gameSettings.enableVsync);
        mc.gameSettings.saveOptions();
    }

    /**
     * Fog type options for mod compatibility
     */
    public enum FogType {
        DEFAULT("celeritasextra.option.fog_type.default"),
        OFF("celeritasextra.option.fog_type.off");

        private final String translationKey;

        FogType(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getLocalizedName() {
            return I18n.format(this.translationKey);
        }
    }

    public static class AnimationSettings {
        public boolean animation = true;
        public boolean water = true;
        public boolean lava = true;
        public boolean fire = true;
        public boolean portal = true;
        public boolean blockAnimations = true;
    }

    public static class ParticleSettings {
        public boolean particles = true;
        public boolean rainSplash = true;
        public boolean blockBreak = true;
        public boolean blockBreaking = true;
    }

    public static class DetailSettings {
        public boolean sky = true;
        public boolean stars = true;
        public boolean sunMoon = true;
        public boolean rainSnow = true;
        public boolean biomeColors = true;
    }

    public static class RenderSettings {
        public boolean fog = true;
        public int fogStart = 100;
        public int fogDistance = 0;
        public FogType fogType = FogType.DEFAULT;
        public boolean clouds = true;
        public int cloudHeight = 192;
        public boolean lightUpdates = true;
        public boolean itemFrames = true;
        public boolean armorStands = true;
        public boolean paintings = true;
        public boolean pistons = true;
        public boolean beacons = true;
        public boolean limitBeaconBeamHeight = false;
        public boolean enchantingTableBooks = true;
        public boolean playerNameTag = true;
        public boolean itemFrameNameTag = true;
        public boolean preventShaders = false;
    }

    public static class ExtraSettings {
        public boolean showFps = false;
        public boolean showFPSExtended = true;
        public boolean showCoords = false;
        public boolean ignoreReducedDebugInfo = false;
        public boolean reducedMotion = false;
        public boolean useAdaptiveSync = false;
        public OverlayCorner overlayCorner = OverlayCorner.TOP_LEFT;
        public TextContrast textContrast = TextContrast.SHADOW;
        public boolean steadyDebugHud = false;
        public int steadyDebugHudRefreshInterval = 20;
    }

    private record BooleanProperty(String category, String key, boolean defaultValue, String comment,
                                    Consumer<Boolean> setter, Supplier<Boolean> getter) {
        void load(Configuration config) {
            setter.accept(config.getBoolean(key, category, defaultValue, comment));
        }

        void save(Configuration config) {
            config.get(category, key, defaultValue).set(getter.get());
        }
    }

    private record IntProperty(String category, String key, int defaultValue, int min, int max, String comment,
                                Consumer<Integer> setter, Supplier<Integer> getter) {
        void load(Configuration config) {
            setter.accept(config.getInt(key, category, defaultValue, min, max, comment));
        }

        void save(Configuration config) {
            config.get(category, key, defaultValue).set(getter.get());
        }
    }
}
