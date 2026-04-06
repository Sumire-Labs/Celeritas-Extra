package jp.s12kuma01.celeritasextra.mixin.particle;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Controls void particle rendering.
 * In 1.12.2, void particles are spawned in WorldClient.randomDisplayTick()
 * based on WorldProvider.getVoidFogYFactor(). Returning 1.0 disables the effect.
 */
@Mixin(WorldClient.class)
public class MixinWorldClient {

    @Redirect(
            method = "randomDisplayTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/WorldProvider;getVoidFogYFactor()D")
    )
    private double celeritasExtra$toggleVoidParticles(WorldProvider provider) {
        if (!CeleritasExtraClientMod.options().detailSettings.voidParticles) {
            return 1.0D;
        }
        return provider.getVoidFogYFactor();
    }
}
