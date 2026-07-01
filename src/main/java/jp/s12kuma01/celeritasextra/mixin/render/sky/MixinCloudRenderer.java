package jp.s12kuma01.celeritasextra.mixin.render.sky;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.CloudRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls cloud render distance and cloud scale via Forge's CloudRenderer.
 * <p>
 * Cloud Distance: Two @Redirects decouple the dirty-check from geometry extent.
 * Cloud Scale: @ModifyReturnValue on getScale() (XZ tiling scale).
 * <p>
 * Scale values (1-4, default 4):
 * <ul>
 *   <li>1 = 0.25x (smallest clouds)</li>
 *   <li>2 = 0.5x</li>
 *   <li>3 = 0.75x</li>
 *   <li>4 = 1.0x (vanilla, default)</li>
 * </ul>
 */
@Mixin(value = CloudRenderer.class, remap = false)
public class MixinCloudRenderer {

    @Shadow
    private int renderDistance;

    @Shadow
    private void dispose() {}

    @Unique
    private int celeritasExtra$prevCloudDist = -1;

    @Unique
    private int celeritasExtra$prevCloudScale = -1;

    // ========================
    // Cloud Distance
    // ========================

    /**
     * Detect cloud distance/scale setting changes and force a geometry rebuild.
     */
    @Inject(method = "checkSettings", at = @At("HEAD"))
    private void celeritasExtra$onCheckSettings(CallbackInfo ci) {
        var options = CeleritasExtraClientMod.options().renderSettings;
        int cloudDist = options.cloudDistance;
        int cloudScale = options.cloudScale;
        if (cloudDist != celeritasExtra$prevCloudDist || cloudScale != celeritasExtra$prevCloudScale) {
            dispose();
            celeritasExtra$prevCloudDist = cloudDist;
            celeritasExtra$prevCloudScale = cloudScale;
        }
    }

    /**
     * Bypass the dirty-check when a custom cloud distance is active.
     */
    @Redirect(
            method = "checkSettings",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraftforge/client/CloudRenderer;renderDistance:I",
                    opcode = Opcodes.GETFIELD)
    )
    private int celeritasExtra$redirectDirtyCheck(CloudRenderer self) {
        int cloudDist = CeleritasExtraClientMod.options().renderSettings.cloudDistance;
        if (cloudDist > 0) {
            return Minecraft.getMinecraft().gameSettings.renderDistanceChunks;
        }
        return this.renderDistance;
    }

    /**
     * Override the render distance used for cloud geometry extent.
     */
    @Redirect(
            method = "vertices",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraftforge/client/CloudRenderer;renderDistance:I",
                    opcode = Opcodes.GETFIELD)
    )
    private int celeritasExtra$redirectCloudDistance(CloudRenderer self) {
        int cloudDist = CeleritasExtraClientMod.options().renderSettings.cloudDistance;
        return cloudDist > 0 ? cloudDist : this.renderDistance;
    }

    // ========================
    // Cloud Scale
    // ========================

    /**
     * Apply the cloud scale multiplier (cloudScale / 4.0, so 4 = vanilla 1.0x) on top of
     * the vanilla base scale returned by getScale(). Non-destructive: preserves the vanilla
     * cloudMode logic and any other mod's modifications, unlike an @Overwrite.
     */
    @ModifyReturnValue(method = "getScale", at = @At("RETURN"))
    private int celeritasExtra$scaleClouds(int original) {
        int cloudScale = CeleritasExtraClientMod.options().renderSettings.cloudScale;
        return Math.max(1, original * cloudScale / 4);
    }
}
