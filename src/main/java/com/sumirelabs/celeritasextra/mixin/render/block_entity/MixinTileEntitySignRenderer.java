package com.sumirelabs.celeritasextra.mixin.render.block_entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod;
import com.sumirelabs.celeritasextra.compat.ShaderPassCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.tileentity.TileEntitySignRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.sumirelabs.celeritasextra.client.SignTextVisibility;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TileEntitySignRenderer.class)
public class MixinTileEntitySignRenderer {
    @Unique private boolean celeritasExtra$hideText;

    @WrapMethod(method = "render(Lnet/minecraft/tileentity/TileEntitySign;DDDFIF)V")
    private void celeritasExtra$scopeVisibility(TileEntitySign sign, double x, double y, double z,
            float partialTicks, int destroyStage, float alpha, Operation<Void> original) {
        boolean previous = celeritasExtra$hideText;
        celeritasExtra$hideText = destroyStage < 0 && celeritasExtra$shouldHide(sign);
        try { original.call(sign, x, y, z, partialTicks, destroyStage, alpha); }
        finally { celeritasExtra$hideText = previous; }
    }

    @Unique private static boolean celeritasExtra$shouldHide(TileEntitySign sign) {
        if (!CeleritasExtraClientMod.options().renderSettings.signTextCulling || !sign.hasWorld()
                || sign.lineBeingEdited >= 0 || ShaderPassCompat.isShadowPass()) return false;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen instanceof GuiEditSign || mc.getRenderViewEntity() == null) return false;
        boolean standing = sign.getBlockType() == Blocks.STANDING_SIGN;
        if (!standing && sign.getBlockType() != Blocks.WALL_SIGN) return false;
        Vec3d camera = ActiveRenderInfo.projectViewFromEntity(mc.getRenderViewEntity(), mc.getRenderPartialTicks());
        return SignTextVisibility.hidden(camera.x, camera.z, sign.getPos().getX() + 0.5,
                sign.getPos().getZ() + 0.5, sign.getBlockMetadata(), standing);
    }

    @WrapOperation(method = "render(Lnet/minecraft/tileentity/TileEntitySign;DDDFIF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I"))
    private int celeritasExtra$visibleText(FontRenderer font, String text, int x, int y, int color,
                                          Operation<Integer> original) {
        return celeritasExtra$hideText ? 0 : original.call(font, text, x, y, color);
    }
}
