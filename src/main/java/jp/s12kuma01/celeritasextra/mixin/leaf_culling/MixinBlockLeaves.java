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
import org.spongepowered.asm.mixin.Unique;

/**
 * Mixin to implement leaf culling for BlockLeaves
 */
@Mixin(BlockLeaves.class)
public class MixinBlockLeaves implements ICullable {

    @Override
    @Unique
    public boolean celeritasextra$shouldCullSide(IBlockState state, IBlockAccess access,
                                                  BlockPos pos, EnumFacing facing) {
        if (!CeleritasExtraClientMod.options().leafCullingSettings.enabled) {
            return false; // Culling disabled, render all sides
        }

        // Use different depth based on graphics settings
        // Fast graphics already culls interior faces, so use depth 1
        // Fancy graphics shows all faces by default, so apply our culling
        int effectiveDepth = Minecraft.isFancyGraphicsEnabled()
            ? CeleritasExtraClientMod.options().leafCullingSettings.getEffectiveDepth()
            : 1;

        return LeafCullingHelper.shouldCullSide(
            effectiveDepth,
            pos, access, facing,
            block -> block instanceof BlockLeaves
        );
    }
}
