package jp.s12kuma01.celeritasextra.client.gui;

import jp.s12kuma01.celeritasextra.CeleritasExtraMod;
import jp.s12kuma01.celeritasextra.client.particle.ParticleClassRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.ForgeEarlyConfig;
import net.minecraftforge.common.config.Configuration;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Central store for all Celeritas Extra configuration values.
 * <p>
 * Settings are grouped into typed holder classes ({@link AnimationSettings}, {@link ParticleSettings},
 * {@link DetailSettings}, {@link RenderSettings}, {@link ExtraSettings}) and persisted to a Forge
 * {@code .cfg} file via {@link Configuration}. Simple boolean and integer options are declared through
 * the {@link BooleanProperty} and {@link IntProperty} tables, while enum-valued and list-valued options
 * are handled explicitly in {@link #loadFromConfig()} and {@link #writeChanges()}.
 * <p>
 * Enum options are stored by their ordinal, so the declaration order of the enum constants is part of
 * the on-disk format and must not be reordered.
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
            new BooleanProperty(CAT_DETAIL, "skyColors", true, "Enable/disable biome-based sky colors",
                    v -> detailSettings.skyColors = v, () -> detailSettings.skyColors),
            new BooleanProperty(CAT_DETAIL, "voidParticles", true, "Enable/disable void particles",
                    v -> detailSettings.voidParticles = v, () -> detailSettings.voidParticles),
            new BooleanProperty(CAT_DETAIL, "voidFog", true, "Enable/disable void fog",
                    v -> detailSettings.voidFog = v, () -> detailSettings.voidFog),
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
                    v -> extraSettings.useAdaptiveSync = v, () -> extraSettings.useAdaptiveSync),
            new BooleanProperty(CAT_EXTRA, "hideHeiUntilSearch", false, "Hide HEI item list until searching",
                    v -> extraSettings.hideHeiUntilSearch = v, () -> extraSettings.hideHeiUntilSearch),
            new BooleanProperty(CAT_EXTRA, "toasts", true, "Master toggle for toast pop-ups",
                    v -> extraSettings.toasts = v, () -> extraSettings.toasts),
            new BooleanProperty(CAT_EXTRA, "toastAdvancement", true, "Show advancement toasts",
                    v -> extraSettings.toastAdvancement = v, () -> extraSettings.toastAdvancement),
            new BooleanProperty(CAT_EXTRA, "toastRecipe", true, "Show recipe toasts",
                    v -> extraSettings.toastRecipe = v, () -> extraSettings.toastRecipe),
            new BooleanProperty(CAT_EXTRA, "toastTutorial", true, "Show tutorial toasts",
                    v -> extraSettings.toastTutorial = v, () -> extraSettings.toastTutorial),
            new BooleanProperty(CAT_EXTRA, "toastSystem", true, "Show system toasts",
                    v -> extraSettings.toastSystem = v, () -> extraSettings.toastSystem),
            new BooleanProperty(CAT_EXTRA, "modNameTooltip", false, "Show the source mod's name at the bottom of item tooltips",
                    v -> extraSettings.modNameTooltip = v, () -> extraSettings.modNameTooltip)
    );
    private final List<IntProperty> intProperties = Arrays.asList(
            new IntProperty(CAT_RENDER, "fogStart", 100, 0, 200, "Fog start distance percentage (100 = default)",
                    v -> renderSettings.fogStart = v, () -> renderSettings.fogStart),
            new IntProperty(CAT_RENDER, "fogDistance", 0, 0, 32, "Fog distance in chunks (0 = use render distance)",
                    v -> renderSettings.fogDistance = v, () -> renderSettings.fogDistance),
            new IntProperty(CAT_RENDER, "cloudHeight", 192, 0, 384, "Cloud height",
                    v -> renderSettings.cloudHeight = v, () -> renderSettings.cloudHeight),
            new IntProperty(CAT_RENDER, "cloudDistance", 0, 0, 128, "Cloud render distance in chunks (0 = use render distance)",
                    v -> renderSettings.cloudDistance = v, () -> renderSettings.cloudDistance),
            new IntProperty(CAT_RENDER, "cloudScale", 4, 1, 4, "Cloud scale (1 = smallest, 4 = default)",
                    v -> renderSettings.cloudScale = v, () -> renderSettings.cloudScale),
            new IntProperty(CAT_RENDER, "itemFrameLodDistance", 0, 0, 256, "Item frame LOD distance in blocks (0 = off)",
                    v -> renderSettings.itemFrameLodDistance = v, () -> renderSettings.itemFrameLodDistance),
            new IntProperty(CAT_DETAIL, "totalStars", 1500, 500, 32000, "Number of stars to render",
                    v -> detailSettings.totalStars = v, () -> detailSettings.totalStars),
            new IntProperty(CAT_EXTRA, "steadyDebugHudRefreshInterval", 20, 1, 60, "F3 debug screen refresh interval in ticks",
                    v -> extraSettings.steadyDebugHudRefreshInterval = v, () -> extraSettings.steadyDebugHudRefreshInterval)
    );
    private Configuration config;

    /**
     * Loads options from the given config file, falling back to defaults on any error.
     * <p>
     * The file is always saved afterwards if Forge marked it changed (for example when missing keys
     * were added with their default values).
     *
     * @param file the {@code .cfg} file to read
     * @return a fully populated options instance backed by {@code file}
     */
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

    /**
     * Populates every setting from the loaded {@link Configuration}, including the declarative
     * boolean/int tables, the ordinal-encoded enum options, and the particle class filter lists.
     */
    private void loadFromConfig() {
        booleanProperties.forEach(p -> p.load(config));
        intProperties.forEach(p -> p.load(config));

        // Enum properties
        renderSettings.cloudTranslucency = CloudTranslucency.values()[config.getInt("cloudTranslucency", CAT_RENDER, 0, 0, CloudTranslucency.values().length - 1, "Cloud translucency mode (0 = Default, 1 = Always, 2 = Never)")];
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

    /**
     * Writes all current setting values back to the config file and saves it.
     * <p>
     * Mirrors {@link #loadFromConfig()}: the boolean/int tables, ordinal-encoded enums, and the
     * particle class filter lists are all flushed, after which the {@link ParticleClassRegistry} is
     * marked clean.
     */
    public void writeChanges() {
        booleanProperties.forEach(p -> p.save(config));
        intProperties.forEach(p -> p.save(config));

        // Enum properties
        config.get(CAT_RENDER, "cloudTranslucency", 0).set(renderSettings.cloudTranslucency.ordinal());
        config.get(CAT_EXTRA, "overlayCorner", 0).set(extraSettings.overlayCorner.ordinal());
        config.get(CAT_EXTRA, "textContrast", 2).set(extraSettings.textContrast.ordinal());

        // Particle class filter
        config.get(CAT_PARTICLE_CLASSES, "disabledClasses", new String[0])
                .set(ParticleClassRegistry.getInstance().getDisabledClassesArray());
        config.get(CAT_PARTICLE_CLASSES, "discoveredClasses", new String[0])
                .set(ParticleClassRegistry.getInstance().getDiscoveredClassesArray());

        config.save();
        ParticleClassRegistry.getInstance().markClean();
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
     * Text contrast modes controlling how overlay text is drawn for readability against the world.
     * - NONE: plain text with no backing
     * - BACKGROUND: a translucent rectangle drawn behind the text
     * - SHADOW: a drop shadow behind the glyphs
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
     * Vertical sync (frame presentation) modes selectable from the options UI.
     * - OFF: no vsync; frames are presented as fast as they render
     * - ON: standard vsync; frames are capped to the display refresh rate
     * - ADAPTIVE: vsync that disengages below the refresh rate to reduce stutter, requiring driver
     *   support for {@code GLX_EXT_swap_control_tear} / {@code WGL_EXT_swap_control_tear}
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

        /**
         * Derives the active mode from the adaptive-sync flag and the vanilla vsync setting.
         *
         * @param opts the options holding the adaptive-sync preference
         * @return the mode reflecting the game's present configuration
         */
        public static VerticalSyncOption getCurrent(CeleritasExtraGameOptions opts) {
            if (opts.extraSettings.useAdaptiveSync) {
                return ADAPTIVE;
            } else if (Minecraft.getMinecraft().gameSettings.enableVsync) {
                return ON;
            } else {
                return OFF;
            }
        }

        /**
         * Applies the selected mode, updating the adaptive-sync flag, the vanilla vsync setting, and
         * the live display, then persisting the vanilla options.
         *
         * @param opts  the options whose adaptive-sync flag is updated
         * @param value the mode to apply; ADAPTIVE silently falls back to ON when the driver lacks
         *              tear-control support
         */
        public static void apply(CeleritasExtraGameOptions opts, VerticalSyncOption value) {
            var mc = Minecraft.getMinecraft();
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
                    boolean supported = GLFW.glfwExtensionSupported("GLX_EXT_swap_control_tear")
                            || GLFW.glfwExtensionSupported("WGL_EXT_swap_control_tear");
                    opts.extraSettings.useAdaptiveSync = supported;
                    mc.gameSettings.enableVsync = true;
                }
            }
            Display.setVSyncEnabled(mc.gameSettings.enableVsync);
            mc.gameSettings.saveOptions();
        }
    }

    /**
     * Screen mode options: Windowed, Borderless Fullscreen, Exclusive Fullscreen.
     * Borderless uses Cleanroom's built-in Display.toggleBorderless() via ForgeEarlyConfig.
     */
    public enum ScreenMode {
        WINDOWED("celeritasextra.option.screen_mode.windowed"),
        BORDERLESS("celeritasextra.option.screen_mode.borderless"),
        FULLSCREEN("celeritasextra.option.screen_mode.fullscreen");

        private static ScreenMode current = null;

        private final String translationKey;

        ScreenMode(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getLocalizedName() {
            return I18n.format(this.translationKey);
        }

        /**
         * Gets the current screen mode. Uses cached state to avoid timing issues with mc.isFullScreen().
         */
        public static ScreenMode getCurrent(CeleritasExtraGameOptions opts) {
            if (current == null) {
                var mc = Minecraft.getMinecraft();
                if (!mc.isFullScreen()) {
                    current = WINDOWED;
                } else if (ForgeEarlyConfig.WINDOW_BORDERLESS_REPLACES_FULLSCREEN) {
                    current = BORDERLESS;
                } else {
                    current = FULLSCREEN;
                }
            }
            return current;
        }

        /**
         * Sets the screen mode by updating Cleanroom's ForgeEarlyConfig and toggling fullscreen as needed.
         */
        public static void apply(CeleritasExtraGameOptions opts, ScreenMode mode) {
            var mc = Minecraft.getMinecraft();
            var previous = getCurrent(opts);
            current = mode;

            if (previous == mode) return;

            if (previous != WINDOWED) {
                mc.toggleFullscreen();
            }

            switch (mode) {
                case WINDOWED -> ForgeEarlyConfig.WINDOW_BORDERLESS_REPLACES_FULLSCREEN = false;
                case BORDERLESS -> {
                    ForgeEarlyConfig.WINDOW_BORDERLESS_REPLACES_FULLSCREEN = true;
                    mc.toggleFullscreen();
                }
                case FULLSCREEN -> {
                    ForgeEarlyConfig.WINDOW_BORDERLESS_REPLACES_FULLSCREEN = false;
                    mc.toggleFullscreen();
                }
            }
        }
    }

    /**
     * Cloud translucency modes controlling when clouds fade to translucent.
     * - DEFAULT: clouds turn translucent above the configured cloud height, as in vanilla
     * - ALWAYS: clouds are always translucent
     * - NEVER: clouds are never translucent
     */
    public enum CloudTranslucency {
        DEFAULT("celeritasextra.option.cloud_translucency.default"),
        ALWAYS("celeritasextra.option.cloud_translucency.always"),
        NEVER("celeritasextra.option.cloud_translucency.never");

        private final String translationKey;

        CloudTranslucency(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getLocalizedName() {
            return I18n.format(this.translationKey);
        }
    }

    /**
     * Toggles for texture animations, with a master {@code animation} switch that gates the finer
     * water, lava, fire, portal, and general block-animation controls.
     */
    public static class AnimationSettings {
        public boolean animation = true;
        public boolean water = true;
        public boolean lava = true;
        public boolean fire = true;
        public boolean portal = true;
        public boolean blockAnimations = true;
    }

    /**
     * Toggles for particle effects, with a master {@code particles} switch and finer controls for
     * rain-splash, block-break, and block-breaking particles.
     */
    public static class ParticleSettings {
        public boolean particles = true;
        public boolean rainSplash = true;
        public boolean blockBreak = true;
        public boolean blockBreaking = true;
    }

    /**
     * Toggles for celestial and environmental detail rendering (sky, stars, sun/moon, weather, biome
     * and sky colors, void particles and fog) plus the {@code totalStars} count.
     */
    public static class DetailSettings {
        public boolean sky = true;
        public boolean stars = true;
        public boolean sunMoon = true;
        public boolean rainSnow = true;
        public boolean biomeColors = true;
        public boolean skyColors = true;
        public boolean voidParticles = true;
        public boolean voidFog = true;
        public int totalStars = 1500;
    }

    /**
     * World-rendering toggles and tuning values: fog, clouds (height, distance, scale, translucency),
     * light updates, and per-entity/block-entity render switches such as item frames, armor stands,
     * paintings, pistons, and beacon beams.
     */
    public static class RenderSettings {
        public boolean fog = true;
        public int fogStart = 100;
        public int fogDistance = 0;
        public boolean clouds = true;
        public int cloudHeight = 192;
        public int cloudDistance = 0;
        public CloudTranslucency cloudTranslucency = CloudTranslucency.DEFAULT;
        public int cloudScale = 4;
        public boolean lightUpdates = true;
        public boolean itemFrames = true;
        public int itemFrameLodDistance = 0;
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

    /**
     * Miscellaneous quality-of-life settings: the FPS/coordinate overlay with its placement and
     * contrast, accessibility options (reduced motion, adaptive vsync, steady debug HUD), toast
     * toggles, the HEI search gate, and the mod-name tooltip.
     */
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
        public boolean hideHeiUntilSearch = false;
        public boolean toasts = true;
        public boolean toastAdvancement = true;
        public boolean toastRecipe = true;
        public boolean toastTutorial = true;
        public boolean toastSystem = true;
        public boolean modNameTooltip = false;
    }

    /**
     * Declarative binding between a boolean config entry and its in-memory field.
     * <p>
     * Couples a category, key, default, and comment with a setter and getter so an entire table of
     * boolean options can be loaded from and saved to the {@link Configuration} uniformly.
     */
    private record BooleanProperty(String category, String key, boolean defaultValue, String comment,
                                   Consumer<Boolean> setter, Supplier<Boolean> getter) {
        /** Reads the entry from {@code config} (creating it with its default if absent) into the bound field. */
        void load(Configuration config) {
            setter.accept(config.getBoolean(key, category, defaultValue, comment));
        }

        /** Writes the bound field's current value into {@code config}. */
        void save(Configuration config) {
            config.get(category, key, defaultValue).set(getter.get());
        }
    }

    /**
     * Declarative binding between a bounded integer config entry and its in-memory field.
     * <p>
     * Like {@link BooleanProperty} but carries an inclusive {@code min}/{@code max} range that
     * {@link Configuration} clamps values to on load.
     */
    private record IntProperty(String category, String key, int defaultValue, int min, int max, String comment,
                               Consumer<Integer> setter, Supplier<Integer> getter) {
        /** Reads and clamps the entry from {@code config} (creating it with its default if absent) into the bound field. */
        void load(Configuration config) {
            setter.accept(config.getInt(key, category, defaultValue, min, max, comment));
        }

        /** Writes the bound field's current value into {@code config}. */
        void save(Configuration config) {
            config.get(category, key, defaultValue).set(getter.get());
        }
    }
}
