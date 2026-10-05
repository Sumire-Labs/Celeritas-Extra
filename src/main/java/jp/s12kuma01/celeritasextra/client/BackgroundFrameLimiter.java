package jp.s12kuma01.celeritasextra.client;

import org.lwjgl.opengl.Display;
import net.minecraft.client.Minecraft;

/** Limits drawing without dropping the 20 Hz client tick/network processing cadence. */
public final class BackgroundFrameLimiter {
    private static final FrameSchedule SCHEDULE = new FrameSchedule();
    private static boolean renderFrame = true;

    private BackgroundFrameLimiter() {}

    public static int currentLimit() {
        var settings = CeleritasExtraClientMod.options().extraSettings;
        if ((settings.inactiveFpsLimit == 0 && settings.minimizedFpsLimit == 0) || !Display.isCreated()) return 0;
        int requested = selectLimit(Display.isActive(), Display.isVisible(), settings.inactiveFpsLimit, settings.minimizedFpsLimit);
        Minecraft mc = Minecraft.getMinecraft();
        int normal = mc.world == null && mc.currentScreen != null ? 30 : mc.gameSettings.limitFramerate;
        return requested > 0 ? Math.min(normal, requested) : 0;
    }

    public static int selectLimit(boolean active, boolean visible, int inactive, int minimized) {
        // Minimized takes precedence. A disabled minimized limit inherits the unfocused limit.
        return !visible && minimized > 0 ? minimized : !active || !visible ? inactive : 0;
    }

    public static int loopLimit(int vanilla, int requested) {
        return requested > 0 ? Math.max(20, Math.min(vanilla, requested)) : vanilla;
    }

    public static void beginFrame(long now) {
        renderFrame = SCHEDULE.allow(now, currentLimit());
    }

    public static boolean shouldRender() { return renderFrame; }

    public static final class FrameSchedule {
        private long lastDraw = -1;
        private int previousLimit;

        public boolean allow(long now, int limit) {
            boolean changed = limit != previousLimit;
            previousLimit = limit;
            if (limit == 0 || limit >= 20 || changed || lastDraw == -1 || now - lastDraw >= 1_000_000_000L / limit) {
                lastDraw = now;
                return true;
            }
            return false;
        }
    }
}
