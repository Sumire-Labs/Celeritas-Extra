package jp.s12kuma01.celeritasextra.client.gui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Localized group metadata shared by the two renderer API adapters. */
public final class OptionGroupLabels {
    private static final Map<String, String> LABELS = new ConcurrentHashMap<>();
    private OptionGroupLabels() { }

    public static void register(String id, String label) { LABELS.put(id, label); }
    public static String get(String id) { return LABELS.get(id); }
}
