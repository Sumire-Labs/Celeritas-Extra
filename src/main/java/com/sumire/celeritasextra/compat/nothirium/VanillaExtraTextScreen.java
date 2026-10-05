package com.sumire.celeritasextra.compat.nothirium;

import com.sumire.celeritasextra.client.gui.Translations;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.IConfigElement;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Vanilla text fields for class exemption lists; edits remain local until Done. */
final class VanillaExtraTextScreen extends GuiScreen {
    private final GuiScreen parent;
    private final ExtraOptionsSession session;
    private final IConfigElement element;
    private final List<String> draft = new ArrayList<>();
    private final List<GuiTextField> fields = new ArrayList<>();
    private int page;
    private int rows;
    private int firstRow;

    VanillaExtraTextScreen(GuiScreen parent, ExtraOptionsSession session, IConfigElement element) {
        this.parent = parent;
        this.session = session;
        this.element = element;
        Object value = session.value(element);
        if (element.isList()) Arrays.stream((Object[]) value).map(Object::toString).forEach(draft::add);
        else draft.add(value.toString());
    }

    private void captureFields() {
        for (int i = 0; i < fields.size(); i++) draft.set(firstRow + i, fields.get(i).getText());
    }

    @Override public void initGui() {
        captureFields();
        fields.clear();
        buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        rows = Math.max(1, (height - 120) / 24);
        page = Math.min(page, pages() - 1);
        firstRow = page * rows;
        int fieldWidth = Math.min(270, width - 75);
        int left = width / 2 - (fieldWidth + 26) / 2;
        for (int index = firstRow; index < Math.min(draft.size(), firstRow + rows); index++) {
            int slot = fields.size();
            var field = new GuiTextField(slot, fontRenderer, left, 52 + slot * 24, fieldWidth, 20);
            field.setMaxStringLength(32767);
            field.setText(draft.get(index));
            fields.add(field);
            if (element.isList()) buttonList.add(new GuiButton(slot, left + fieldWidth + 5, field.y, 20, 20, "×"));
        }
        if (element.isList()) {
            buttonList.add(new GuiButton(2002, width / 2 - 95, height - 54, 90, 20, Translations.format("celeritasextra.gui.add_entry")));
            buttonList.add(new GuiButton(2003, width / 2 + 5, height - 54, 90, 20, I18n.format("controls.reset")));
            if (pages() > 1) {
                buttonList.add(new GuiButton(2004, width / 2 - 145, height - 54, 40, 20, "<"));
                buttonList.add(new GuiButton(2005, width / 2 + 105, height - 54, 40, 20, ">"));
            }
        }
        buttonList.add(new GuiButton(2000, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
        buttonList.add(new GuiButton(2001, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
    }

    private int pages() { return Math.max(1, (draft.size() + Math.max(1, rows) - 1) / Math.max(1, rows)); }

    @Override protected void actionPerformed(GuiButton button) {
        captureFields();
        if (button.id == 2000) {
            if (element.isList()) session.set(element, draft.stream().map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new));
            else session.set(element, draft.getFirst());
            mc.displayGuiScreen(parent);
        } else if (button.id == 2001) {
            mc.displayGuiScreen(parent);
        } else {
            // Clear controls before changing indexes, so initGui does not recapture old rows.
            fields.clear();
            if (button.id == 2002) {
                draft.add("");
                page = pages() - 1;
            } else if (button.id == 2003) {
                draft.clear();
                Arrays.stream(element.getDefaults()).map(Object::toString).forEach(draft::add);
                page = 0;
            } else if (button.id == 2004 || button.id == 2005) {
                page = Math.floorMod(page + (button.id == 2005 ? 1 : -1), pages());
            } else if (button.id >= 0 && button.id < rows) {
                draft.remove(firstRow + button.id);
            }
            initGui();
        }
    }

    @Override protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        fields.forEach(field -> field.mouseClicked(x, y, button));
    }
    @Override protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
        else if (keyCode == Keyboard.KEY_TAB && !fields.isEmpty()) {
            int focused = -1;
            for (int i = 0; i < fields.size(); i++) if (fields.get(i).isFocused()) focused = i;
            int next = Math.floorMod(focused + (isShiftKeyDown() ? -1 : 1), fields.size());
            for (int i = 0; i < fields.size(); i++) fields.get(i).setFocused(i == next);
        } else fields.forEach(field -> field.textboxKeyTyped(typedChar, keyCode));
    }
    @Override public void updateScreen() { fields.forEach(GuiTextField::updateCursorCounter); }
    @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
    @Override public void drawScreen(int x, int y, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, VanillaExtraOptionsScreen.label(element), width / 2, 18, 0xFFFFFF);
        if (pages() > 1) drawCenteredString(fontRenderer, (page + 1) + " / " + pages(), width / 2, 32, 0xAAAAAA);
        fields.forEach(GuiTextField::drawTextBox);
        super.drawScreen(x, y, partialTicks);
    }
}
