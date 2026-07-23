package jp.s12kuma01.celeritasextra.mixin.render.sky;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overrides the world's cloud altitude with the configured cloud height.
 * <p>
 * Injects at the return of {@code WorldProvider.getCloudHeight} and replaces the result with the
 * "cloudHeight" render setting. Since vanilla and other systems query {@code getCloudHeight} for the
 * altitude at which clouds are drawn, this is the single source of truth for cloud height relied on
 * by {@link MixinRenderGlobalClouds} and {@link MixinEntityRendererCloudTranslucency}.
 */
@Mixin(WorldProvider.class)
public class MixinWorldProvider {

    /**
     * Override the cloud height with the configured value
     */
    @Inject(method = "getCloudHeight", at = @At("RETURN"), cancellable = true)
    private void modifyCloudHeight(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue((float) CeleritasExtraClientMod.options().renderSettings.cloudHeight);
    }
}
