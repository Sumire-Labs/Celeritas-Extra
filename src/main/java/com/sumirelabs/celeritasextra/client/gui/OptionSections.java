/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.sumirelabs.celeritasextra.client.gui;

import java.util.Map;
import java.util.WeakHashMap;

import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.taumc.celeritas.api.options.structure.OptionGroup;

/** Extra-only presentation metadata; native/add-on groups without metadata keep their original layout. */
public final class OptionSections {

    public record Section(String id, TextComponent title, boolean collapsible, boolean initiallyCollapsed,
        Section parent) {
    }
    private static final Map<OptionGroup, Section> SECTIONS = new WeakHashMap<>();
    private static final Map<org.taumc.celeritas.api.options.structure.Option<?>, Section> OPTIONS = new WeakHashMap<>();
    public static final Section COLORS = section("hud/colors", "celeritasextra.gui.hud_colors", true, true, null);
    public static final Section DIMENSIONS = section("weather/dimensions", "celeritasextra.option.page.dimension_fog",
        true, true, null);
    public static final Section PARTICLES = section("particles/classes", "celeritasextra.gui.particle_classes", true,
        true, null);
    private OptionSections() {}
    public static Section section(String id, String key, boolean collapsible, boolean closed, Section parent) {
        return new Section(id, TextComponent.translatable(key), collapsible, closed, parent);
    }
    public static OptionGroup annotate(OptionGroup group, Section section) {
        if (section != null) {
            SECTIONS.put(group, section);
            for (var option : group.getOptions())
                OPTIONS.put(option, section);
        }
        return group;
    }
    public static String searchTerms(org.taumc.celeritas.api.options.structure.Option<?> option,
        org.embeddedt.embeddium.impl.gui.framework.FontMetricsProvider font) {
        var text = new StringBuilder();
        for (var section = OPTIONS.get(option); section != null; section = section.parent())
            text.append(' ').append(font.extractString(section.title()));
        return text.toString();
    }
    public static Section of(OptionGroup group) {
        return SECTIONS.get(group);
    }
    public static Section forId(String id) {
        String key = switch (id) {
            case "groups/overlay/0" -> "celeritasextra.group.fps";
            case "groups/overlay/1" -> "celeritasextra.group.coordinates";
            case "groups/overlay/memory" -> "celeritasextra.group.memory";
            case "groups/overlay/information" -> "celeritasextra.group.time";
            case "groups/overlay/world" -> "celeritasextra.group.world";
            case "groups/overlay/2" -> "celeritasextra.group.appearance";
            case "groups/overlay/3" -> "celeritasextra.group.debug";
            default -> null;
        };
        if (key != null)
            return section(id, key, false, false, null);
        String colors = "groups/overlay/colors/";
        if (id.startsWith(colors)) {
            var line = CeleritasExtraGameOptions.HudLine
                .valueOf(id.substring(colors.length()).toUpperCase(java.util.Locale.ROOT));
            return new Section(id, TextComponent.translatable("celeritasextra.group.color",
                TextComponent.translatable(line.colorLabelKey())), true, true, COLORS);
        }
        String particles = "groups/particle/mod/";
        if (id.startsWith(particles))
            return new Section(id, TextComponent.literal(id.substring(particles.length())), true, true, PARTICLES);
        return null;
    }

}
