/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.compat.pintonium;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.taumc.celeritas.api.options.OptionIdentifier;
import org.taumc.celeritas.api.options.control.Control;
import org.taumc.celeritas.api.options.structure.*;

import com.sumirelabs.celeritasextra.client.gui.Translations;
import com.sumirelabs.celeritasextra.compat.nothirium.VanillaExtraOptionsScreen;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.config.IConfigElement;

/** Keeps edits local to Pintonium's Apply/Undo lifecycle, including list values. */
final class PintoniumOption implements Option<Object> {

    final IConfigElement element;
    private final OptionIdentifier<Object> id;
    private final ITextComponent name, tooltip;
    private final OptionStorage<?> storage;
    private final Control<Object> control;
    private Object pending;

    PintoniumOption(IConfigElement element, String context, String path, OptionStorage<?> storage) {
        this.element = element;
        this.id = OptionIdentifier.create("celeritasextra", path, Object.class);
        this.storage = storage;
        String label = VanillaExtraOptionsScreen.label(element);
        name = new TextComponentString(context.isEmpty() ? label : context + " · " + label);
        String key = element.getLanguageKey();
        String tooltipKey = key == null || key.isEmpty() ? "" : Translations.tooltipKey(key);
        String comment = I18n.hasKey(tooltipKey) ? Translations.format(tooltipKey) : element.getComment();
        tooltip = new TextComponentString(comment == null ? "" : comment);
        reset();
        control = new PintoniumControl(this);
    }

    static Object normalized(IConfigElement element, Object value) {
        if (value instanceof Object[] array)
            return array.clone();
        return switch (element.getType()) {
            case BOOLEAN -> Boolean.parseBoolean(value.toString());
            case INTEGER -> Integer.parseInt(value.toString());
            case DOUBLE -> Double.parseDouble(value.toString());
            default -> value.toString();
        };
    }
    private Object stored() {
        return normalized(element, element.isList() ? element.getList() : element.get());
    }
    Object defaultValue() {
        return normalized(element, element.isList() ? element.getDefaults() : element.getDefault());
    }
    boolean canReset() {
        return isAvailable() && !Objects.deepEquals(pending, defaultValue());
    }
    void resetToDefault() {
        setValue(defaultValue());
    }

    @Override
    public OptionIdentifier<Object> getId() {
        return id;
    }
    @Override
    public ITextComponent getName() {
        return name;
    }
    @Override
    public ITextComponent getTooltip() {
        return tooltip;
    }
    @Override
    public OptionImpact getImpact() {
        return null;
    }
    @Override
    public Control<Object> getControl() {
        return control;
    }
    @Override
    public Object getValue() {
        return pending instanceof Object[] array ? array.clone() : pending;
    }
    @Override
    public void setValue(Object value) {
        pending = normalized(element, value);
    }
    @Override
    public void reset() {
        pending = stored();
    }
    @Override
    public OptionStorage<?> getStorage() {
        return storage;
    }
    @Override
    public boolean isAvailable() {
        return element.showInGui();
    }
    @Override
    public boolean hasChanged() {
        return !Objects.deepEquals(pending, stored());
    }
    @Override
    public void applyChanges() {
        if (element.isList())
            element.set(((Object[]) pending).clone());
        else
            element.set(pending);
    }
    @Override
    public Collection<OptionFlag> getFlags() {
        return List.of();
    }

}
