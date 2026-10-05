package com.sumirelabs.celeritasextra.compat.nothirium;

import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.config.ConfigGuiType;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Holds edits across child screens without modifying live settings until the root Done button. */
final class ExtraOptionsSession {
    private final Map<IConfigElement, Object> pending = new IdentityHashMap<>();
    private final Map<IConfigElement, Object> initial = new IdentityHashMap<>();
    private final Map<IConfigElement, List<IConfigElement>> children = new IdentityHashMap<>();

    ExtraOptionsSession(List<IConfigElement> roots) { roots.forEach(this::capture); }

    private void capture(IConfigElement element) {
        if (element.isProperty()) {
            Object value = normalized(element, element.isList() ? element.getList() : element.get());
            initial.put(element, value);
            pending.put(element, value);
        } else {
            var entries = List.copyOf(element.getChildElements());
            children.put(element, entries);
            entries.forEach(this::capture);
        }
    }

    List<IConfigElement> children(IConfigElement category) { return children.get(category); }
    Object value(IConfigElement element) {
        Object value = pending.get(element);
        return value instanceof Object[] array ? array.clone() : value;
    }
    void set(IConfigElement element, Object value) {
        pending.put(element, normalized(element, value));
    }
    private static Object normalized(IConfigElement element, Object value) {
        if (value instanceof Object[] array) return array.clone();
        // Forge ConfigElement exposes scalar values and defaults as strings, unlike DummyConfigElement.
        if (element.getType() == ConfigGuiType.INTEGER) return Integer.parseInt(value.toString());
        if (element.getType() == ConfigGuiType.BOOLEAN) return Boolean.parseBoolean(value.toString());
        if (element.getType() == ConfigGuiType.DOUBLE) return Double.parseDouble(value.toString());
        return value;
    }
    void reset(List<IConfigElement> entries) {
        for (var element : entries) {
            if (element.isProperty()) set(element, element.isList() ? element.getDefaults() : element.getDefault());
            else reset(children(element));
        }
    }
    boolean changed() {
        return pending.entrySet().stream().anyMatch(e -> !Objects.deepEquals(e.getValue(), initial.get(e.getKey())));
    }
    void commit() {
        for (var entry : pending.entrySet()) {
            if (Objects.deepEquals(entry.getValue(), initial.get(entry.getKey()))) continue;
            if (entry.getKey().isList()) entry.getKey().set(((Object[]) entry.getValue()).clone());
            else entry.getKey().set(entry.getValue());
        }
    }
}
