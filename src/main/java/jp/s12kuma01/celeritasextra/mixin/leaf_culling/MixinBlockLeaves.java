package jp.s12kuma01.celeritasextra.mixin.leaf_culling;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.util.LeafCullingHelper;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to implement leaf culling for BlockLeaves
 * Directly injects into BlockLeaves.shouldSideBeRendered to avoid Block class loading issues
 */
@Mixin(BlockLeaves.class)
public class MixinBlockLeaves {

    @Inject(method = "shouldSideBeRendered", at = @At("HEAD"), cancellable = true)
    private void celeritasextra$onShouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess,
                                                        BlockPos pos, EnumFacing side,
                                                        CallbackInfoReturnable<Boolean> cir) {
        if (!CeleritasExtraClientMod.options().leafCullingSettings.enabled) {
            return; // Culling disabled, let vanilla handle it
        }

        // Use different depth based on graphics settings
        // Fast graphics already culls interior faces, so use depth 1
        // Fancy graphics shows all faces by default, so apply our culling
        int effectiveDepth = Minecraft.isFancyGraphicsEnabled()
            ? CeleritasExtraClientMod.options().leafCullingSettings.getEffectiveDepth()
            : 1;

        boolean shouldCull = LeafCullingHelper.shouldCullSide(
            effectiveDepth,
            pos, blockAccess, side,
            block -> block instanceof BlockLeaves
        );

        if (shouldCull) {
            cir.setReturnValue(false);
        }
    }
}
