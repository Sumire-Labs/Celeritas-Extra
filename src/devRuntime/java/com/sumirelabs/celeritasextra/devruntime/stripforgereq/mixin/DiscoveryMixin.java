package com.sumirelabs.celeritasextra.devruntime.stripforgereq.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sumirelabs.celeritasextra.devruntime.stripforgereq.ForgeRequirements;

import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.discovery.DirectoryDiscoverer;
import net.minecraftforge.fml.common.discovery.JarDiscoverer;

/** Both discovery paths finish binding metadata before Loader checks version requirements. */
@Mixin(value = {DirectoryDiscoverer.class, JarDiscoverer.class}, remap = false)
abstract class DiscoveryMixin {

    @Inject(method = "discover", at = @At("RETURN"), require = 1)
    private void celeritasExtra$stripForgeRequirements(CallbackInfoReturnable<List<ModContainer>> result) {
        for (ModContainer container : result.getReturnValue()) {
            if (container instanceof FMLModContainer) {
                ForgeRequirements.strip(container.getMetadata());
            }
        }
    }

}
