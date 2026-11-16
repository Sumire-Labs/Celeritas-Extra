/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.compat.pintonium;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.taumc.celeritas.api.options.structure.OptionStorage;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;

import static org.junit.jupiter.api.Assertions.*;

class PintoniumGuiIntegrationTest {

    @BeforeAll
    static void initializeTranslations() throws Exception {
        var field = net.minecraft.client.resources.I18n.class.getDeclaredField("i18nLocale");
        field.setAccessible(true);
        field.set(null, new net.minecraft.client.resources.Locale());
    }

    private final AtomicInteger saves = new AtomicInteger();
    private final OptionStorage<Object> storage = new OptionStorage<>() {

        @Override
        public Object getData() {
            return this;
        }
        @Override
        public void save() {
            saves.incrementAndGet();
        }

    };

    @Test
    void editsDefaultsAndUndoFollowNativeApplyAcrossRepeatedSaves() {
        var property = new Property("distance", "8", Property.Type.INTEGER);
        property.setDefaultValue("0");
        property.setMinValue(0).setMaxValue(32);
        var option = new PintoniumOption(new ConfigElement(property), "", "fog/distance", storage);
        assertEquals(8, option.getValue());
        option.setValue(20);
        assertTrue(option.hasChanged());
        assertEquals(8, property.getInt());
        option.reset();
        assertEquals(8, option.getValue());
        assertFalse(option.hasChanged());
        option.resetToDefault();
        assertEquals(0, option.getValue());
        assertFalse(option.canReset());
        assertEquals(8, property.getInt());
        option.applyChanges();
        option.getStorage().save();
        assertFalse(option.hasChanged());
        assertEquals(0, property.getInt());
        option.setValue(12);
        option.reset();
        assertEquals(0, option.getValue());
        option.setValue(7);
        option.applyChanges();
        option.getStorage().save();
        option.setValue(10);
        option.reset();
        assertEquals(7, option.getValue());
        assertEquals(2, saves.get());
    }

    @Test
    void listEditsDoNotLeakAndDefaultsRemainIndependent() {
        var property = new Property("exemptions", new String[]{"original"}, Property.Type.STRING);
        property.setDefaultValues(new String[0]);
        var option = new PintoniumOption(new ConfigElement(property), "Entities", "entities/exemptions", storage);
        Object[] value = (Object[]) option.getValue();
        value[0] = "leaked";
        assertArrayEquals(new String[]{"original"}, (Object[]) option.getValue());
        String[] selected = {"changed"};
        option.setValue(selected);
        selected[0] = "leaked";
        assertArrayEquals(new String[]{"changed"}, (Object[]) option.getValue());
        assertArrayEquals(new String[]{"original"}, property.getStringList());
        option.resetToDefault();
        assertFalse(option.canReset());
        assertTrue(option.hasChanged());
        option.reset();
        assertArrayEquals(new String[]{"original"}, (Object[]) option.getValue());
        option.setValue(new String[]{"applied"});
        option.applyChanges();
        assertArrayEquals(new String[]{"applied"}, property.getStringList());
        assertFalse(option.hasChanged());
    }

    @Test
    void nestedSettingsBecomeNativeSearchableOptionsWithDistinctIdentifiers() {
        var toggle = new ConfigElement(new Property("sky", "true", Property.Type.BOOLEAN));
        var channel = new ConfigElement(
            new Property("red", "255", Property.Type.INTEGER).setMinValue(0).setMaxValue(255));
        var category = new DummyCategoryElement("overlay", "", List.of(toggle,
            new DummyCategoryElement("FPS", "", List.of(channel)),
            new DummyCategoryElement("Coordinates", "", List.of(channel))));
        var page = PintoniumGuiIntegration.page(category, storage);
        assertEquals("celeritasextra", page.getId().getModId());
        assertEquals(3, page.getOptions().size());
        assertEquals(3, page.getOptions().stream().map(option -> option.getId().getPath()).distinct().count());
        assertEquals("FPS · red", page.getOptions().get(1).getName().getUnformattedText());
        assertEquals("Coordinates · red", page.getOptions().get(2).getName().getUnformattedText());
        assertTrue(page.getOptions().stream().allMatch(option -> option.getStorage() == storage));
    }

    @Test
    void testsUsePintoniumsIndependentGuiInsteadOfTheCeleritasSdk() throws Exception {
        assertNotNull(Class.forName("org.taumc.celeritas.impl.gui.compat.GuiGraphics"));
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("org.embeddedt.embeddium.impl.gui.CeleritasVideoOptionsController"));
    }

}
