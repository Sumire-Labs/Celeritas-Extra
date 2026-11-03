package com.sumirelabs.celeritasextra.devruntime.stripforgereq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.versioning.VersionParser;

import static org.junit.jupiter.api.Assertions.*;

class ForgeRequirementsTest {

    @Test
    void stripsOnlyForgeRequirementsAndAfterDependencies() {
        var forge = VersionParser.parseVersionReference("forge@[99.0,)");
        var cleanroom = VersionParser.parseVersionReference("cleanroom@[0.7.14-alpha,0.8)");
        var renderer = VersionParser.parseVersionReference("celeritas");
        var metadata = new ModMetadata();
        metadata.requiredMods = new HashSet<>(List.of(forge, cleanroom));
        metadata.dependencies = new ArrayList<>(List.of(forge, cleanroom, renderer));
        metadata.dependants = new ArrayList<>(List.of(forge, renderer));

        ForgeRequirements.strip(metadata);

        assertEquals(new HashSet<>(List.of(cleanroom)), metadata.requiredMods);
        assertEquals(List.of(cleanroom, renderer), metadata.dependencies);
        assertEquals(List.of(forge, renderer), metadata.dependants);
    }

    @Test
    void acceptsUnsetDependencyCollections() {
        var metadata = new ModMetadata();
        metadata.requiredMods = null;
        metadata.dependencies = null;
        assertDoesNotThrow(() -> ForgeRequirements.strip(metadata));
    }

}
