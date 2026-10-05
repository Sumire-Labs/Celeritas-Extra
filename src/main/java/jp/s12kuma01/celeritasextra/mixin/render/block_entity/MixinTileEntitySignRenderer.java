package jp.s12kuma01.celeritasextra.mixin.render.block_entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.VisibilityRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.tileentity.TileEntitySignRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TileEntitySignRenderer.class)
public class MixinTileEntitySignRenderer {
    @WrapOperation(method = "render(Lnet/minecraft/tileentity/TileEntitySign;DDDFIF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I"))
    private int celeritasExtra$visibleText(FontRenderer font, String text, int x, int y, int color,
                                          Operation<Integer> original,
                                          @Local(argsOnly = true) TileEntitySign sign) {
        Minecraft mc = Minecraft.getMinecraft();
        if (CeleritasExtraClientMod.options().renderSettings.signTextCulling && sign.hasWorld()
                && sign.lineBeingEdited < 0 && !(mc.currentScreen instanceof GuiEditSign)
                && mc.getRenderViewEntity() != null) {
            int metadata = sign.getBlockMetadata();
            double angle;
            if (sign.getBlockType() == Blocks.STANDING_SIGN) angle = metadata * Math.PI / 8;
            else if (sign.getBlockType() == Blocks.WALL_SIGN) {
                angle = switch (metadata) { case 2 -> Math.PI; case 4 -> Math.PI / 2; case 5 -> -Math.PI / 2; default -> 0; };
            } else return original.call(font, text, x, y, color);
            Vec3d camera = ActiveRenderInfo.projectViewFromEntity(mc.getRenderViewEntity(), mc.getRenderPartialTicks());
            // Wall signs sit near a block boundary. A half-block margin keeps near-plane text visible.
            if (VisibilityRules.behindHorizontalFace(camera.x, camera.z,
                    sign.getPos().getX() + 0.5, sign.getPos().getZ() + 0.5,
                    -Math.sin(angle), Math.cos(angle), 0.5)) return 0;
        }
        return original.call(font, text, x, y, color);
    }
}
