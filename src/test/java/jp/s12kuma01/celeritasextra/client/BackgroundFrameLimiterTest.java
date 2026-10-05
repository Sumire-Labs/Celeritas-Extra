package jp.s12kuma01.celeritasextra.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BackgroundFrameLimiterTest {
    @Test void foregroundIsUnrestrictedAndMinimizedTakesPrecedence() {
        assertEquals(0, BackgroundFrameLimiter.selectLimit(true, true, 30, 5));
        assertEquals(30, BackgroundFrameLimiter.selectLimit(false, true, 30, 5));
        assertEquals(5, BackgroundFrameLimiter.selectLimit(false, false, 30, 5));
        assertEquals(5, BackgroundFrameLimiter.selectLimit(true, false, 30, 5));
        assertEquals(30, BackgroundFrameLimiter.selectLimit(false, false, 30, 0));
    }

    @Test void lowDrawingRatesDoNotSlowClientTicksBelowTwentyHz() {
        assertEquals(20, BackgroundFrameLimiter.loopLimit(120, 1));
        assertEquals(30, BackgroundFrameLimiter.loopLimit(120, 30));
        assertEquals(30, BackgroundFrameLimiter.loopLimit(30, 60));
        assertEquals(120, BackgroundFrameLimiter.loopLimit(120, 0));
    }

    @Test void lowFpsDrawsAtRequestedIntervalsAndFocusReturnsImmediately() {
        var schedule = new BackgroundFrameLimiter.FrameSchedule();
        assertTrue(schedule.allow(0, 5));
        assertFalse(schedule.allow(50_000_000, 5));
        assertFalse(schedule.allow(199_999_999, 5));
        assertTrue(schedule.allow(200_000_000, 5));
        assertTrue(schedule.allow(210_000_000, 0));
        assertTrue(schedule.allow(220_000_000, 0));
        assertTrue(schedule.allow(230_000_000, 1));
        assertFalse(schedule.allow(240_000_000, 1));
    }
}
