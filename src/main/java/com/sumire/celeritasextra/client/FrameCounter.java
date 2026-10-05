package com.sumire.celeritasextra.client;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;

/**
 * Tracks per-frame timing data over a rolling 5-second window
 * and computes average FPS, 1% low, and 0.1% low percentile metrics.
 * Ported from Sodium Extra's FrameCounter.
 */
@Mod.EventBusSubscriber(Side.CLIENT)
@SideOnly(Side.CLIENT)
public class FrameCounter {

    private static final long WINDOW_NS = 5_000_000_000L;
    private static final long CACHE_INTERVAL_NS = 500_000_000L;

    private static long[] sampleTimes = new long[512];
    private static long[] sampleDeltas = new long[512];
    private static int sampleHead;
    private static int sampleCount;

    private static long lastFrameTime;
    private static boolean hasLastFrameTime;
    private static long lastCacheTime;
    private static boolean hasCacheTime;

    private static int cachedAverageFps = 0;
    private static int cachedSmoothFps = 0;
    private static int cachedOnePercentLowFps = 0;
    private static int cachedPointOnePercentLowFps = 0;

    /**
     * Samples the current frame's duration at the start of each render tick, evicts samples older than
     * the 5-second window, and refreshes the cached FPS statistics at most once every 500ms.
     * <p>
     * Only the {@link TickEvent.Phase#START} phase is processed; the first observed frame seeds the
     * timer without producing a sample.
     *
     * @param event the render-tick event carrying the current {@link TickEvent.Phase}
     */
    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !BackgroundFrameLimiter.shouldRender()) {
            return;
        }
        recordFrame(System.nanoTime());
    }

    // Explicit time input keeps rolling-window behavior testable without sleeping.
    static void recordFrame(long now) {

        if (hasLastFrameTime) {
            long delta = now - lastFrameTime;
            if (delta > 0) {
                addSample(now, delta);
            }
        }
        lastFrameTime = now;
        hasLastFrameTime = true;

        // Evict samples older than 5 seconds
        while (sampleCount > 0 && now - sampleTimes[sampleHead] > WINDOW_NS) {
            sampleHead = (sampleHead + 1) % sampleTimes.length;
            sampleCount--;
        }

        // Recalculate cached stats every 500ms
        if (!hasCacheTime || now - lastCacheTime >= CACHE_INTERVAL_NS) {
            lastCacheTime = now;
            hasCacheTime = true;
            recalculate(now);
        }
    }

    /**
     * Recomputes the cached average FPS and the 1% / 0.1% low metrics from the current sample window,
     * including the 0.5-second smooth FPS, resetting all metrics when the window holds no samples.
     */
    private static void recalculate(long now) {
        int size = sampleCount;
        if (size == 0) {
            cachedAverageFps = 0;
            cachedSmoothFps = 0;
            cachedOnePercentLowFps = 0;
            cachedPointOnePercentLowFps = 0;
            return;
        }

        long[] deltas = new long[size];
        long totalDelta = 0;
        long recentDelta = 0;
        int recentCount = 0;
        for (int i = 0; i < size; i++) {
            long delta = sampleDeltas[(sampleHead + i) % sampleDeltas.length];
            deltas[i] = delta;
            totalDelta += delta;
            if (now - sampleTimes[(sampleHead + i) % sampleTimes.length] <= CACHE_INTERVAL_NS) {
                recentDelta += delta;
                recentCount++;
            }
        }

        Arrays.sort(deltas);
        double avgDelta = (double) totalDelta / size;
        cachedAverageFps = avgDelta > 0 ? (int) Math.round(1_000_000_000.0 / avgDelta) : 0;
        cachedSmoothFps = recentCount > 0 ? (int) Math.round(recentCount * 1_000_000_000.0 / recentDelta) : 0;
        cachedOnePercentLowFps = computePercentileLow(deltas, 1.0);
        cachedPointOnePercentLowFps = computePercentileLow(deltas, 0.1);
    }

    private static void addSample(long time, long delta) {
        if (sampleCount == sampleTimes.length) {
            int newCapacity = sampleTimes.length * 2;
            long[] newTimes = new long[newCapacity];
            long[] newDeltas = new long[newCapacity];
            for (int i = 0; i < sampleCount; i++) {
                int oldIndex = (sampleHead + i) % sampleTimes.length;
                newTimes[i] = sampleTimes[oldIndex];
                newDeltas[i] = sampleDeltas[oldIndex];
            }
            sampleTimes = newTimes;
            sampleDeltas = newDeltas;
            sampleHead = 0;
        }

        int tail = (sampleHead + sampleCount) % sampleTimes.length;
        sampleTimes[tail] = time;
        sampleDeltas[tail] = delta;
        sampleCount++;
    }

    /**
     * Computes the percentile low FPS by averaging the slowest N% of frame deltas.
     * This matches the standard gaming benchmark definition of "1% low" / "0.1% low".
     */
    private static int computePercentileLow(long[] deltas, double percent) {
        int count = (int) Math.ceil(deltas.length * (percent / 100.0));
        if (count == 0) count = 1;

        // Sorted ascending — take the last `count` entries (slowest frames)
        long sum = 0;
        for (int i = deltas.length - count; i < deltas.length; i++) {
            sum += deltas[i];
        }

        double avgDelta = (double) sum / count;
        return avgDelta > 0 ? (int) Math.round(1_000_000_000.0 / avgDelta) : 0;
    }

    /** Current FPS smoothed over 0.5 seconds, matching Sodium Extra. */
    public static int getSmoothFps() {
        return cachedSmoothFps;
    }

    /** Start a fresh timing window. Render thread only. */
    static void reset() {
        sampleHead = sampleCount = 0;
        hasLastFrameTime = false;
        hasCacheTime = false;
        cachedSmoothFps = cachedAverageFps = cachedOnePercentLowFps = cachedPointOnePercentLowFps = 0;
    }

    /**
     * Returns the most recently cached average FPS over the rolling 5-second window.
     */
    public static int getAverageFps() {
        return cachedAverageFps;
    }

    /**
     * Returns the most recently cached 1% low FPS (average of the slowest 1% of frames in the window).
     */
    public static int getOnePercentLowFps() {
        return cachedOnePercentLowFps;
    }

    /**
     * Returns the most recently cached 0.1% low FPS (average of the slowest 0.1% of frames in the window).
     */
    public static int getPointOnePercentLowFps() {
        return cachedPointOnePercentLowFps;
    }
}
