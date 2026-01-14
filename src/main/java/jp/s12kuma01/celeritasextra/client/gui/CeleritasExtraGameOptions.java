package jp.s12kuma01.celeritasextra.client.gui;

import jp.s12kuma01.celeritasextra.CeleritasExtraMod;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

/**
 * Main configuration class for Celeritas Extra
 * Uses Forge Configuration for .cfg file format
 */
public class CeleritasExtraGameOptions {

    // Category names
    private static final String CAT_ANIMATION = "animation";
    private static final String CAT_PARTICLE = "particle";
    private static final String CAT_PARTICLE_TYPES = "particle_types";
    private static final String CAT_DETAIL = "detail";
    private static final String CAT_RENDER = "render";
    private static final String CAT_EXTRA = "extra";
    private static final String CAT_LEAF_CULLING = "leaf_culling";

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

    /**
     * Leaf culling preset modes
     */
    public enum CullingPreset {
        FAST("celeritasextra.option.leaf_culling.preset.fast", 1, 0.0),
        BALANCED("celeritasextra.option.leaf_culling.preset.balanced", 2, 0.2),
        QUALITY("celeritasextra.option.leaf_culling.preset.quality", 3, 0.1),
        CUSTOM("celeritasextra.option.leaf_culling.preset.custom", 2, 0.2);

        private final String translationKey;
        public final int depth;
        public final double randomRejection;

        CullingPreset(String translationKey, int depth, double randomRejection) {
            this.translationKey = translationKey;
            this.depth = depth;
            this.randomRejection = randomRejection;
        }

        public String getLocalizedName() {
            return I18n.format(this.translationKey);
        }
    }

    private Configuration config;

    public final AnimationSettings animationSettings = new AnimationSettings();
    public final ParticleSettings particleSettings = new ParticleSettings();
    public final DetailSettings detailSettings = new DetailSettings();
    public final RenderSettings renderSettings = new RenderSettings();
    public final ExtraSettings extraSettings = new ExtraSettings();
    public final LeafCullingSettings leafCullingSettings = new LeafCullingSettings();

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

        // Particle settings - individual types
        particleSettings.explosion = config.getBoolean("explosion", CAT_PARTICLE_TYPES, true, "Enable/disable explosion particles");
        particleSettings.fireworks = config.getBoolean("fireworks", CAT_PARTICLE_TYPES, true, "Enable/disable firework particles");
        particleSettings.water = config.getBoolean("water", CAT_PARTICLE_TYPES, true, "Enable/disable water particles");
        particleSettings.suspended = config.getBoolean("suspended", CAT_PARTICLE_TYPES, true, "Enable/disable suspended particles");
        particleSettings.crit = config.getBoolean("crit", CAT_PARTICLE_TYPES, true, "Enable/disable critical hit particles");
        particleSettings.smoke = config.getBoolean("smoke", CAT_PARTICLE_TYPES, true, "Enable/disable smoke particles");
        particleSettings.spell = config.getBoolean("spell", CAT_PARTICLE_TYPES, true, "Enable/disable spell particles");
        particleSettings.drip = config.getBoolean("drip", CAT_PARTICLE_TYPES, true, "Enable/disable dripping particles");
        particleSettings.villager = config.getBoolean("villager", CAT_PARTICLE_TYPES, true, "Enable/disable villager particles");
        particleSettings.townAura = config.getBoolean("townAura", CAT_PARTICLE_TYPES, true, "Enable/disable town aura particles");
        particleSettings.note = config.getBoolean("note", CAT_PARTICLE_TYPES, true, "Enable/disable note particles");
        particleSettings.portal = config.getBoolean("portal", CAT_PARTICLE_TYPES, true, "Enable/disable portal particles");
        particleSettings.enchantmentTable = config.getBoolean("enchantmentTable", CAT_PARTICLE_TYPES, true, "Enable/disable enchantment table particles");
        particleSettings.flame = config.getBoolean("flame", CAT_PARTICLE_TYPES, true, "Enable/disable flame particles");
        particleSettings.cloud = config.getBoolean("cloud", CAT_PARTICLE_TYPES, true, "Enable/disable cloud particles");
        particleSettings.redstone = config.getBoolean("redstone", CAT_PARTICLE_TYPES, true, "Enable/disable redstone particles");
        particleSettings.snowball = config.getBoolean("snowball", CAT_PARTICLE_TYPES, true, "Enable/disable snowball particles");
        particleSettings.slime = config.getBoolean("slime", CAT_PARTICLE_TYPES, true, "Enable/disable slime particles");
        particleSettings.heart = config.getBoolean("heart", CAT_PARTICLE_TYPES, true, "Enable/disable heart particles");
        particleSettings.barrier = config.getBoolean("barrier", CAT_PARTICLE_TYPES, true, "Enable/disable barrier particles");
        particleSettings.itemCrack = config.getBoolean("itemCrack", CAT_PARTICLE_TYPES, true, "Enable/disable item crack particles");
        particleSettings.mobAppearance = config.getBoolean("mobAppearance", CAT_PARTICLE_TYPES, true, "Enable/disable mob appearance particles");
        particleSettings.dragonBreath = config.getBoolean("dragonBreath", CAT_PARTICLE_TYPES, true, "Enable/disable dragon breath particles");
        particleSettings.endRod = config.getBoolean("endRod", CAT_PARTICLE_TYPES, true, "Enable/disable end rod particles");
        particleSettings.damageIndicator = config.getBoolean("damageIndicator", CAT_PARTICLE_TYPES, true, "Enable/disable damage indicator particles");
        particleSettings.sweepAttack = config.getBoolean("sweepAttack", CAT_PARTICLE_TYPES, true, "Enable/disable sweep attack particles");
        particleSettings.fallingDust = config.getBoolean("fallingDust", CAT_PARTICLE_TYPES, true, "Enable/disable falling dust particles");
        particleSettings.totem = config.getBoolean("totem", CAT_PARTICLE_TYPES, true, "Enable/disable totem particles");
        particleSettings.spit = config.getBoolean("spit", CAT_PARTICLE_TYPES, true, "Enable/disable llama spit particles");

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

        // Leaf culling settings
        leafCullingSettings.enabled = config.getBoolean("enabled", CAT_LEAF_CULLING, true, "Enable/disable leaf culling");
        leafCullingSettings.preset = CullingPreset.values()[config.getInt("preset", CAT_LEAF_CULLING, 1, 0, CullingPreset.values().length - 1, "Culling preset (0=Fast, 1=Balanced, 2=Fancy, 3=Custom)")];
        leafCullingSettings.customDepth = config.getInt("customDepth", CAT_LEAF_CULLING, 2, 0, 5, "Custom culling depth (only used when preset is Custom)");
        leafCullingSettings.customRandomRejection = config.getInt("customRandomRejection", CAT_LEAF_CULLING, 20, 0, 50, "Custom random rejection percentage (only used when preset is Custom)");
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

        // Particle settings - individual types
        config.get(CAT_PARTICLE_TYPES, "explosion", true).set(particleSettings.explosion);
        config.get(CAT_PARTICLE_TYPES, "fireworks", true).set(particleSettings.fireworks);
        config.get(CAT_PARTICLE_TYPES, "water", true).set(particleSettings.water);
        config.get(CAT_PARTICLE_TYPES, "suspended", true).set(particleSettings.suspended);
        config.get(CAT_PARTICLE_TYPES, "crit", true).set(particleSettings.crit);
        config.get(CAT_PARTICLE_TYPES, "smoke", true).set(particleSettings.smoke);
        config.get(CAT_PARTICLE_TYPES, "spell", true).set(particleSettings.spell);
        config.get(CAT_PARTICLE_TYPES, "drip", true).set(particleSettings.drip);
        config.get(CAT_PARTICLE_TYPES, "villager", true).set(particleSettings.villager);
        config.get(CAT_PARTICLE_TYPES, "townAura", true).set(particleSettings.townAura);
        config.get(CAT_PARTICLE_TYPES, "note", true).set(particleSettings.note);
        config.get(CAT_PARTICLE_TYPES, "portal", true).set(particleSettings.portal);
        config.get(CAT_PARTICLE_TYPES, "enchantmentTable", true).set(particleSettings.enchantmentTable);
        config.get(CAT_PARTICLE_TYPES, "flame", true).set(particleSettings.flame);
        config.get(CAT_PARTICLE_TYPES, "cloud", true).set(particleSettings.cloud);
        config.get(CAT_PARTICLE_TYPES, "redstone", true).set(particleSettings.redstone);
        config.get(CAT_PARTICLE_TYPES, "snowball", true).set(particleSettings.snowball);
        config.get(CAT_PARTICLE_TYPES, "slime", true).set(particleSettings.slime);
        config.get(CAT_PARTICLE_TYPES, "heart", true).set(particleSettings.heart);
        config.get(CAT_PARTICLE_TYPES, "barrier", true).set(particleSettings.barrier);
        config.get(CAT_PARTICLE_TYPES, "itemCrack", true).set(particleSettings.itemCrack);
        config.get(CAT_PARTICLE_TYPES, "mobAppearance", true).set(particleSettings.mobAppearance);
        config.get(CAT_PARTICLE_TYPES, "dragonBreath", true).set(particleSettings.dragonBreath);
        config.get(CAT_PARTICLE_TYPES, "endRod", true).set(particleSettings.endRod);
        config.get(CAT_PARTICLE_TYPES, "damageIndicator", true).set(particleSettings.damageIndicator);
        config.get(CAT_PARTICLE_TYPES, "sweepAttack", true).set(particleSettings.sweepAttack);
        config.get(CAT_PARTICLE_TYPES, "fallingDust", true).set(particleSettings.fallingDust);
        config.get(CAT_PARTICLE_TYPES, "totem", true).set(particleSettings.totem);
        config.get(CAT_PARTICLE_TYPES, "spit", true).set(particleSettings.spit);

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

        // Leaf culling settings
        config.get(CAT_LEAF_CULLING, "enabled", true).set(leafCullingSettings.enabled);
        config.get(CAT_LEAF_CULLING, "preset", 1).set(leafCullingSettings.preset.ordinal());
        config.get(CAT_LEAF_CULLING, "customDepth", 2).set(leafCullingSettings.customDepth);
        config.get(CAT_LEAF_CULLING, "customRandomRejection", 20).set(leafCullingSettings.customRandomRejection);

        config.save();
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

        // Individual particle type controls
        public boolean explosion = true;
        public boolean fireworks = true;
        public boolean water = true;
        public boolean suspended = true;
        public boolean crit = true;
        public boolean smoke = true;
        public boolean spell = true;
        public boolean drip = true;
        public boolean villager = true;
        public boolean townAura = true;
        public boolean note = true;
        public boolean portal = true;
        public boolean enchantmentTable = true;
        public boolean flame = true;
        public boolean cloud = true;
        public boolean redstone = true;
        public boolean snowball = true;
        public boolean slime = true;
        public boolean heart = true;
        public boolean barrier = true;
        public boolean itemCrack = true;
        public boolean mobAppearance = true;
        public boolean dragonBreath = true;
        public boolean endRod = true;
        public boolean damageIndicator = true;
        public boolean sweepAttack = true;
        public boolean fallingDust = true;
        public boolean totem = true;
        public boolean spit = true;

        /**
         * Check if a particle type should be rendered based on its EnumParticleTypes ID
         */
        public boolean isParticleEnabled(int particleId) {
            if (!particles) return false;

            switch (particleId) {
                case 0: case 1: case 2:
                    return explosion;
                case 3:
                    return fireworks;
                case 4: case 5: case 6: case 39:
                    return water;
                case 7: case 8:
                    return suspended;
                case 9: case 10:
                    return crit;
                case 11: case 12:
                    return smoke;
                case 13: case 14: case 15: case 16: case 17:
                    return spell;
                case 18: case 19:
                    return drip;
                case 20: case 21:
                    return villager;
                case 22:
                    return townAura;
                case 23:
                    return note;
                case 24:
                    return portal;
                case 25:
                    return enchantmentTable;
                case 26: case 27:
                    return flame;
                case 29:
                    return cloud;
                case 30:
                    return redstone;
                case 31: case 32:
                    return snowball;
                case 33:
                    return slime;
                case 34:
                    return heart;
                case 35:
                    return barrier;
                case 36:
                    return itemCrack;
                case 41:
                    return mobAppearance;
                case 42:
                    return dragonBreath;
                case 43:
                    return endRod;
                case 44:
                    return damageIndicator;
                case 45:
                    return sweepAttack;
                case 46:
                    return fallingDust;
                case 47:
                    return totem;
                case 48:
                    return spit;
                default:
                    return true;
            }
        }
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

    public static class LeafCullingSettings {
        public boolean enabled = true;
        public CullingPreset preset = CullingPreset.BALANCED;
        public int customDepth = 2;
        public int customRandomRejection = 20; // percentage (0-50)

        /**
         * Get effective depth based on current preset
         */
        public int getEffectiveDepth() {
            if (!enabled) return 0;
            return preset == CullingPreset.CUSTOM ? customDepth : preset.depth;
        }

        /**
         * Get effective random rejection based on current preset (as float 0.0-1.0)
         */
        public float getEffectiveRandomRejection() {
            if (!enabled) return 0.0f;
            return (float) (preset == CullingPreset.CUSTOM ? customRandomRejection / 100.0 : preset.randomRejection);
        }
    }
}
