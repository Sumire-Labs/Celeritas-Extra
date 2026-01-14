package jp.s12kuma01.celeritasextra.mixin.leaf_culling;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to intercept shouldSideBeRendered for leaf culling
 */
@Mixin(Block.class)
public class MixinBlock {

    @Inject(method = "shouldSideBeRendered", at = @At("HEAD"), cancellable = true)
    private void celeritasextra$onShouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess,
                                                        BlockPos pos, EnumFacing side,
                                                        CallbackInfoReturnable<Boolean> cir) {
        Block block = blockState.getBlock();
        if (block instanceof ICullable cullable) {
            if (cullable.celeritasextra$shouldCullSide(blockState, blockAccess, pos, side)) {
                cir.setReturnValue(false);
            }
        }
    }
}
