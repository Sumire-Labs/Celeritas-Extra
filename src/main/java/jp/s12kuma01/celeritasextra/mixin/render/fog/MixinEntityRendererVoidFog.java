package jp.s12kuma01.celeritasextra.mixin.render.fog;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Suppresses the distance-based void fog that darkens the view near the world bottom.
 * <p>
 * In 1.12.2 void fog is applied inside {@link EntityRenderer#setupFog} from
 * {@link WorldProvider#getVoidFogYFactor()}. When void fog is disabled in the detail config this
 * redirect returns {@code 1.0}, which makes the game treat the player as fully above the void and
 * skip the effect; otherwise the real factor is used.
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
