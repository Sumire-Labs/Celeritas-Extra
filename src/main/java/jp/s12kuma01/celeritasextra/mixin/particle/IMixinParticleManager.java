package jp.s12kuma01.celeritasextra.mixin.particle;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ParticleManager.class)
public interface IMixinParticleManager {

    @Accessor("particleTypes")
    Map<Integer, IParticleFactory> getParticleTypes();
}
