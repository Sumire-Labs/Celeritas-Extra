package com.sumirelabs.celeritasextra.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SignTextVisibilityTest {

    @Test
    void precomputedNormalsPreserveEveryRotationAndNearPlaneMargin() {
        for (boolean standing : new boolean[]{false, true})
            for (int metadata = 0; metadata < 16; metadata++) {
                double angle = standing
                    ? metadata * Math.PI / 8
                    : switch (metadata) {
                        case 2 -> Math.PI;
                        case 4 -> Math.PI / 2;
                        case 5 -> -Math.PI / 2;
                        default -> 0;
                    };
                for (double distance : new double[]{-5, -0.49, 0, 0.49, 5}) {
                    double x = -Math.sin(angle) * distance, z = Math.cos(angle) * distance;
                    assertEquals(distance < -0.5, SignTextVisibility.hidden(x, z, 0, 0, metadata, standing));
                }
            }
    }

}
