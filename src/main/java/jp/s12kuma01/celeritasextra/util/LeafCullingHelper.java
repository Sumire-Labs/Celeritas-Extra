package jp.s12kuma01.celeritasextra.util;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraGameOptions;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.function.Predicate;

/**
 * Helper class for leaf culling calculations
 */
public final class LeafCullingHelper {

    private LeafCullingHelper() {}

    /**
     * Determines if a side should be culled based on surrounding blocks
     *
     * @param depth Maximum depth to check for leaf blocks
     * @param pos Current block position
     * @param access Block access for checking neighbors
     * @param facing Direction to check
     * @param blockCheck Predicate to determine if a block counts as "same type"
     * @return true if the side should be culled (not rendered)
     */
    public static boolean shouldCullSide(int depth, BlockPos pos, IBlockAccess access,
                                         EnumFacing facing, Predicate<Block> blockCheck) {
        CeleritasExtraGameOptions.LeafCullingSettings settings =
            CeleritasExtraClientMod.options().leafCullingSettings;

        float rejectionChance = settings.getEffectiveRandomRejection();
        boolean allLeaves = true;
        boolean firstBlockIsLeaf = false;

        // Check each layer up to depth
        for (int i = 1; i <= depth; i++) {
            BlockPos checkPos = pos.offset(facing, i);
            IBlockState state = access.getBlockState(checkPos);
            boolean isLeaf = blockCheck.test(state.getBlock());

            if (i == 1) {
                firstBlockIsLeaf = isLeaf;
            }

            if (!isLeaf) {
                allLeaves = false;
                break; // Early exit - no need to check further
            }
        }

        // If all checked blocks are leaves, cull this side
        if (allLeaves) {
            return true;
        }

        // If the first block is not a leaf (edge of tree), don't cull
        if (!firstBlockIsLeaf) {
            return false;
        }

        // For interior blocks that failed the depth check, apply random rejection
        // This creates a more natural appearance by randomly showing some interior faces
        if (rejectionChance > 0 && BlockConstantRandom.getConstantRandom(pos) < rejectionChance) {
            return true;
        }

        return false;
    }
}
