package com.sumire.celeritasextra.client.gui;

import java.util.HashSet;
import java.util.Set;

/** View state only: collapsing never removes an option from Apply or Undo. */
public final class OptionsCollapseState {
    private final Set<String> collapsedMods = new HashSet<>();

    public boolean modCollapsed(String id, boolean searching) { return !searching && collapsedMods.contains(id); }
    public void toggleMod(String id) { toggle(collapsedMods, id); }
    private static void toggle(Set<String> set, String id) { if (!set.remove(id)) set.add(id); }
}
