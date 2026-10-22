package com.sumirelabs.celeritasextra.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FrameCounterTest {
    @Test
    void percentileTailMatchesFullSortingAcrossBoundariesAndFrameDistributions() {
        var random = new Random(42);
        int[][] cases = {{1, 1}, {2, 1}, {99, 1}, {100, 1}, {101, 2}, {1000, 10}, {4096, 41}};
        for (int[] test : cases) {
            int size = test[0], tail = test[1];
            for (int pattern = 0; pattern < 5; pattern++) {
                // Spare capacity contains stale, very slow samples which must never enter the result.
                long[] actual = new long[size + 16];
                Arrays.fill(actual, Long.MAX_VALUE);
                for (int i = 0; i < size; i++) actual[i] = switch (pattern) {
                    case 0 -> i;
                    case 1 -> size - i;
                    case 2 -> 16_666_667;
                    case 3 -> random.nextInt(5);
                    default -> random.nextLong(1, 100_000_000);
                };
                long[] expected = Arrays.copyOf(actual, size);
                Arrays.sort(expected);
                FrameCounter.sortSlowestTail(actual, size);
                assertArrayEquals(Arrays.copyOfRange(expected, size - tail, size),
                        Arrays.copyOfRange(actual, size - tail, size), "size=" + size + ", pattern=" + pattern);
            }
        }
    }
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
