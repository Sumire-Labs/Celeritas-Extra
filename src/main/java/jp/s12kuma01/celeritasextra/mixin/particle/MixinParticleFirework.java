package jp.s12kuma01.celeritasextra.mixin.particle;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;

import net.minecraft.client.particle.ParticleFirework;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Disables firework particles when particle rendering is disabled.
 */
@Mixin(ParticleFirework.class)
public class MixinParticleFirework {

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
