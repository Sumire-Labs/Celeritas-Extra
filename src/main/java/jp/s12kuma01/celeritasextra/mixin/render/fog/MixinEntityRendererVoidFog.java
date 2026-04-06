package jp.s12kuma01.celeritasextra.mixin.render.fog;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Controls void fog rendering.
 * In 1.12.2, void fog is applied in EntityRenderer.setupFog() based on
 * WorldProvider.getVoidFogYFactor(). Returning 1.0 disables the void fog effect.
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRendererVoidFog {

    @Redirect(
            method = "setupFog",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/WorldProvider;getVoidFogYFactor()D")
    )
    private double celeritasExtra$toggleVoidFog(WorldProvider provider) {
        if (!CeleritasExtraClientMod.options().detailSettings.voidFog) {
            return 1.0D;
        }
        return provider.getVoidFogYFactor();
    }
}
