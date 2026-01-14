package jp.s12kuma01.celeritasextra.client.gui;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jp.s12kuma01.celeritasextra.CeleritasExtraMod;
import net.minecraft.client.resources.I18n;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Main configuration class for Celeritas Extra
 * Stores all game options and settings
 */
public class CeleritasExtraGameOptions {

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

    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();

    public final AnimationSettings animationSettings = new AnimationSettings();
    public final ParticleSettings particleSettings = new ParticleSettings();
    public final DetailSettings detailSettings = new DetailSettings();
    public final RenderSettings renderSettings = new RenderSettings();
    public final ExtraSettings extraSettings = new ExtraSettings();

    private File file;

    public static CeleritasExtraGameOptions load(File file) {
        CeleritasExtraGameOptions config;

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                config = gson.fromJson(reader, CeleritasExtraGameOptions.class);
            } catch (Exception e) {
                CeleritasExtraMod.LOGGER.error("Could not parse config, falling back to defaults!", e);
                config = new CeleritasExtraGameOptions();
            }
        } else {
            config = new CeleritasExtraGameOptions();
        }

        config.file = file;
        config.writeChanges();

        return config;
    }

    public void writeChanges() {
        File dir = this.file.getParentFile();

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Could not create parent directories");
            }
        } else if (!dir.isDirectory()) {
            throw new RuntimeException("The parent file is not a directory");
        }

        try (FileWriter writer = new FileWriter(this.file)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException("Could not save configuration file", e);
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
        public boolean sculkSensor = true;
    }

    public static class ParticleSettings {
        public boolean particles = true;
        public boolean rainSplash = true;
        public boolean blockBreak = true;
        public boolean blockBreaking = true;

        // Individual particle type controls
        // These correspond to EnumParticleTypes in 1.12.2
        public boolean explosion = true;       // EXPLOSION_NORMAL, EXPLOSION_LARGE, EXPLOSION_HUGE
        public boolean fireworks = true;       // FIREWORKS_SPARK
        public boolean water = true;           // WATER_BUBBLE, WATER_SPLASH, WATER_WAKE, WATER_DROP
        public boolean suspended = true;       // SUSPENDED, SUSPENDED_DEPTH
        public boolean crit = true;            // CRIT, CRIT_MAGIC
        public boolean smoke = true;           // SMOKE_NORMAL, SMOKE_LARGE
        public boolean spell = true;           // SPELL, SPELL_INSTANT, SPELL_MOB, SPELL_MOB_AMBIENT, SPELL_WITCH
        public boolean drip = true;            // DRIP_WATER, DRIP_LAVA
        public boolean villager = true;        // VILLAGER_ANGRY, VILLAGER_HAPPY
        public boolean townAura = true;        // TOWN_AURA
        public boolean note = true;            // NOTE
        public boolean portal = true;          // PORTAL
        public boolean enchantmentTable = true;// ENCHANTMENT_TABLE
        public boolean flame = true;           // FLAME, LAVA
        public boolean cloud = true;           // CLOUD
        public boolean redstone = true;        // REDSTONE
        public boolean snowball = true;        // SNOWBALL, SNOW_SHOVEL
        public boolean slime = true;           // SLIME
        public boolean heart = true;           // HEART
        public boolean barrier = true;         // BARRIER
        public boolean itemCrack = true;       // ITEM_CRACK
        public boolean mobAppearance = true;   // MOB_APPEARANCE
        public boolean dragonBreath = true;    // DRAGON_BREATH
        public boolean endRod = true;          // END_ROD
        public boolean damageIndicator = true; // DAMAGE_INDICATOR
        public boolean sweepAttack = true;     // SWEEP_ATTACK
        public boolean fallingDust = true;     // FALLING_DUST
        public boolean totem = true;           // TOTEM
        public boolean spit = true;            // SPIT

        // Map for storing custom particle settings by name (for future extensibility)
        public Map<String, Boolean> customParticles = new HashMap<>();

        /**
         * Check if a particle type should be rendered based on its EnumParticleTypes ID
         */
        public boolean isParticleEnabled(int particleId) {
            if (!particles) return false;

            switch (particleId) {
                case 0: case 1: case 2:   // EXPLOSION_NORMAL, EXPLOSION_LARGE, EXPLOSION_HUGE
                    return explosion;
                case 3:                    // FIREWORKS_SPARK
                    return fireworks;
                case 4: case 5: case 6: case 39: // WATER_BUBBLE, WATER_SPLASH, WATER_WAKE, WATER_DROP
                    return water;
                case 7: case 8:            // SUSPENDED, SUSPENDED_DEPTH
                    return suspended;
                case 9: case 10:           // CRIT, CRIT_MAGIC
                    return crit;
                case 11: case 12:          // SMOKE_NORMAL, SMOKE_LARGE
                    return smoke;
                case 13: case 14: case 15: case 16: case 17: // SPELL variants
                    return spell;
                case 18: case 19:          // DRIP_WATER, DRIP_LAVA
                    return drip;
                case 20: case 21:          // VILLAGER_ANGRY, VILLAGER_HAPPY
                    return villager;
                case 22:                   // TOWN_AURA
                    return townAura;
                case 23:                   // NOTE
                    return note;
                case 24:                   // PORTAL
                    return portal;
                case 25:                   // ENCHANTMENT_TABLE
                    return enchantmentTable;
                case 26: case 27:          // FLAME, LAVA
                    return flame;
                case 29:                   // CLOUD
                    return cloud;
                case 30:                   // REDSTONE
                    return redstone;
                case 31: case 32:          // SNOWBALL, SNOW_SHOVEL
                    return snowball;
                case 33:                   // SLIME
                    return slime;
                case 34:                   // HEART
                    return heart;
                case 35:                   // BARRIER
                    return barrier;
                case 36:                   // ITEM_CRACK
                    return itemCrack;
                case 41:                   // MOB_APPEARANCE
                    return mobAppearance;
                case 42:                   // DRAGON_BREATH
                    return dragonBreath;
                case 43:                   // END_ROD
                    return endRod;
                case 44:                   // DAMAGE_INDICATOR
                    return damageIndicator;
                case 45:                   // SWEEP_ATTACK
                    return sweepAttack;
                case 46:                   // FALLING_DUST
                    return fallingDust;
                case 47:                   // TOTEM
                    return totem;
                case 48:                   // SPIT
                    return spit;
                default:
                    return true; // Allow unknown particles by default
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
        public int fogDistance = 0; // 0 = default (use render distance), 1-32 = chunks
        public FogType fogType = FogType.DEFAULT;
        public boolean clouds = true;
        public boolean multiDimensionalClouds = true;
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
        public boolean useAdaptiveSync = false;
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
