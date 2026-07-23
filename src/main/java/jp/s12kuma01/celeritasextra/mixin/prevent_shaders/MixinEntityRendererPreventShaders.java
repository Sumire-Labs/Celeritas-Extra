package jp.s12kuma01.celeritasextra.mixin.prevent_shaders;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Blocks the vanilla post-processing shader pipeline from being activated.
 * Port of embeddium-extra's prevent_shaders.MixinGameRenderer.
 * <p>
 * When enabled in the render config, this cancels {@link EntityRenderer#loadShader} and
 * {@code switchUseShader} at their {@code HEAD} so effects such as the spectator/creeper/entity
 * view shaders never take over, avoiding conflicts and overhead with Celeritas' own rendering.
 * <p>
 * In 1.20.1: GameRenderer.togglePostProcessorEnabled() / loadPostProcessor()
 * In 1.12.2: EntityRenderer.loadShader() / switchUseShader()
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRendererPreventShaders {

    /**
     * Prevent loading shaders
     */
    @Inject(
            method = "loadShader",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventLoadShader(ResourceLocation resourceLocationIn, CallbackInfo ci) {
        if (CeleritasExtraClientMod.options().renderSettings.preventShaders) {
            ci.cancel();
        }
    }

    /**
     * Prevent toggling shader use
     */
    @Inject(
            method = "switchUseShader",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventSwitchUseShader(CallbackInfo ci) {
        if (CeleritasExtraClientMod.options().renderSettings.preventShaders) {
            ci.cancel();
        }
    }
}
