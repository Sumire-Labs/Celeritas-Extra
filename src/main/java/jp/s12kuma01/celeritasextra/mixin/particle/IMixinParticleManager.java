package jp.s12kuma01.celeritasextra.mixin.particle;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Accessor mixin for {@link ParticleManager} that exposes its private particle-factory registry.
 * <p>
 * Grants read access to the internal {@code particleTypes} map so registered particle factories can
 * be enumerated and attributed to their owning mod, backing the per-mod particle toggles.
 */
@Mixin(ParticleManager.class)
public interface IMixinParticleManager {

    /** Returns the internal map of particle IDs to their registered {@link IParticleFactory}. */
    @Accessor("particleTypes")
    Map<Integer, IParticleFactory> getParticleTypes();
}
