package jp.s12kuma01.celeritasextra.mixin.particle;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.particle.ParticleClassRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls particle rendering and spawning
 * Ported from MixinParticleManager in Embeddium Extra 1.20.1
 */
@Mixin(ParticleManager.class)
public class MixinParticleManager {

    /**
     * Capture the owning mod of each particle factory as it is registered.
     * This is the authoritative attribution signal for factories that cannot be resolved
     * statically (lambdas/anonymous classes), letting discovered classes be grouped by mod.
     */
    @Inject(method = "registerParticle", at = @At("HEAD"))
    public void onRegisterParticle(int id, IParticleFactory particleFactory, CallbackInfo ci) {
        if (particleFactory == null) {
            return;
        }
        String modId = null;
        try {
            ModContainer container = Loader.instance().activeModContainer();
            if (container != null) {
                modId = container.getModId();
            }
        } catch (Throwable t) {
            // Best-effort attribution only.
        }
        ParticleClassRegistry.getInstance().registerFactoryMod(particleFactory, modId);
    }

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
     * Intercept all particle additions via addEffect.
     * Records discovered particle classes (identity-guarded, cheap after first sighting)
     * and filters by global toggle + per-class disabled set.
     */
    @Inject(method = "addEffect", at = @At("HEAD"), cancellable = true)
    public void onAddEffect(Particle effect, CallbackInfo ci) {
        if (effect == null) {
            return;
        }

        ParticleClassRegistry registry = ParticleClassRegistry.getInstance();
        registry.recordClass(effect.getClass());

        if (!CeleritasExtraClientMod.options().particleSettings.particles) {
            ci.cancel();
            return;
        }

        if (!registry.isEmptyDisabled() && registry.isClassDisabled(effect.getClass().getName())) {
            ci.cancel();
        }
    }
}
