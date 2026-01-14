package jp.s12kuma01.celeritasextra.mixin.particle;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Controls particle rendering and spawning
 * Ported from MixinParticleManager in Embeddium Extra 1.20.1
 */
@Mixin(ParticleManager.class)
public class MixinParticleManager {

    /**
     * Control block breaking particles (when a block is destroyed)
     */
    @Inject(method = "addBlockDestroyEffects", at = @At("HEAD"), cancellable = true)
    public void addBlockDestroyEffects(BlockPos pos, IBlockState state, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().particleSettings.particles ||
            !CeleritasExtraClientMod.options().particleSettings.blockBreak) {
            ci.cancel();
        }
    }

    /**
     * Control block hit particles (when hitting a block)
     */
    @Inject(method = "addBlockHitEffects(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)V", at = @At("HEAD"), cancellable = true)
    public void addBlockHitEffects(BlockPos pos, EnumFacing side, CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().particleSettings.particles ||
            !CeleritasExtraClientMod.options().particleSettings.blockBreaking) {
            ci.cancel();
        }
    }

    /**
     * Control individual particle types by their EnumParticleTypes ID
     * This intercepts the main particle spawning method
     */
    @Inject(method = "spawnEffectParticle", at = @At("HEAD"), cancellable = true)
    public void spawnEffectParticle(int particleId, double x, double y, double z,
                                     double xSpeed, double ySpeed, double zSpeed,
                                     int[] parameters, CallbackInfoReturnable<Particle> cir) {
        if (!CeleritasExtraClientMod.options().particleSettings.isParticleEnabled(particleId)) {
            cir.setReturnValue(null);
        }
    }
}
