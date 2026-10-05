package com.sumirelabs.celeritasextra.compat.actinium;

import dhj.embeddedt.embeddium.api.OptionGUIConstructionEvent;
import dhj.embeddedt.embeddium.api.OptionGroupConstructionEvent;
import dhj.embeddedt.embeddium.api.options.OptionIdentifier;
import dhj.embeddedt.embeddium.api.options.structure.OptionPage;
import dhj.embeddedt.embeddium.api.options.structure.StandardOptions;
import com.sumirelabs.celeritasextra.compat.actinium.gui.CeleritasExtraOptionsListener;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Direct integration with Actinium alpha-0.0.12's relocated option API. */
public final class ActiniumOptionsAdapter {
    private static final Map<String, OptionIdentifier<?>> NATIVE_EQUIVALENTS = Map.of(
            "options.renderClouds", StandardOptions.Option.CLOUDS,
            "celeritasextra.option.menu_fps_limit", OptionIdentifier.create("actinium", "loading_screen_framerate_limit", int.class));
    // Scope the native-option snapshot to Extra's page construction.
    private static final ThreadLocal<Set<OptionIdentifier<?>>> NATIVE_OPTIONS = ThreadLocal.withInitial(Set::of);
    private ActiniumOptionsAdapter() { }

    public static void register() {
        OptionGUIConstructionEvent.BUS.addListener(event -> withNativeOptions(event.getPages(),
                () -> CeleritasExtraOptionsListener.onCeleritasOptionsConstruct(event)));
        // Actinium's native FULLSCREEN_MODE differs from the legacy FULLSCREEN
        // identifier, so the shared listener preserves it and enhances VSync only.
        OptionGroupConstructionEvent.BUS.addListener(event -> {
            CeleritasExtraOptionsListener.onOptionGroupConstruct(event);
            event.getOptions().removeIf(option -> option.getId() != null
                    && option.getId().getModId().equals("celeritasextra")
                    && NATIVE_EQUIVALENTS.entrySet().stream().anyMatch(entry ->
                    option.getId().getPath().equalsIgnoreCase(entry.getKey())
                            && NATIVE_OPTIONS.get().contains(entry.getValue())));
        });
    }

    static void withNativeOptions(List<OptionPage> pages, Runnable construct) {
        Set<OptionIdentifier<?>> ids = new HashSet<>();
        for (var page : pages) {
            if (!page.getId().getModId().equals("celeritasextra")) {
                for (var option : page.getOptions()) ids.add(option.getId());
            }
        }
        var previous = NATIVE_OPTIONS.get();
        NATIVE_OPTIONS.set(ids);
        try {
            construct.run();
        } finally {
            NATIVE_OPTIONS.set(previous);
        }
    }
}
