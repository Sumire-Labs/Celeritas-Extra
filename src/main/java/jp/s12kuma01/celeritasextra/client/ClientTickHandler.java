package jp.s12kuma01.celeritasextra.client;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * Tracks per-frame timing data over a rolling 5-second window
 * and computes average FPS, 1% low, and 0.1% low percentile metrics.
 * Ported from Sodium Extra's FrameCounter.
 */
@Mod.EventBusSubscriber(Side.CLIENT)
@SideOnly(Side.CLIENT)
public class ClientTickHandler {

    private static final long WINDOW_NS = 5_000_000_000L;
    private static final long CACHE_INTERVAL_NS = 500_000_000L;

    private static final ArrayDeque<long[]> frameSamples = new ArrayDeque<>();

    private static long lastFrameTime = 0;
    private static long lastCacheTime = 0;

    private static int cachedAverageFps = 0;
    private static int cachedOnePercentLowFps = 0;
    private static int cachedPointOnePercentLowFps = 0;

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        long now = System.nanoTime();

        if (lastFrameTime != 0) {
            long delta = now - lastFrameTime;
            if (delta > 0) {
                frameSamples.addLast(new long[]{now, delta});
            }
        }
        lastFrameTime = now;

        // Evict samples older than 5 seconds
        while (!frameSamples.isEmpty() && (now - frameSamples.peekFirst()[0]) > WINDOW_NS) {
            frameSamples.pollFirst();
        }

        // Recalculate cached stats every 500ms
        if (now - lastCacheTime >= CACHE_INTERVAL_NS) {
            lastCacheTime = now;
            recalculate();
        }
    }

    private static void recalculate() {
        int size = frameSamples.size();
        if (size == 0) {
            cachedAverageFps = 0;
            cachedOnePercentLowFps = 0;
            cachedPointOnePercentLowFps = 0;
            return;
        }

        long[] deltas = new long[size];
        int i = 0;
        long totalDelta = 0;
        for (var sample : frameSamples) {
            deltas[i] = sample[1];
            totalDelta += sample[1];
            i++;
        }

        double avgDelta = (double) totalDelta / size;
        cachedAverageFps = avgDelta > 0 ? (int) (1_000_000_000.0 / avgDelta) : 0;
        cachedOnePercentLowFps = computePercentileLow(deltas, 1.0);
        cachedPointOnePercentLowFps = computePercentileLow(deltas, 0.1);
    }

    /**
     * Computes the percentile low FPS by averaging the slowest N% of frame deltas.
     * This matches the standard gaming benchmark definition of "1% low" / "0.1% low".
     */
    private static int computePercentileLow(long[] deltas, double percent) {
        int count = (int) Math.ceil(deltas.length * (percent / 100.0));
        if (count == 0) count = 1;

        long[] sorted = deltas.clone();
        Arrays.sort(sorted);

        // Sorted ascending — take the last `count` entries (slowest frames)
        long sum = 0;
        for (int i = sorted.length - count; i < sorted.length; i++) {
            sum += sorted[i];
        }

        double avgDelta = (double) sum / count;
        return avgDelta > 0 ? (int) (1_000_000_000.0 / avgDelta) : 0;
    }

    public static int getAverageFps() {
        return cachedAverageFps;
    }

    public static int getOnePercentLowFps() {
        return cachedOnePercentLowFps;
    }

    public static int getPointOnePercentLowFps() {
        return cachedPointOnePercentLowFps;
    }
}
