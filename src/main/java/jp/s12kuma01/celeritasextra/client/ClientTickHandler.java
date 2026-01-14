package jp.s12kuma01.celeritasextra.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Handles client tick events for FPS tracking
 * Tracks highest, average, and lowest FPS over a rolling window
 */
@Mod.EventBusSubscriber(Side.CLIENT)
@SideOnly(Side.CLIENT)
public class ClientTickHandler {

    private static final int QUEUE_SIZE = 200;
    private static final Deque<Integer> fpsQueue = new ArrayDeque<>(QUEUE_SIZE);

    private static int averageFps = 0;
    private static int lowestFps = 0;
    private static int highestFps = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        int currentFPS = Minecraft.getDebugFPS();

        // Add current FPS to queue
        if (fpsQueue.size() >= QUEUE_SIZE) {
            fpsQueue.pollFirst();
        }
        fpsQueue.addLast(currentFPS);

        // Calculate statistics
        if (!fpsQueue.isEmpty()) {
            int sum = 0;
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;

            for (int fps : fpsQueue) {
                sum += fps;
                if (fps < min) min = fps;
                if (fps > max) max = fps;
            }

            averageFps = sum / fpsQueue.size();
            lowestFps = min;
            highestFps = max;
        }
    }

    public static int getAverageFps() {
        return averageFps;
    }

    public static int getLowestFps() {
        return lowestFps;
    }

    public static int getHighestFps() {
        return highestFps;
    }
}
