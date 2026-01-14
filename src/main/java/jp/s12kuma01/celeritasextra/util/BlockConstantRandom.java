package jp.s12kuma01.celeritasextra.util;

import net.minecraft.util.math.BlockPos;

/**
 * Utility for generating deterministic random values based on block position
 * Used for consistent leaf culling randomness that doesn't change each frame
 */
public final class BlockConstantRandom {

    private BlockConstantRandom() {}

    /**
     * Returns a deterministic random value [0.0, 1.0) based on block position using seeded algorithm
     * This produces more evenly distributed values than the hash method
     */
    public static float getConstantRandom(BlockPos pos) {
        // Use Java's LCG constants for seed calculation
        long seed = (pos.toLong() ^ 25214903917L) & 281474976710655L;
        seed = seed * 25214903917L + 11L & 281474976710655L;
        return ((int) (seed >> 24)) * 5.9604645E-8F;
    }
}
