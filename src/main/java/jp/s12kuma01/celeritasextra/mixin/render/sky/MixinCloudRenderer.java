package jp.s12kuma01.celeritasextra.mixin.render.sky;

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
 * Controls cloud render distance via Forge's CloudRenderer.
 * <p>
 * The CloudRenderer caches cloud geometry in a VBO/display list, using the
 * {@code renderDistance} field for both dirty-checking (in {@code checkSettings()})
 * and geometry extent (in {@code vertices()}). We decouple these two uses:
 * <ul>
 *   <li>Redirect the dirty-check read to prevent spurious rebuilds every frame</li>
 *   <li>Redirect the geometry read to apply our custom cloud distance</li>
 *   <li>Inject at HEAD to detect setting changes and trigger rebuilds</li>
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

    /**
     * Detect cloud distance setting changes and force a geometry rebuild.
     * Runs before the vanilla dirty-check so that {@code isBuilt()} returns false,
     * causing the subsequent {@code build()} call to use our redirected distance.
     */
    @Inject(method = "checkSettings", at = @At("HEAD"))
    private void celeritasExtra$onCheckSettings(CallbackInfo ci) {
        int cloudDist = CeleritasExtraClientMod.options().renderSettings.cloudDistance;
        if (cloudDist != celeritasExtra$prevCloudDist) {
            dispose();
            celeritasExtra$prevCloudDist = cloudDist;
        }
    }

    /**
     * Bypass the dirty-check when a custom cloud distance is active.
     * <p>
     * The vanilla check is {@code mc.gameSettings.renderDistanceChunks != renderDistance}.
     * When {@code cloudDistance > 0}, we return {@code renderDistanceChunks} so the
     * comparison always evaluates to equal, preventing unwanted dispose/rebuild cycles.
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
     * <p>
     * In {@code vertices()}, the expression {@code ceilToScale((renderDistance * 2) * 16)}
     * determines how far clouds stretch. We replace {@code renderDistance} with our
     * custom value when active.
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
}
