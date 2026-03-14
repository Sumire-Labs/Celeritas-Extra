package jp.s12kuma01.celeritasextra.client.gui;

import jp.s12kuma01.celeritasextra.CeleritasExtraMod;
import jp.s12kuma01.celeritasextra.client.particle.ParticleClassRegistry;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * Main configuration class for Celeritas Extra
 * Uses Forge Configuration for .cfg file format
 */
public class CeleritasExtraGameOptions {

    // Category names
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
        // Animation settings
        animationSettings.animation = config.getBoolean("animation", CAT_ANIMATION, true, "Enable/disable all animations");
        animationSettings.water = config.getBoolean("water", CAT_ANIMATION, true, "Enable/disable water animations");
        animationSettings.lava = config.getBoolean("lava", CAT_ANIMATION, true, "Enable/disable lava animations");
        animationSettings.fire = config.getBoolean("fire", CAT_ANIMATION, true, "Enable/disable fire animations");
        animationSettings.portal = config.getBoolean("portal", CAT_ANIMATION, true, "Enable/disable portal animations");
        animationSettings.blockAnimations = config.getBoolean("blockAnimations", CAT_ANIMATION, true, "Enable/disable block animations");

        // Particle settings - basic
        particleSettings.particles = config.getBoolean("particles", CAT_PARTICLE, true, "Enable/disable all particles");
        particleSettings.rainSplash = config.getBoolean("rainSplash", CAT_PARTICLE, true, "Enable/disable rain splash particles");
        particleSettings.blockBreak = config.getBoolean("blockBreak", CAT_PARTICLE, true, "Enable/disable block break particles");
        particleSettings.blockBreaking = config.getBoolean("blockBreaking", CAT_PARTICLE, true, "Enable/disable block breaking particles");

        // Particle settings - class-based filter
        String[] disabledClasses = config.getStringList("disabledClasses", CAT_PARTICLE_CLASSES,
                new String[0], "List of disabled particle class names");
        ParticleClassRegistry.getInstance().loadDisabledClasses(disabledClasses);

        // Load previously discovered particle classes so they appear in settings before spawning
        String[] discoveredClasses = config.getStringList("discoveredClasses", CAT_PARTICLE_CLASSES,
                new String[0], "Cached list of discovered particle classes (auto-populated)");
        ParticleClassRegistry.getInstance().loadDiscoveredClasses(discoveredClasses);

        // Detail settings
        detailSettings.sky = config.getBoolean("sky", CAT_DETAIL, true, "Enable/disable sky rendering");
        detailSettings.stars = config.getBoolean("stars", CAT_DETAIL, true, "Enable/disable star rendering");
        detailSettings.sunMoon = config.getBoolean("sunMoon", CAT_DETAIL, true, "Enable/disable sun and moon rendering");
        detailSettings.rainSnow = config.getBoolean("rainSnow", CAT_DETAIL, true, "Enable/disable rain and snow rendering");
        detailSettings.biomeColors = config.getBoolean("biomeColors", CAT_DETAIL, true, "Enable/disable biome-specific colors");

        // Render settings
        renderSettings.fog = config.getBoolean("fog", CAT_RENDER, true, "Enable/disable fog rendering");
        renderSettings.fogStart = config.getInt("fogStart", CAT_RENDER, 100, 0, 200, "Fog start distance percentage (100 = default)");
        renderSettings.fogDistance = config.getInt("fogDistance", CAT_RENDER, 0, 0, 32, "Fog distance in chunks (0 = use render distance)");
        renderSettings.fogType = FogType.values()[config.getInt("fogType", CAT_RENDER, 0, 0, FogType.values().length - 1, "Fog type (0 = Default, 1 = Off)")];
        renderSettings.clouds = config.getBoolean("clouds", CAT_RENDER, true, "Enable/disable cloud rendering");
        renderSettings.cloudHeight = config.getInt("cloudHeight", CAT_RENDER, 192, 0, 384, "Cloud height");
        renderSettings.lightUpdates = config.getBoolean("lightUpdates", CAT_RENDER, true, "Enable/disable light updates");
        renderSettings.itemFrames = config.getBoolean("itemFrames", CAT_RENDER, true, "Enable/disable item frame rendering");
        renderSettings.armorStands = config.getBoolean("armorStands", CAT_RENDER, true, "Enable/disable armor stand rendering");
        renderSettings.paintings = config.getBoolean("paintings", CAT_RENDER, true, "Enable/disable painting rendering");
        renderSettings.pistons = config.getBoolean("pistons", CAT_RENDER, true, "Enable/disable piston animation rendering");
        renderSettings.beacons = config.getBoolean("beacons", CAT_RENDER, true, "Enable/disable beacon beam rendering");
        renderSettings.limitBeaconBeamHeight = config.getBoolean("limitBeaconBeamHeight", CAT_RENDER, false, "Limit beacon beam to world height");
        renderSettings.enchantingTableBooks = config.getBoolean("enchantingTableBooks", CAT_RENDER, true, "Enable/disable enchanting table book rendering");
        renderSettings.playerNameTag = config.getBoolean("playerNameTag", CAT_RENDER, true, "Enable/disable player name tag rendering");
        renderSettings.itemFrameNameTag = config.getBoolean("itemFrameNameTag", CAT_RENDER, true, "Enable/disable item frame name tag rendering");
        renderSettings.preventShaders = config.getBoolean("preventShaders", CAT_RENDER, false, "Prevent accidental shader activation");

        // Extra settings
        extraSettings.showFps = config.getBoolean("showFps", CAT_EXTRA, false, "Show FPS overlay");
        extraSettings.showFPSExtended = config.getBoolean("showFPSExtended", CAT_EXTRA, true, "Show extended FPS info (high/avg/low)");
        extraSettings.showCoords = config.getBoolean("showCoords", CAT_EXTRA, false, "Show coordinates overlay");
        extraSettings.ignoreReducedDebugInfo = config.getBoolean("ignoreReducedDebugInfo", CAT_EXTRA, false, "Ignore reduced debug info gamerule");
        extraSettings.reducedMotion = config.getBoolean("reducedMotion", CAT_EXTRA, false, "Reduce motion effects for accessibility");
        extraSettings.overlayCorner = OverlayCorner.values()[config.getInt("overlayCorner", CAT_EXTRA, 0, 0, OverlayCorner.values().length - 1, "Overlay corner position (0=TopLeft, 1=TopRight, 2=BottomLeft, 3=BottomRight)")];
        extraSettings.textContrast = TextContrast.values()[config.getInt("textContrast", CAT_EXTRA, 2, 0, TextContrast.values().length - 1, "Text contrast mode (0=None, 1=Background, 2=Shadow)")];
        extraSettings.steadyDebugHud = config.getBoolean("steadyDebugHud", CAT_EXTRA, false, "Reduce F3 debug screen update frequency");
        extraSettings.steadyDebugHudRefreshInterval = config.getInt("steadyDebugHudRefreshInterval", CAT_EXTRA, 20, 1, 60, "F3 debug screen refresh interval in ticks");
    }

    public void writeChanges() {
        // Animation settings
        config.get(CAT_ANIMATION, "animation", true).set(animationSettings.animation);
        config.get(CAT_ANIMATION, "water", true).set(animationSettings.water);
        config.get(CAT_ANIMATION, "lava", true).set(animationSettings.lava);
        config.get(CAT_ANIMATION, "fire", true).set(animationSettings.fire);
        config.get(CAT_ANIMATION, "portal", true).set(animationSettings.portal);
        config.get(CAT_ANIMATION, "blockAnimations", true).set(animationSettings.blockAnimations);

        // Particle settings - basic
        config.get(CAT_PARTICLE, "particles", true).set(particleSettings.particles);
        config.get(CAT_PARTICLE, "rainSplash", true).set(particleSettings.rainSplash);
        config.get(CAT_PARTICLE, "blockBreak", true).set(particleSettings.blockBreak);
        config.get(CAT_PARTICLE, "blockBreaking", true).set(particleSettings.blockBreaking);

        // Particle settings - class-based filter
        config.get(CAT_PARTICLE_CLASSES, "disabledClasses", new String[0])
                .set(ParticleClassRegistry.getInstance().getDisabledClassesArray());

        // Persist discovered particle classes for next session
        config.get(CAT_PARTICLE_CLASSES, "discoveredClasses", new String[0])
                .set(ParticleClassRegistry.getInstance().getDiscoveredClassesArray());

        // Detail settings
        config.get(CAT_DETAIL, "sky", true).set(detailSettings.sky);
        config.get(CAT_DETAIL, "stars", true).set(detailSettings.stars);
        config.get(CAT_DETAIL, "sunMoon", true).set(detailSettings.sunMoon);
        config.get(CAT_DETAIL, "rainSnow", true).set(detailSettings.rainSnow);
        config.get(CAT_DETAIL, "biomeColors", true).set(detailSettings.biomeColors);

        // Render settings
        config.get(CAT_RENDER, "fog", true).set(renderSettings.fog);
        config.get(CAT_RENDER, "fogStart", 100).set(renderSettings.fogStart);
        config.get(CAT_RENDER, "fogDistance", 0).set(renderSettings.fogDistance);
        config.get(CAT_RENDER, "fogType", 0).set(renderSettings.fogType.ordinal());
        config.get(CAT_RENDER, "clouds", true).set(renderSettings.clouds);
        config.get(CAT_RENDER, "cloudHeight", 192).set(renderSettings.cloudHeight);
        config.get(CAT_RENDER, "lightUpdates", true).set(renderSettings.lightUpdates);
        config.get(CAT_RENDER, "itemFrames", true).set(renderSettings.itemFrames);
        config.get(CAT_RENDER, "armorStands", true).set(renderSettings.armorStands);
        config.get(CAT_RENDER, "paintings", true).set(renderSettings.paintings);
        config.get(CAT_RENDER, "pistons", true).set(renderSettings.pistons);
        config.get(CAT_RENDER, "beacons", true).set(renderSettings.beacons);
        config.get(CAT_RENDER, "limitBeaconBeamHeight", false).set(renderSettings.limitBeaconBeamHeight);
        config.get(CAT_RENDER, "enchantingTableBooks", true).set(renderSettings.enchantingTableBooks);
        config.get(CAT_RENDER, "playerNameTag", true).set(renderSettings.playerNameTag);
        config.get(CAT_RENDER, "itemFrameNameTag", true).set(renderSettings.itemFrameNameTag);
        config.get(CAT_RENDER, "preventShaders", false).set(renderSettings.preventShaders);

        // Extra settings
        config.get(CAT_EXTRA, "showFps", false).set(extraSettings.showFps);
        config.get(CAT_EXTRA, "showFPSExtended", true).set(extraSettings.showFPSExtended);
        config.get(CAT_EXTRA, "showCoords", false).set(extraSettings.showCoords);
        config.get(CAT_EXTRA, "ignoreReducedDebugInfo", false).set(extraSettings.ignoreReducedDebugInfo);
        config.get(CAT_EXTRA, "reducedMotion", false).set(extraSettings.reducedMotion);
        config.get(CAT_EXTRA, "overlayCorner", 0).set(extraSettings.overlayCorner.ordinal());
        config.get(CAT_EXTRA, "textContrast", 2).set(extraSettings.textContrast.ordinal());
        config.get(CAT_EXTRA, "steadyDebugHud", false).set(extraSettings.steadyDebugHud);
        config.get(CAT_EXTRA, "steadyDebugHudRefreshInterval", 20).set(extraSettings.steadyDebugHudRefreshInterval);

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

    // Settings classes
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
        public OverlayCorner overlayCorner = OverlayCorner.TOP_LEFT;
        public TextContrast textContrast = TextContrast.SHADOW;
        public boolean steadyDebugHud = false;
        public int steadyDebugHudRefreshInterval = 20;
    }
}
