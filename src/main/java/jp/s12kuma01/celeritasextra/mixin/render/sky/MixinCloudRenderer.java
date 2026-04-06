package jp.s12kuma01.celeritasextra.mixin.render.sky;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.CloudRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Controls cloud render distance and cloud scale via Forge's CloudRenderer.
 * <p>
 * Cloud Distance: Two @Redirects decouple the dirty-check from geometry extent.
 * Cloud Scale: @Overwrite on getScale() and @ModifyConstant on HEIGHT (4).
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
    private int cloudMode;

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
     * Override getScale() to apply cloud scale multiplier.
     * Multiplier = cloudScale / 4.0, so scale 4 = vanilla (1.0x).
     *
     * @author Celeritas Extra
     * @reason Configurable cloud scale
     */
    @Overwrite
    private int getScale() {
        int baseScale = cloudMode == 2 ? 12 : 8;
        int cloudScale = CeleritasExtraClientMod.options().renderSettings.cloudScale;
        return Math.max(1, baseScale * cloudScale / 4);
    }

    /**
     * Scale the cloud HEIGHT constant (4) proportionally.
     * HEIGHT is inlined by the compiler, so @ModifyConstant targets the literal value.
     */
    @ModifyConstant(method = "vertices", constant = @Constant(intValue = 4))
    private int celeritasExtra$modifyCloudHeight(int original) {
        int cloudScale = CeleritasExtraClientMod.options().renderSettings.cloudScale;
        return Math.max(1, original * cloudScale / 4);
    }
}
