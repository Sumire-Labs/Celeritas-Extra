package com.sumirelabs.celeritasextra.client;

import com.sumirelabs.celeritasextra.client.gui.CeleritasExtraGameOptions.RenderSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CloudPassStateTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void explicitCloudDistanceWinsWithEitherTexture(boolean modernClouds) {
        RenderSettings settings = new RenderSettings();
        settings.modernClouds = modernClouds;
        settings.cloudDistance = 63;

        assertEquals(63, CloudPassState.effectiveCloudDistanceChunks(settings, 12));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void defaultCloudDistanceFollowsGameDistanceWithEitherTexture(boolean modernClouds) {
        RenderSettings settings = new RenderSettings();
        settings.modernClouds = modernClouds;
        settings.cloudDistance = 0;

        assertEquals(12, CloudPassState.effectiveCloudDistanceChunks(settings, 12));
        assertEquals(24, CloudPassState.effectiveCloudDistanceChunks(settings, 24));
    }

    @Test
    void physicalRangeMatchesForgesEstablishedLegacyCloudScale() {
        assertEquals(384.0F, CloudPassState.cloudHorizontalRangeBlocks(12));
        assertEquals(2_080.0F, CloudPassState.cloudHorizontalRangeBlocks(65));
        assertEquals(4_096.0F, CloudPassState.cloudHorizontalRangeBlocks(128));
    }
}
