package jp.s12kuma01.celeritasextra.mixin.particle;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.particle.ParticleFirework;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls firework particles
 * In 1.12.2, firework particles are handled by ParticleFirework class
 */
@Mixin(ParticleFirework.class)
public class MixinParticleFirework {

    /**
     * Control firework explosion particles
     * In 1.12.2, this is in the Starter inner class
     */
    @Inject(
        method = "onUpdate",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onUpdate(CallbackInfo ci) {
        if (!CeleritasExtraClientMod.options().particleSettings.particles) {
            ci.cancel();
        }
    }
}
