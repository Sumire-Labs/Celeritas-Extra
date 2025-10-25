package jp.s12kuma01.celeritasextra.mixin.render.sky;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobalStars {

    @WrapOperation(
        method = "renderSky(FI)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getStarBrightness(F)F"
        )
    )
    private float wrapGetStarBrightness(World world, float partialTicks, Operation<Float> original) {
        if (!CeleritasExtraClientMod.options().detailSettings.stars) {
            return 0.0f;
        }
        return original.call(world, partialTicks);
    }
}
