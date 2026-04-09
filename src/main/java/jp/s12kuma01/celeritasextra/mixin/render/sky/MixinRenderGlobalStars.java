package jp.s12kuma01.celeritasextra.mixin.render.sky;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.RenderStars;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Controls star rendering and generation.
 * - Toggles star visibility via star brightness wrapping
 * - Overrides star generation to use configurable star count
 */
@Mixin(RenderGlobal.class)
public class MixinRenderGlobalStars {

    @Shadow
    private VertexBuffer starVBO;

    @Shadow
    private VertexFormat vertexBufferFormat;

    /**
     * Control star rendering by wrapping the star brightness calculation.
     * Returns 0.0f when stars are disabled, preventing star rendering.
     */
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

    /**
     * Override star generation to use configurable star count.
     * Ported from Angelica's NotFine RenderStars.
     *
     * @author Celeritas Extra
     * @reason Configurable star count
     */
    @Overwrite
    private void generateStars() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        if (this.starVBO != null) {
            this.starVBO.deleteGlBuffers();
        }

        this.starVBO = new VertexBuffer(this.vertexBufferFormat);

        buffer.begin(7, DefaultVertexFormats.POSITION);

        int totalStars = CeleritasExtraClientMod.options().detailSettings.totalStars;
        RenderStars.generateStars(buffer, totalStars);

        buffer.finishDrawing();
        buffer.reset();
        this.starVBO.bufferData(buffer.getByteBuffer());
    }
}
