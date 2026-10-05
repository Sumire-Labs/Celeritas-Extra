package jp.s12kuma01.celeritasextra.client.gui;

import java.util.HashSet;
import java.util.Set;

/** View state only: collapsing never removes an option from Apply or Undo. */
public final class OptionsCollapseState {
    private final Set<String> collapsedMods = new HashSet<>();
    private final Set<String> collapsedGroups = new HashSet<>();

    public boolean modCollapsed(String id, boolean searching) { return !searching && collapsedMods.contains(id); }
    public boolean groupCollapsed(String id, boolean searching) { return !searching && collapsedGroups.contains(id); }
    public void toggleMod(String id) { toggle(collapsedMods, id); }
    public void toggleGroup(String id) { toggle(collapsedGroups, id); }
    private static void toggle(Set<String> set, String id) { if (!set.remove(id)) set.add(id); }
}
