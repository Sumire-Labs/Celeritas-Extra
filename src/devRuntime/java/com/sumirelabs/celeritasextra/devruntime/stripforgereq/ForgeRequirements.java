package com.sumirelabs.celeritasextra.devruntime.stripforgereq;

import net.minecraftforge.fml.common.ModMetadata;

/** Preserves the original helper's filtering, without changing other Mod dependencies or ordering. */
public final class ForgeRequirements {

    private ForgeRequirements() {}

    public static void strip(ModMetadata metadata) {
        if (metadata.requiredMods != null) {
            metadata.requiredMods.removeIf(version -> version.getLabel().equals("forge"));
        }
        if (metadata.dependencies != null) {
            metadata.dependencies.removeIf(version -> version.getLabel().equals("forge"));
        }
    }

}
