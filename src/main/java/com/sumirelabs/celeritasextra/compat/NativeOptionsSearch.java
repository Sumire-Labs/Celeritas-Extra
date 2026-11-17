/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.taumc.celeritas.api.options.structure.Option;

/** Bridges the optional newer search model without linking classes absent from older Celeritas. */
public final class NativeOptionsSearch {

    private NativeOptionsSearch() {}

    private record Access(Field model, Method predicate, Field text, Field focused, Field filter, Field[] cursors) {
    }

    private static final ClassValue<Access> ACCESS = new ClassValue<>() {

        @Override
        protected Access computeValue(Class<?> type) {
            for (Class<?> current = type; current != null; current = current.getSuperclass()) {
                Field model;
                try {
                    model = current.getDeclaredField("searchTextModel");
                } catch (NoSuchFieldException absent) {
                    continue;
                }
                try {
                    model.setAccessible(true);
                    Class<?> modelType = model.getType();
                    var cursors = new Field[]{field(modelType, "selectionStart"), field(modelType, "selectionEnd"),
                        field(modelType, "firstCharacterIndex"), field(modelType, "lastCursorPosition")};
                    return new Access(model, modelType.getMethod("getOptionPredicate"), field(modelType, "text"),
                        field(modelType, "focused"), field(modelType, "optionPredicate"), cursors);
                } catch (ReflectiveOperationException exception) {
                    throw new IllegalStateException("Unsupported Celeritas native search model", exception);
                }
            }
            return null;
        }

    };

    private static Field field(Class<?> type, String name) throws NoSuchFieldException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    public static boolean available(Object controller) {
        return ACCESS.get(controller.getClass()) != null;
    }

    @SuppressWarnings("unchecked")
    public static Predicate<Option<?>> predicate(Object controller) {
        Access access = ACCESS.get(controller.getClass());
        if (access == null)
            return option -> true;
        try {
            return (Predicate<Option<?>>) access.predicate().invoke(access.model().get(controller));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static boolean hasQuery(Object controller) {
        Access access = ACCESS.get(controller.getClass());
        if (access == null)
            return false;
        try {
            return !((String) access.text().get(access.model().get(controller))).isBlank();
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static void clear(Object controller) {
        Access access = ACCESS.get(controller.getClass());
        if (access == null)
            return;
        try {
            Object model = access.model().get(controller);
            access.text().set(model, "");
            access.focused().setBoolean(model, false);
            access.filter().set(model, (Predicate<Option<?>>) option -> true);
            for (Field cursor : access.cursors())
                cursor.setInt(model, 0);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }

}
