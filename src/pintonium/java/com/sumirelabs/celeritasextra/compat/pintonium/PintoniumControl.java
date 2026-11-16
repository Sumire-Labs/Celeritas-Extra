/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.compat.pintonium;

import java.util.Arrays;
import java.util.Locale;

import org.taumc.celeritas.api.options.control.*;
import org.taumc.celeritas.api.options.structure.Option;
import org.taumc.celeritas.impl.gui.compat.GuiGraphics;
import org.taumc.celeritas.impl.util.ComponentUtil;
import org.taumc.celeritas.impl.util.Dim2i;

import com.sumirelabs.celeritasextra.compat.nothirium.VanillaExtraTextScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.ConfigGuiType;

/** Uses Pintonium's own checkboxes and sliders, with a small choice/list control for Forge values. */
final class PintoniumControl implements Control<Object> {

    private final PintoniumOption option;
    private final Control<Object> delegate;

    PintoniumControl(PintoniumOption option) {
        this.option = option;
        this.delegate = nativeControl(option);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Control<Object> nativeControl(PintoniumOption option) {
        var element = option.element;
        if (!element.isList() && element.getType() == ConfigGuiType.BOOLEAN)
            return (Control) new TickBoxControl((Option) option);
        if (!element.isList() && element.getType() == ConfigGuiType.INTEGER) {
            int min = Integer.parseInt(element.getMinValue().toString());
            int max = Integer.parseInt(element.getMaxValue().toString());
            if (max > min) {
                int step = element.getName().equals("totalStars") ? 500 : 1;
                if ((max - min) % step != 0)
                    step = 1;
                return (Control) new SliderControl((Option) option, min, max, step, value -> {
                    String label = switch (element.getName()) {
                        case "cloudScale" -> String.format(Locale.ROOT, "%.2f×", value / 4.0);
                        case "fogStart", "start" -> value + "%";
                        case "cloudHeight" -> value == -16 ? I18n.format("generator.default") : Integer.toString(value);
                        default -> Integer.toString(value);
                    };
                    return ComponentUtil.literal(label);
                });
            }
        }
        return null;
    }

    ControlElement<Object> createDelegate(Dim2i dim) {
        return delegate == null ? new ChoiceElement(option, dim) : delegate.createElement(dim);
    }
    @Override
    public Option<Object> getOption() {
        return option;
    }
    @Override
    public int getMaxWidth() {
        return delegate == null ? 130 : delegate.getMaxWidth();
    }
    @Override
    public ControlElement<Object> createElement(Dim2i dim) {
        return new PintoniumActionElement(option, this, dim);
    }

    private static final class ChoiceElement extends ControlElement<Object> {

        private final PintoniumOption choice;

        private ChoiceElement(PintoniumOption option, Dim2i dim) {
            super(option, dim);
            choice = option;
        }

        @Override
        public void render(GuiGraphics context, int x, int y, float delta) {
            super.render(context, x, y, delta);
            String text = "…";
            String[] values = choice.element.getValidValues();
            if (!choice.element.isList() && values != null && values.length > 0) {
                String[] labels = choice.element.getValidValuesDisplay();
                int index = Arrays.asList(values).indexOf(choice.getValue().toString());
                text = labels != null && index >= 0 && index < labels.length
                    ? labels[index]
                    : choice.getValue().toString();
            }
            int max = Math.max(1, Math.min(125, dim.width() / 2));
            text = font.trimStringToWidth(text, max);
            context.drawString(font, text, dim.getLimitX() - font.getStringWidth(text) - 6,
                dim.getCenterY() - 4, 0xFFFFFFFF);
        }

        private void activate(boolean backwards) {
            String[] values = choice.element.getValidValues();
            if (!choice.element.isList() && values != null && values.length > 0) {
                int index = Arrays.asList(values).indexOf(choice.getValue().toString());
                choice.setValue(values[Math.floorMod(index + (backwards ? -1 : 1), values.length)]);
            } else {
                var mc = Minecraft.getMinecraft();
                mc.displayGuiScreen(new VanillaExtraTextScreen(mc.currentScreen, choice.element,
                    choice.getValue(), choice::setValue));
            }
            playClickSound();
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            if (button != 0 || !choice.isAvailable() || !dim.containsCursor(x, y))
                return false;
            activate(net.minecraft.client.gui.GuiScreen.isShiftKeyDown());
            return true;
        }
        @Override
        public boolean keyPressed(int key) {
            if (!isFocused() || !choice.isAvailable() || !keySelected(key))
                return false;
            activate(net.minecraft.client.gui.GuiScreen.isShiftKeyDown());
            return true;
        }

    }

}
