package jp.s12kuma01.celeritasextra.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FrameCounterTest {
    @BeforeEach
    void reset() {
        FrameCounter.reset();
    }

    @Test
    void firstFrameOnlySeedsTheTimerAndZeroIsAValidTimestamp() {
        FrameCounter.recordFrame(0);
        assertEquals(0, FrameCounter.getAverageFps());
        for (int i = 1; i <= 50; i++) FrameCounter.recordFrame(i * 10_000_000L);
        assertEquals(100, FrameCounter.getSmoothFps());
        assertEquals(100, FrameCounter.getAverageFps());
        assertEquals(100, FrameCounter.getOnePercentLowFps());
        assertEquals(100, FrameCounter.getPointOnePercentLowFps());
    }

    @Test
    void cacheRefreshWorksWhenMonotonicClockOriginIsNegative() {
        FrameCounter.recordFrame(-2_000_000_000L);
        FrameCounter.recordFrame(-1_500_000_000L);

        assertEquals(2, FrameCounter.getAverageFps());
        assertEquals(2, FrameCounter.getSmoothFps());
    }

    @Test
    void lowsAverageSlowestFramesAndUseCeilingForTheTailSize() {
        long now = 1_000_000_000L;
        FrameCounter.recordFrame(now);
        for (int i = 0; i < 999; i++) FrameCounter.recordFrame(now += 1_000_000L);
        FrameCounter.recordFrame(now += 10_000_000L);
        FrameCounter.recordFrame(now += 20_000_000L);
        // At 1002 samples the tails contain 11 and 2 samples, respectively.
        FrameCounter.recordFrame(now += 500_000_000L);
        assertEquals(655, FrameCounter.getAverageFps());
        assertEquals(20, FrameCounter.getOnePercentLowFps());
        assertEquals(4, FrameCounter.getPointOnePercentLowFps());
    }

    @Test
    void roundsRatherThanTruncatesAndThrottlesRefreshes() {
        FrameCounter.recordFrame(1_000_000_000L);
        FrameCounter.recordFrame(1_700_000_000L);
        assertEquals(1, FrameCounter.getAverageFps());
        FrameCounter.recordFrame(2_100_000_000L);
        assertEquals(1, FrameCounter.getAverageFps());
        FrameCounter.recordFrame(2_200_000_000L);
        assertEquals(3, FrameCounter.getAverageFps());
        // The sample ending exactly 0.5 seconds ago is included, as in Sodium Extra.
        assertEquals(3, FrameCounter.getSmoothFps());
    }

    @Test
    void currentFpsUsesHalfASecondWhileAverageUsesTheFullWindow() {
        long now = 1_000_000_000L;
        FrameCounter.recordFrame(now);
        for (int i = 0; i < 50; i++) FrameCounter.recordFrame(now += 10_000_000L);
        for (int i = 0; i < 50; i++) FrameCounter.recordFrame(now += 20_000_000L);
        assertEquals(50, FrameCounter.getSmoothFps());
        assertEquals(67, FrameCounter.getAverageFps());
    }

    @Test
    void evictsFramesOutsideFiveSecondsAfterRingBufferWraps() {
        long now = 1_000_000_000L;
        FrameCounter.recordFrame(now);
        for (int i = 0; i < 6000; i++) FrameCounter.recordFrame(now += 1_000_000L);
        for (int i = 0; i < 600; i++) FrameCounter.recordFrame(now += 10_000_000L);
        assertEquals(100, FrameCounter.getAverageFps());
        assertEquals(100, FrameCounter.getSmoothFps());
        assertEquals(100, FrameCounter.getOnePercentLowFps());
        assertEquals(100, FrameCounter.getPointOnePercentLowFps());
    }
}
