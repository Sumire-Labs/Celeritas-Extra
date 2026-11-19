/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import java.util.HashSet;
import java.util.Set;

/** View state only: collapsing never removes an option from Apply or Undo. */
public final class OptionsCollapseState {

    private final Set<String> collapsedMods = new HashSet<>();
    private final java.util.Map<String, Boolean> collapsedGroups = new java.util.HashMap<>();
    public boolean groupCollapsed(OptionSections.Section section) {
        return section.collapsible() && collapsedGroups.getOrDefault(section.id(), section.initiallyCollapsed());
    }
    public void toggleGroup(OptionSections.Section section) {
        collapsedGroups.put(section.id(), !groupCollapsed(section));
    }
    private String selectedTabId;
    public String selectedTabId() {
        return selectedTabId;
    }
    public void selectTab(String id) {
        selectedTabId = id;
    }

    public boolean modCollapsed(String id, boolean searching) {
        return !searching && collapsedMods.contains(id);
    }
    public void toggleMod(String id) {
        toggle(collapsedMods, id);
    }
    private static void toggle(Set<String> set, String id) {
        if (!set.remove(id))
            set.add(id);
    }

}
