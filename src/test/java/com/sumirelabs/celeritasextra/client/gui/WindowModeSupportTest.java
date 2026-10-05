package com.sumirelabs.celeritasextra.client.gui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.IntBuffer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WindowModeSupportTest {
    @ParameterizedTest
    @CsvSource({
            "opengles3, Linux, 6.1.0",
            ", Linux, Android-16",
            ", Android, 16",
            ", Linux, 6.1.0-android14"
    })
    void recognizesMobileLaunchersAndAndroidOpenJdk(String renderer, String osName, String osVersion) {
        assertTrue(WindowModeSupport.isMobileRuntime(renderer, osName, osVersion));
    }

    @ParameterizedTest
    @CsvSource({
            ", Linux, 6.1.0",
            "'', Linux, 6.1.0",
            ", Windows 11, 10.0",
            ", Mac OS X, 15.0"
    })
    void keepsDesktopRuntimesEligible(String renderer, String osName, String osVersion) {
        assertFalse(WindowModeSupport.isMobileRuntime(renderer, osName, osVersion));
    }

    @Test
    void signatureProbeDoesNotInitializeGlfwOrCallWindowApis() {
        assertTrue(WindowModeSupport.hasRequiredGlfwMethods(DesktopGlfw.class));
    }

    @Test
    void rejectsArrayOnlyShimThatWouldCauseTheReportedLinkageError() {
        assertFalse(WindowModeSupport.hasRequiredGlfwMethods(ArrayOnlyGlfw.class));
    }

    @Test
    void alsoRequiresTheWindowSizeBufferOverload() {
        assertFalse(WindowModeSupport.hasRequiredGlfwMethods(PositionOnlyGlfw.class));
    }

    public static class DesktopGlfw {
        static {
            failIfCalled();
        }

        public static void glfwGetWindowPos(long window, IntBuffer x, IntBuffer y) {
            failIfCalled();
        }

        public static void glfwGetWindowSize(long window, IntBuffer width, IntBuffer height) {
            failIfCalled();
        }

        private static void failIfCalled() {
            throw new AssertionError("The compatibility probe must not initialize or invoke GLFW");
        }
    }

    public static class ArrayOnlyGlfw {
        public static void glfwGetWindowPos(long window, int[] x, int[] y) {
        }

        public static void glfwGetWindowSize(long window, int[] width, int[] height) {
        }
    }

    public static class PositionOnlyGlfw {
        public static void glfwGetWindowPos(long window, IntBuffer x, IntBuffer y) {
        }
    }
}
