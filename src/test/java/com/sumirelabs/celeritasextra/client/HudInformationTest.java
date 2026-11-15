/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HudInformationTest {

    @Test
    void sessionExcludesPausesResetsOnDisconnectAndAcceptsNegativeClockOrigins() {
        var clock = new HudInformation.SessionClock();
        clock.update(-10_000_000_000L, true, false);
        clock.update(-8_000_000_000L, true, false);
        assertEquals(2, clock.seconds());
        clock.update(-7_000_000_000L, true, true);
        assertEquals(3, clock.seconds());
        clock.update(7_000_000_000L, true, true);
        assertEquals(3, clock.seconds());
        clock.update(8_000_000_000L, true, false);
        clock.update(9_000_000_000L, true, false);
        assertEquals(4, clock.seconds());
        clock.update(10_000_000_000L, false, false);
        assertEquals(0, clock.seconds());
        clock.update(20_000_000_000L, true, false);
        assertEquals(0, clock.seconds());
    }

}
