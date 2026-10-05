package jp.s12kuma01.celeritasextra.client.particle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParticleSpawnRateTest {
    private final ParticleClassRegistry registry = ParticleClassRegistry.getInstance();

    @BeforeEach @AfterEach void reset() {
        registry.loadDisabledClasses(new String[0]);
        registry.loadSpawnPercentages(new String[0]);
        registry.markClean();
    }

    @Test void defaultsAndLegacyDisabledClassesArePreserved() {
        assertEquals(100, registry.getSpawnPercentage("example.Smoke"));
        registry.loadDisabledClasses(new String[]{"example.Smoke"});
        registry.loadSpawnPercentages(new String[]{"example.Smoke|50"});
        assertEquals(0, registry.getSpawnPercentage("example.Smoke"));
        registry.setSpawnPercentage("example.Smoke", 25);
        assertFalse(registry.isClassDisabled("example.Smoke"));
        assertEquals(25, registry.getSpawnPercentage("example.Smoke"));
        assertTrue(registry.isDirty());
    }

    @Test void settingsRoundTripAndMalformedEntriesDoNotDiscardValidOnes() {
        registry.loadSpawnPercentages(new String[]{"example.Smoke|50", "bad", "example.Bad|nan", "example.High|150", "example.Low|-5"});
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

    @Test void percentageHasExactAcceptanceAcrossTheHundredPossibleRolls() {
        for (int percentage : new int[]{0, 1, 25, 50, 99, 100}) {
            int accepted = 0;
            for (int roll = 0; roll < 100; roll++) if (ParticleClassRegistry.acceptSpawn(percentage, roll)) accepted++;
            assertEquals(percentage, accepted);
        }
    }
}
