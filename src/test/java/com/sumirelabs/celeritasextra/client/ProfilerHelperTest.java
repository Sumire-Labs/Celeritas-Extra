package com.sumirelabs.celeritasextra.client;

import net.minecraft.profiler.Profiler;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProfilerHelperTest {
    static final class Renderer { }
    static final class TrackingProfiler extends Profiler {
        int starts, ends;
        String name;
        @Override public void startSection(String name) { starts++; this.name = name; }
        @Override public void endSection() { ends++; }
    }

    @Test void disabledOrUnnamedRenderersDoNotPushOrPop() {
        var profiler = new TrackingProfiler();
        boolean started = ProfilerHelper.startSection(profiler, new Renderer());
        ProfilerHelper.endSection(profiler, started);
        assertEquals(0, profiler.starts);
        assertEquals(0, profiler.ends);
        profiler.profilingEnabled = true;
        assertFalse(ProfilerHelper.startSection(profiler, new Object() { }));
        assertFalse(ProfilerHelper.startSection(profiler, null));
        assertFalse(ProfilerHelper.startSection(null, new Renderer()));
    }

    @Test void successfulSectionIsClosedWhenRenderingThrows() {
        var profiler = new TrackingProfiler();
        profiler.profilingEnabled = true;
        assertThrows(IllegalStateException.class, () -> {
            boolean started = ProfilerHelper.startSection(profiler, new Renderer());
            try { throw new IllegalStateException("renderer failure"); }
            finally { ProfilerHelper.endSection(profiler, started); }
        });
        assertEquals("Renderer", profiler.name);
        assertEquals(1, profiler.starts);
        assertEquals(1, profiler.ends);
    }
}
