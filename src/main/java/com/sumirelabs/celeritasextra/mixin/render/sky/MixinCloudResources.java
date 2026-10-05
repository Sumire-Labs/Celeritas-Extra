package com.sumirelabs.celeritasextra.mixin.render.sky;

import com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod;
import com.sumirelabs.celeritasextra.client.render.cloud.ModernCloudAssets;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

/** Makes the texture available at the standard path to any existing cloud renderer. */
@Mixin(SimpleReloadableResourceManager.class)
public class MixinCloudResources {

    @ModifyVariable(method = "reloadResources", at = @At("HEAD"), argsOnly = true)
    private List<IResourcePack> celeritasExtra$addCloudTexture(List<IResourcePack> packs) {
        return ModernCloudAssets.withCloudTexture(packs,
                CeleritasExtraClientMod.options().renderSettings.modernClouds);
    }
}
