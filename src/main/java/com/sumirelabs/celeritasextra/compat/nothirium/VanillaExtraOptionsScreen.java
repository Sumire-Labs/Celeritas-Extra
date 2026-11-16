/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.compat.nothirium;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod;
import com.sumirelabs.celeritasextra.client.gui.OptionActionIcon;
import com.sumirelabs.celeritasextra.client.gui.OptionSearchQuery;
import com.sumirelabs.celeritasextra.client.gui.Translations;
import com.sumirelabs.celeritasextra.client.gui.search.SearchIndex;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.IConfigElement;

/** Vanilla buttons and sliders in the two-column video-settings layout. */
public final class VanillaExtraOptionsScreen extends GuiScreen {

    private static final int DONE = 2000, CANCEL = 2001, RESET = 2002, PREVIOUS = 2003, NEXT = 2004;
    private static final int ACTION = 10000;
    private final GuiScreen parent;
    private final ForgeExtraConfigElements options;
    private final ExtraOptionsSession session;
    private final List<IConfigElement> entries;
    private final boolean root;
    private final String title;
    private int page;
    private int capacity;
    private final List<IConfigElement> visibleEntries = new ArrayList<>();
    private GuiButton hoveredButton;
    private long hoverStarted;
    private GuiTextField search;
    private List<IConfigElement> displayed;
    private SearchIndex<IConfigElement> searchIndex;
    private boolean repeatBefore, opened;

    public VanillaExtraOptionsScreen(GuiScreen parent) {
        this(parent, new ForgeExtraConfigElements(CeleritasExtraClientMod.options()));
    }

    private VanillaExtraOptionsScreen(GuiScreen parent, ForgeExtraConfigElements options) {
        this(parent, options, new ExtraOptionsSession(options.categories()), options.categories(),
            Translations.format("celeritasextra.gui.extra_settings"), true);
    }

    private VanillaExtraOptionsScreen(GuiScreen parent, ForgeExtraConfigElements options, ExtraOptionsSession session,
        List<IConfigElement> entries, String title, boolean root) {
        this.parent = parent;
        this.options = options;
        this.session = session;
        this.entries = entries;
        this.title = title;
        this.root = root;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        visibleEntries.clear();
        String query = search == null ? "" : search.getText();
        boolean focused = search != null && search.isFocused();
        int cursor = search == null ? 0 : search.getCursorPosition();
        int selection = search == null ? 0 : search.getSelectionEnd();
        search = new GuiTextField(3000, fontRenderer, Math.max(10, width / 2 - 150), 34, Math.min(300, width - 20), 20);
        search.setMaxStringLength(128);
        search.setText(query);
        search.setCursorPosition(cursor);
        search.setSelectionPos(selection);
        search.setFocused(focused);
        if (!opened) {
            repeatBefore = Keyboard.areRepeatEventsEnabled();
            opened = true;
        }
        Keyboard.enableRepeatEvents(true);
        var matcher = new OptionSearchQuery(query);
        if (searchIndex == null)
            searchIndex = OptionSearchQuery.index(session.properties(entries), element -> label(element) + " "
                + (element.getComment() == null ? "" : element.getComment()));
        displayed = matcher.isEmpty()
            ? entries
            : matcher.results(searchIndex)
                .stream().map(result -> result.item()).toList();
        int startY = Math.max(62, height / 6);
        int columns = width >= 320 ? 2 : 1;
        int rows = Math.max(1, (height - startY - 72) / 24);
        capacity = columns * rows;
        page = Math.min(page, pageCount() - 1);
        int cellWidth = Math.min(190, (width - 30) / columns);
        int left = width / 2 - (columns * cellWidth + (columns - 1) * 10) / 2;
        for (int index = page * capacity; index < Math.min(displayed.size(), (page + 1) * capacity); index++) {
            var element = displayed.get(index);
            int buttonWidth = cellWidth - (element.isProperty() ? 40 : 0);
            int slot = visibleEntries.size();
            visibleEntries.add(element);
            int x = left + (slot % columns) * (cellWidth + 10);
            int y = startY + (slot / columns) * 24;
            GuiButton button;
            if (element.isProperty() && !element.isList() && element.getType() == ConfigGuiType.INTEGER
                && element.getMinValue() != null && element.getMaxValue() != null) {
                int min = Integer.parseInt(element.getMinValue().toString());
                int max = Integer.parseInt(element.getMaxValue().toString());
                button = new GuiSlider(new GuiPageButtonList.GuiResponder() {

                    @Override
                    public void setEntryValue(int id, boolean value) {}
                    @Override
                    public void setEntryValue(int id, String value) {}
                    @Override
                    public void setEntryValue(int id, float value) {
                        int step = element.getName().equals("totalStars") ? 500 : 1;
                        session.set(element, Math.clamp(Math.round(value / step) * step, min, max));
                    }

                }, slot, x, y, label(element), min, max,
                    ((Number) session.value(element)).floatValue(),
                    (id, name, value) -> trim(name + ": " + integerLabel(element, value), buttonWidth - 16));
                button.width = buttonWidth;
            } else {
                button = new GuiButton(slot, x, y, buttonWidth, 20, buttonText(element, buttonWidth));
            }
            buttonList.add(button);
            if (element.isProperty()) {
                var undo = new OptionIconButton(ACTION + slot * 2, x + buttonWidth, y, OptionActionIcon.UNDO);
                undo.enabled = session.changed(element);
                undo.visible = undo.enabled;
                buttonList.add(undo);
                var reset = new OptionIconButton(ACTION + slot * 2 + 1, x + buttonWidth + 20, y,
                    OptionActionIcon.RESET);
                reset.enabled = session.canReset(element);
                reset.visible = reset.enabled;
                buttonList.add(reset);
            }
        }
        buttonList.add(new GuiButton(RESET, width / 2 - 50, height - 54, 100, 20,
            I18n.format("controls.reset")));
        if (pageCount() > 1) {
            buttonList.add(new GuiButton(PREVIOUS, width / 2 - 155, height - 54, 95, 20, "<"));
            buttonList.add(new GuiButton(NEXT, width / 2 + 60, height - 54, 95, 20, ">"));
        }
        if (root) {
            buttonList.add(new GuiButton(DONE, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
            buttonList.add(new GuiButton(CANCEL, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
        } else {
            buttonList.add(new GuiButton(DONE, width / 2 - 100, height - 28, 200, 20, I18n.format("gui.done")));
        }
    }

    private int pageCount() {
        return Math.max(1,
            ((displayed == null ? entries : displayed).size() + Math.max(1, capacity) - 1) / Math.max(1, capacity));
    }

    private String integerLabel(IConfigElement element, float value) {
        int rounded = element.getName().equals("totalStars") ? Math.round(value / 500) * 500 : Math.round(value);
        if (element.getName().equals("cloudScale"))
            return String.format(java.util.Locale.ROOT, "%.2f×", rounded / 4.0);
        if (element.getName().equals("fogStart") || element.getName().equals("start"))
            return rounded + "%";
        if (element.getName().equals("cloudHeight") && rounded == -16)
            return I18n.format("generator.default");
        return Integer.toString(rounded);
    }

    public static String label(IConfigElement element) {
        String key = element.getLanguageKey();
        return key != null && !key.isEmpty() && I18n.hasKey(key) ? Translations.format(key) : element.getName();
    }

    private String buttonText(IConfigElement element, int buttonWidth) {
        String text = label(element);
        if (!element.isProperty() || element.isList())
            text += "…";
        else if (element.getType() == ConfigGuiType.BOOLEAN) {
            text += ": "
                + I18n.format(Boolean.parseBoolean(session.value(element).toString()) ? "options.on" : "options.off");
        } else {
            String value = session.value(element).toString();
            String[] valid = element.getValidValues();
            String[] labels = element.getValidValuesDisplay();
            if (valid != null && labels != null) {
                for (int i = 0; i < valid.length; i++)
                    if (valid[i].equals(value)) {
                        value = labels[i];
                        break;
                    }
            }
            text += ": " + value;
        }
        return trim(text, buttonWidth - 12);
    }

    private String trim(String text, int maxWidth) {
        return fontRenderer.getStringWidth(text) <= maxWidth
            ? text
            : fontRenderer.trimStringToWidth(text, maxWidth - 6) + "…";
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id >= ACTION && (button.id - ACTION) / 2 < visibleEntries.size()) {
            var element = visibleEntries.get((button.id - ACTION) / 2);
            if ((button.id - ACTION) % 2 == 0)
                session.undo(element);
            else
                session.reset(List.of(element));
            initGui();
        } else if (button.id == DONE) {
            if (root && session.changed()) {
                session.commit();
                options.apply();
            }
            mc.displayGuiScreen(parent);
        } else if (button.id == CANCEL) {
            mc.displayGuiScreen(parent);
        } else if (button.id == RESET) {
            session.reset(entries);
            initGui();
        } else if (button.id == PREVIOUS || button.id == NEXT) {
            page = Math.floorMod(page + (button.id == NEXT ? 1 : -1), pageCount());
            initGui();
        } else if (button.id >= 0 && button.id < visibleEntries.size() && !(button instanceof GuiSlider)) {
            var element = visibleEntries.get(button.id);
            if (!element.isProperty()) {
                mc.displayGuiScreen(new VanillaExtraOptionsScreen(this, options, session, session.children(element),
                    label(element), false));
            } else if (element.isList()
                || element.getType() == ConfigGuiType.STRING && element.getValidValues() == null) {
                mc.displayGuiScreen(new VanillaExtraTextScreen(this, session, element));
            } else if (element.getType() == ConfigGuiType.BOOLEAN) {
                session.set(element, !Boolean.parseBoolean(session.value(element).toString()));
                button.displayString = buttonText(element, button.width);
            } else if (element.getValidValues() != null) {
                var valid = List.of(element.getValidValues());
                int direction = isShiftKeyDown() ? -1 : 1;
                session.set(element, valid
                    .get(Math.floorMod(valid.indexOf(session.value(element).toString()) + direction, valid.size())));
                button.displayString = buttonText(element, button.width);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (isCtrlKeyDown() && keyCode == Keyboard.KEY_F) {
            search.setFocused(true);
        } else if (keyCode == Keyboard.KEY_ESCAPE && !search.getText().isEmpty()) {
            search.setText("");
            page = 0;
            initGui();
        } else if (keyCode == Keyboard.KEY_ESCAPE)
            mc.displayGuiScreen(parent);
        else if (search.isFocused()) {
            String before = search.getText();
            search.textboxKeyTyped(typedChar, keyCode);
            if (!before.equals(search.getText())) {
                page = 0;
                initGui();
            }
        } else
            super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        search.mouseClicked(x, y, button);
        super.mouseClicked(x, y, button);
    }

    @Override
    public void updateScreen() {
        search.updateCursorCounter();
        for (var button : buttonList) {
            if (button.id >= ACTION && (button.id - ACTION) / 2 < visibleEntries.size()) {
                var element = visibleEntries.get((button.id - ACTION) / 2);
                button.enabled = (button.id - ACTION) % 2 == 0 ? session.changed(element) : session.canReset(element);
                button.visible = button.enabled;
            }
        }
    }

    @Override
    public void onGuiClosed() {
        if (opened) {
            Keyboard.enableRepeatEvents(repeatBefore);
            opened = false;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0 && pageCount() > 1) {
            page = Math.floorMod(page + (scroll < 0 ? 1 : -1), pageCount());
            initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, title, width / 2, 18, 0xFFFFFF);
        search.drawTextBox();
        if (search.getText().isEmpty() && !search.isFocused())
            fontRenderer.drawString(Translations.format("rso.search_bar_empty"), search.x + 4, search.y + 6, 0x888888);
        if (pageCount() > 1)
            drawCenteredString(fontRenderer, (page + 1) + " / " + pageCount(), width / 2, height - 66, 0xAAAAAA);
        if (displayed.isEmpty())
            drawCenteredString(fontRenderer, Translations.format("celeritasextra.search.no_results"), width / 2, 66,
                0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
        GuiButton hovered = buttonList.stream()
            .filter(b -> b.visible && (b.id >= 0 && b.id < visibleEntries.size() || b.id >= ACTION) && b.isMouseOver())
            .findFirst().orElse(null);
        if (hovered != hoveredButton) {
            hoveredButton = hovered;
            hoverStarted = System.currentTimeMillis();
        }
        if (hovered != null && System.currentTimeMillis() - hoverStarted >= 600) {
            boolean action = hovered.id >= ACTION;
            var element = visibleEntries.get(action ? (hovered.id - ACTION) / 2 : hovered.id);
            var tooltip = new ArrayList<String>();
            tooltip.add(label(element));
            if (action)
                tooltip.add(Translations.format((hovered.id - ACTION) % 2 == 0
                    ? "celeritasextra.option_action.undo"
                    : "celeritasextra.option_action.reset"));
            String key = Translations.tooltipKey(element.getLanguageKey());
            String comment = I18n.hasKey(key) ? Translations.format(key) : element.getComment();
            if (comment != null && !comment.isBlank()) {
                for (String line : comment.split("\n"))
                    tooltip.addAll(fontRenderer.listFormattedStringToWidth(line, Math.min(260, width - 30)));
            }
            drawHoveringText(tooltip, mouseX, mouseY);
        }
    }

}
