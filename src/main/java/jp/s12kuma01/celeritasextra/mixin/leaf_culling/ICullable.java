package jp.s12kuma01.celeritasextra.mixin.leaf_culling;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Interface for blocks that support custom culling logic
 */
public interface ICullable {
    /**
     * Determines if a side should be culled (not rendered)
     * @return true if the side should be culled, false to render normally
     */
    boolean celeritasextra$shouldCullSide(IBlockState state, IBlockAccess access, BlockPos pos, EnumFacing facing);
}
