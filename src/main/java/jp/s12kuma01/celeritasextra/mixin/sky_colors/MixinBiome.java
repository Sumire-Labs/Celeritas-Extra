package jp.s12kuma01.celeritasextra.mixin.sky_colors;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Toggles biome-based sky coloring (Sodium Extra's "Sky Colors").
 * <p>
 * {@code World.getSkyColorBody} blends each nearby biome's {@link Biome#getSkyColorByTemp(float)}.
 * When the option is off, we force the temperature to a fixed value so every biome yields the same
 * sky color — removing biome variation while keeping the vanilla time-of-day/weather sky shading.
 */
@Mixin(Biome.class)
public class MixinBiome {

    /** Plains-like temperature; produces the standard overworld sky blue. */
    private static final float CELERITASEXTRA$UNIFORM_TEMPERATURE = 0.8F;

    @ModifyVariable(method = "getSkyColorByTemp", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float celeritasExtra$uniformSkyColor(float temperature) {
        if (!CeleritasExtraClientMod.options().detailSettings.skyColors) {
            return CELERITASEXTRA$UNIFORM_TEMPERATURE;
        }
        return temperature;
    }
}
