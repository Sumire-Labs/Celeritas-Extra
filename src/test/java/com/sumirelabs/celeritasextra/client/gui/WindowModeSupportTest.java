package com.sumirelabs.celeritasextra.client.gui;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

}
