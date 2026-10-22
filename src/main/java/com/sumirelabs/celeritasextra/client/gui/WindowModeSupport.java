package com.sumirelabs.celeritasextra.client.gui;

import com.sumirelabs.celeritasextra.CeleritasExtraMod;

import java.util.Locale;

/** Gates the desktop screen-mode control without querying the native window. */
public final class WindowModeSupport {
    private WindowModeSupport() {
    }

    public static boolean canOfferBorderless() {
        return SupportHolder.AVAILABLE;
    }

    private static boolean detectSupport() {
        if (isMobileRuntime(System.getenv("POJAV_RENDERER"),
                System.getProperty("os.name", ""), System.getProperty("os.version", ""))) {
            CeleritasExtraMod.LOGGER.info("Keeping the standard fullscreen option on a mobile runtime");
            return false;
        }

        return true;
    }

    static boolean isMobileRuntime(String pojavRenderer, String osName, String osVersion) {
        // Android OpenJDK can report Linux; Zalith/Pojav also identify their renderer this way.
        return (pojavRenderer != null && !pojavRenderer.isBlank())
                || osName.toLowerCase(Locale.ROOT).contains("android")
                || osVersion.toLowerCase(Locale.ROOT).contains("android");
    }

    private static final class SupportHolder {
        // Resolve once, when the options GUI first needs the decision.
        private static final boolean AVAILABLE = detectSupport();
    }
}
