package jp.s12kuma01.celeritasextra.compat.nothirium;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.gui.Translations;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.IConfigElement;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Vanilla buttons and sliders in the two-column video-settings layout. */
public final class VanillaExtraOptionsScreen extends GuiScreen {
    private static final int DONE = 2000, CANCEL = 2001, RESET = 2002, PREVIOUS = 2003, NEXT = 2004;
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

    @Override public void initGui() {
        buttonList.clear();
        visibleEntries.clear();
        int startY = Math.max(38, height / 6);
        int columns = width >= 320 ? 2 : 1;
        int rows = Math.max(1, (height - startY - 72) / 24);
        capacity = columns * rows;
        page = Math.min(page, pageCount() - 1);
        int buttonWidth = Math.min(150, (width - 30) / columns);
        int left = width / 2 - (columns * buttonWidth + (columns - 1) * 10) / 2;
        for (int index = page * capacity; index < Math.min(entries.size(), (page + 1) * capacity); index++) {
            var element = entries.get(index);
            int slot = visibleEntries.size();
            visibleEntries.add(element);
            int x = left + (slot % columns) * (buttonWidth + 10);
            int y = startY + (slot / columns) * 24;
            GuiButton button;
            if (element.isProperty() && !element.isList() && element.getType() == ConfigGuiType.INTEGER
                    && element.getMinValue() != null && element.getMaxValue() != null) {
                int min = Integer.parseInt(element.getMinValue().toString());
                int max = Integer.parseInt(element.getMaxValue().toString());
                button = new GuiSlider(new GuiPageButtonList.GuiResponder() {
                    @Override public void setEntryValue(int id, boolean value) { }
                    @Override public void setEntryValue(int id, String value) { }
                    @Override public void setEntryValue(int id, float value) {
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

    private int pageCount() { return Math.max(1, (entries.size() + Math.max(1, capacity) - 1) / Math.max(1, capacity)); }

    private String integerLabel(IConfigElement element, float value) {
        int rounded = element.getName().equals("totalStars") ? Math.round(value / 500) * 500 : Math.round(value);
        if (element.getName().equals("cloudScale")) return String.format(java.util.Locale.ROOT, "%.2f×", rounded / 4.0);
        if (element.getName().equals("fogStart") || element.getName().equals("start")) return rounded + "%";
        if (element.getName().equals("cloudHeight") && rounded == -16) return I18n.format("generator.default");
        return Integer.toString(rounded);
    }

    static String label(IConfigElement element) {
        String key = element.getLanguageKey();
        return key != null && !key.isEmpty() && I18n.hasKey(key) ? Translations.format(key) : element.getName();
    }

    private String buttonText(IConfigElement element, int buttonWidth) {
        String text = label(element);
        if (!element.isProperty() || element.isList()) text += "…";
        else if (element.getType() == ConfigGuiType.BOOLEAN) {
            text += ": " + I18n.format(Boolean.parseBoolean(session.value(element).toString()) ? "options.on" : "options.off");
        } else {
            String value = session.value(element).toString();
            String[] valid = element.getValidValues();
            String[] labels = element.getValidValuesDisplay();
            if (valid != null && labels != null) {
                for (int i = 0; i < valid.length; i++) if (valid[i].equals(value)) { value = labels[i]; break; }
            }
            text += ": " + value;
        }
        return trim(text, buttonWidth - 12);
    }

    private String trim(String text, int maxWidth) {
        return fontRenderer.getStringWidth(text) <= maxWidth ? text : fontRenderer.trimStringToWidth(text, maxWidth - 6) + "…";
    }

    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == DONE) {
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
                mc.displayGuiScreen(new VanillaExtraOptionsScreen(this, options, session, session.children(element), label(element), false));
            } else if (element.isList() || element.getType() == ConfigGuiType.STRING && element.getValidValues() == null) {
                mc.displayGuiScreen(new VanillaExtraTextScreen(this, session, element));
            } else if (element.getType() == ConfigGuiType.BOOLEAN) {
                session.set(element, !Boolean.parseBoolean(session.value(element).toString()));
                button.displayString = buttonText(element, button.width);
            } else if (element.getValidValues() != null) {
                var valid = List.of(element.getValidValues());
                int direction = isShiftKeyDown() ? -1 : 1;
                session.set(element, valid.get(Math.floorMod(valid.indexOf(session.value(element).toString()) + direction, valid.size())));
                button.displayString = buttonText(element, button.width);
            }
        }
    }

    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
        else super.keyTyped(typedChar, keyCode);
    }

    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0 && pageCount() > 1) {
            page = Math.floorMod(page + (scroll < 0 ? 1 : -1), pageCount());
            initGui();
        }
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, title, width / 2, 18, 0xFFFFFF);
        if (pageCount() > 1) drawCenteredString(fontRenderer, (page + 1) + " / " + pageCount(), width / 2, 29, 0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
        GuiButton hovered = buttonList.stream().filter(b -> b.id >= 0 && b.id < visibleEntries.size() && b.isMouseOver())
                .findFirst().orElse(null);
        if (hovered != hoveredButton) { hoveredButton = hovered; hoverStarted = System.currentTimeMillis(); }
        if (hovered != null && System.currentTimeMillis() - hoverStarted >= 600) {
            var element = visibleEntries.get(hovered.id);
            var tooltip = new ArrayList<String>();
            tooltip.add(label(element));
            String key = Translations.tooltipKey(element.getLanguageKey());
            String comment = I18n.hasKey(key) ? Translations.format(key) : element.getComment();
            if (comment != null && !comment.isBlank()) {
                for (String line : comment.split("\n")) tooltip.addAll(fontRenderer.listFormattedStringToWidth(line, Math.min(260, width - 30)));
            }
            drawHoveringText(tooltip, mouseX, mouseY);
        }
    }
}
