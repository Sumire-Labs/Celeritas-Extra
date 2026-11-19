/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client;

import java.lang.ref.WeakReference;

/** Estimates server tick rate from time packets, independently of client frame/tick scheduling. */
public final class ServerTickRateEstimator {

    public static final ServerTickRateEstimator INSTANCE = new ServerTickRateEstimator();
    private static final long STALE_NS = 5_000_000_000L;
    private final long[] ticks = new long[5], durations = new long[5];
    private WeakReference<Object> source = new WeakReference<>(null);
    private boolean initialized;
    private long lastTick, lastTime, totalTicks, totalDuration;
    private int cursor, count;

    public synchronized void reset(Object connection) {
        if (source.get() == connection)
            clear();
    }

    private void clear() {
        initialized = false;
        totalTicks = totalDuration = 0;
        cursor = count = 0;
    }

    public synchronized void record(Object connection, long serverTick, long now) {
        if (connection == null)
            return;
        if (source.get() != connection) {
            clear();
            source = new WeakReference<>(connection);
        }
        if (initialized) {
            long deltaTicks = serverTick - lastTick;
            long elapsed = now - lastTime;
            if (deltaTicks == 0 && elapsed >= 0)
                return;
            // Custom servers may change total world time. Do not interpret a jump as ticking.
            if (deltaTicks < 0 || deltaTicks > 1200 || elapsed < 0) {
                clear();
            } else {
                if (count == ticks.length) {
                    totalTicks -= ticks[cursor];
                    totalDuration -= durations[cursor];
                } else
                    count++;
                ticks[cursor] = deltaTicks;
                durations[cursor] = elapsed;
                totalTicks += deltaTicks;
                totalDuration += elapsed;
                cursor = (cursor + 1) % ticks.length;
            }
        }
        initialized = true;
        lastTick = serverTick;
        lastTime = now;
    }

    /** NaN means that the current connection has no recent, usable interval. */
    public synchronized double estimate(Object connection, long now) {
        if (connection == null || source.get() != connection || !initialized || totalDuration <= 0
            || now - lastTime < 0 || now - lastTime > STALE_NS)
            return Double.NaN;
        return Math.min(20.0, totalTicks * 1_000_000_000.0 / totalDuration);
    }

}
