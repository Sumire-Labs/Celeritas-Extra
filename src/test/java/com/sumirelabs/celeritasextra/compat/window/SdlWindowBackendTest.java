package com.sumirelabs.celeritasextra.compat.window;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.IntBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class SdlWindowBackendTest {
    private SdlWindowBackend backend;
    private boolean borderlessPreference;

    @BeforeEach void setup() throws Exception {
        FakeSdl.current = new FakeWindow();
        FakeVideo.interval = 1;
        FakeVideo.adaptive = true;
        FakeVideo.querySucceeds = true;
        FakeVideo.writes = 0;
        backend = new SdlWindowBackend(FakeSdl.class, FakeWindow.class, FakeVideo.class, () -> borderlessPreference);
    }

    @Test void visibilityUsesActualMinimizeAndHideEvents() {
        assertTrue(backend.visible());
        FakeSdl.current.minimized = true;
        assertFalse(backend.visible());
        FakeSdl.current.minimized = false;
        FakeSdl.current.hidden = true;
        assertFalse(backend.visible());
        FakeSdl.current.hidden = false;
        assertTrue(backend.visible());
    }

    @Test void borderlessUsesSdlWithoutTheRemovedDisplayMethod() {
        assertFalse(backend.borderless());
        FakeSdl.current.fullscreen = true;
        borderlessPreference = true;
        assertTrue(backend.borderless());
        borderlessPreference = false;
        assertFalse(backend.borderless());
        borderlessPreference = true;
        FakeSdl.current.fullscreen = false;
        assertFalse(backend.borderless(), "A windowed game retains the preference but is not borderless fullscreen");
        FakeSdl.current = null;
        assertFalse(backend.borderless());
        assertFalse(backend.visible());
    }

    @ParameterizedTest @ValueSource(ints = {0, 1, -1})
    void capabilityProbeRestoresTheOriginalVsyncMode(int previous) {
        FakeVideo.interval = previous;
        assertTrue(backend.adaptiveSupported());
        assertEquals(previous, FakeVideo.interval);
        assertEquals(2, FakeVideo.writes);
    }

    @Test void unsupportedAdaptiveModeDoesNotChangeTheInterval() {
        FakeVideo.adaptive = false;
        assertFalse(backend.adaptiveSupported());
        assertEquals(1, FakeVideo.interval);
        assertFalse(backend.enableAdaptive());
        assertEquals(1, FakeVideo.interval);
    }

    @Test void failedQueryNeverAttemptsToChangeVsync() {
        FakeVideo.querySucceeds = false;
        assertFalse(backend.adaptiveSupported());
        assertEquals(0, FakeVideo.writes);
    }

    @Test void enablingAdaptiveKeepsNegativeSwapInterval() {
        assertTrue(backend.enableAdaptive());
        assertEquals(-1, FakeVideo.interval);
    }

    public static final class FakeSdl {
        static FakeWindow current;
        public static FakeWindow window() { return current; }
    }

    public static final class FakeWindow {
        boolean fullscreen, minimized, hidden;
        public boolean fullscreen() { return fullscreen; }
        public boolean minimized() { return minimized; }
        public boolean hidden() { return hidden; }
    }

    public static final class FakeVideo {
        static int interval, writes;
        static boolean adaptive, querySucceeds;
        public static boolean SDL_GL_GetSwapInterval(IntBuffer result) {
            if (!querySucceeds) return false;
            result.put(0, interval);
            return true;
        }
        public static boolean SDL_GL_SetSwapInterval(int value) {
            writes++;
            if (value == -1 && !adaptive) return false;
            interval = value;
            return true;
        }
    }
}
