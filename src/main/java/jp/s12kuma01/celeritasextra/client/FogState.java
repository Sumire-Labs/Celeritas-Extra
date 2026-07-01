package jp.s12kuma01.celeritasextra.client;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;

/**
 * Detects gameplay-important fog so the mod's fog controls never strip or alter it.
 * <p>
 * Inspired by Sodium Extra's "protected gameplay fog": disabling atmospheric fog or changing the
 * fog distance should not remove the fog that conveys gameplay state — blindness, or being
 * submerged in water/lava. (Boss fog is not detected here.)
 */
public final class FogState {

    private FogState() {
    }

    public static boolean isGameplayFog() {
        Minecraft mc = Minecraft.getMinecraft();
        Entity view = mc.getRenderViewEntity();
        if (view == null) {
            return false;
        }
        if (view instanceof EntityLivingBase
                && ((EntityLivingBase) view).isPotionActive(MobEffects.BLINDNESS)) {
            return true;
        }
        return view.isInsideOfMaterial(Material.WATER) || view.isInsideOfMaterial(Material.LAVA);
    }
}
