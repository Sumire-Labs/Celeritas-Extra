/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerTickRateEstimatorTest {

    private final ServerTickRateEstimator estimator = new ServerTickRateEstimator();
    private final Object connection = new Object();

    @Test
    void measuresServerTicksRatherThanClientFramesAndCapsAtTwenty() {
        estimator.record(connection, 100, -1_000_000_000L);
        assertTrue(Double.isNaN(estimator.estimate(connection, 0)));
        estimator.record(connection, 120, 0);
        assertEquals(20, estimator.estimate(connection, 0));
        estimator.record(connection, 140, 2_000_000_000L);
        assertEquals(40.0 / 3, estimator.estimate(connection, 2_000_000_000L), 0.000001);
        estimator.record(connection, 160, 2_000_000_001L);
        assertEquals(20, estimator.estimate(connection, 2_000_000_001L), 0.000001);
    }

    @Test
    void usesFiveIntervalsAndAggregatesBurstsWithoutAveragingRates() {
        for (int i = 0; i <= 5; i++)
            estimator.record(connection, i * 20, i * 2_000_000_000L);
        assertEquals(10, estimator.estimate(connection, 10_000_000_000L));
        for (int i = 6; i <= 10; i++)
            estimator.record(connection, i * 20, (i + 5) * 1_000_000_000L);
        assertEquals(20, estimator.estimate(connection, 15_000_000_000L));
        estimator.record(connection, 220, 17_000_000_000L);
        estimator.record(connection, 240, 17_000_000_000L);
        assertEquals(20, estimator.estimate(connection, 17_000_000_000L));
    }

    @Test
    void hidesMissingStaleAndForeignConnections() {
        estimator.record(connection, 0, 0);
        estimator.record(connection, 20, 1_000_000_000L);
        assertTrue(Double.isNaN(estimator.estimate(null, 1_000_000_000L)));
        assertTrue(Double.isNaN(estimator.estimate(new Object(), 1_000_000_000L)));
        assertTrue(Double.isNaN(estimator.estimate(connection, 6_000_000_001L)));
        assertTrue(Double.isNaN(estimator.estimate(connection, 0)));
        estimator.reset(new Object());
        assertEquals(20, estimator.estimate(connection, 1_000_000_000L));
        estimator.reset(connection);
        assertTrue(Double.isNaN(estimator.estimate(connection, 1_000_000_000L)));
    }

    @Test
    void resetsAcrossServersAndTimeDiscontinuities() {
        estimator.record(connection, 100, 0);
        estimator.record(connection, 120, 1_000_000_000L);
        estimator.record(connection, 100, 2_000_000_000L);
        assertTrue(Double.isNaN(estimator.estimate(connection, 2_000_000_000L)));
        estimator.record(connection, 120, 3_000_000_000L);
        assertEquals(20, estimator.estimate(connection, 3_000_000_000L));
        estimator.record(connection, 10000, 4_000_000_000L);
        assertTrue(Double.isNaN(estimator.estimate(connection, 4_000_000_000L)));
        var other = new Object();
        estimator.record(other, 200, 5_000_000_000L);
        assertTrue(Double.isNaN(estimator.estimate(other, 5_000_000_000L)));
        assertTrue(Double.isNaN(estimator.estimate(connection, 5_000_000_000L)));
    }

    @Test
    void duplicateTicksDoNotMoveBaselineAndNegativeClockOriginsWork() {
        estimator.record(connection, 0, -2_000_000_000L);
        estimator.record(connection, 0, -1_500_000_000L);
        estimator.record(connection, 20, -1_000_000_000L);
        assertEquals(20, estimator.estimate(connection, -1_000_000_000L));
        estimator.record(connection, 40, -3_000_000_000L);
        assertTrue(Double.isNaN(estimator.estimate(connection, -3_000_000_000L)));
    }

}
