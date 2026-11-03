package com.sumirelabs.celeritasextra.client.particle;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParticleSpawnRateTest {

    private final ParticleClassRegistry registry = ParticleClassRegistry.getInstance();

    @Test
    void cachedClassPolicyObservesAllSettingsMutationPaths() {
        String name = getClass().getName();
        assertEquals(100, registry.observeSpawn(getClass()));
        registry.setSpawnPercentage(name, 25);
        assertEquals(25, registry.observeSpawn(getClass()));
        registry.setClassEnabled(name, false);
        assertEquals(0, registry.observeSpawn(getClass()));
        registry.setClassEnabled(name, true);
        assertEquals(25, registry.observeSpawn(getClass()));
        registry.loadSpawnPercentages(new String[]{name + "|75"});
        assertEquals(75, registry.observeSpawn(getClass()));
        registry.loadDisabledClasses(new String[]{name});
        assertEquals(0, registry.observeSpawn(getClass()));
        registry.loadDisabledClasses(new String[0]);
        assertEquals(75, registry.observeSpawn(getClass()));
    }

    @Test
    void vanillaZeroRateSkipsCreationButCustomFactoriesRetainDiscoveryPath() {
        var factory = new net.minecraft.client.particle.ParticleFlame.Factory();
        String name = net.minecraft.client.particle.ParticleFlame.class.getName();
        registry.setSpawnPercentage(name, 50);
        assertFalse(registry.skipVanillaFactory(factory, true), "Probabilistic filtering must run only once");
        registry.setSpawnPercentage(name, 0);
        assertTrue(registry.skipVanillaFactory(factory, true));
        assertTrue(registry.getDiscoveredClasses().containsKey(name));
        registry.setSpawnPercentage(name, 100);
        assertFalse(registry.skipVanillaFactory(factory, true));
        assertTrue(registry.skipVanillaFactory(factory, false));
        net.minecraft.client.particle.IParticleFactory custom = (id, world, x, y, z, vx, vy, vz, args) -> null;
        assertFalse(registry.skipVanillaFactory(custom, false));
    }

    @BeforeEach
    @AfterEach
    void reset() {
        registry.loadDisabledClasses(new String[0]);
        registry.loadSpawnPercentages(new String[0]);
        registry.markClean();
    }

    @Test
    void defaultsAndLegacyDisabledClassesArePreserved() {
        assertEquals(100, registry.getSpawnPercentage("example.Smoke"));
        registry.loadDisabledClasses(new String[]{"example.Smoke"});
        registry.loadSpawnPercentages(new String[]{"example.Smoke|50"});
        assertEquals(0, registry.getSpawnPercentage("example.Smoke"));
        registry.setSpawnPercentage("example.Smoke", 25);
        assertFalse(registry.isClassDisabled("example.Smoke"));
        assertEquals(25, registry.getSpawnPercentage("example.Smoke"));
        assertTrue(registry.isDirty());
    }

    @Test
    void settingsRoundTripAndMalformedEntriesDoNotDiscardValidOnes() {
        registry.loadSpawnPercentages(
            new String[]{"example.Smoke|50", "bad", "example.Bad|nan", "example.High|150", "example.Low|-5"});
        assertEquals(50, registry.getSpawnPercentage("example.Smoke"));
        assertEquals(100, registry.getSpawnPercentage("example.High"));
        assertEquals(0, registry.getSpawnPercentage("example.Low"));
        String[] persisted = registry.getSpawnPercentagesArray();
        registry.loadSpawnPercentages(persisted);
        assertEquals(50, registry.getSpawnPercentage("example.Smoke"));
        registry.setSpawnPercentage("example.Smoke", 0);
        assertTrue(registry.isClassDisabled("example.Smoke"));
        registry.setSpawnPercentage("example.Smoke", 100);
        assertEquals(100, registry.getSpawnPercentage("example.Smoke"));
    }

    @Test
    void percentageHasExactAcceptanceAcrossTheHundredPossibleRolls() {
        for (int percentage : new int[]{0, 1, 25, 50, 99, 100}) {
            int accepted = 0;
            for (int roll = 0; roll < 100; roll++)
                if (ParticleClassRegistry.acceptSpawn(percentage, roll))
                    accepted++;
            assertEquals(percentage, accepted);
        }
    }

}
