package jp.s12kuma01.celeritasextra.mixin.options_search;

import jp.s12kuma01.celeritasextra.client.gui.Translations;

import jp.s12kuma01.celeritasextra.client.gui.OptionsSearchScreen;
import jp.s12kuma01.celeritasextra.client.gui.SearchableOptionsController;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.embeddedt.embeddium.impl.gui.CeleritasVideoOptionsController;
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.lwjgl.input.Keyboard;
import org.taumc.celeritas.impl.gui.CeleritasVideoOptionsScreen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CeleritasVideoOptionsScreen.class, remap = false)
public abstract class MixinOptionsScreen extends GuiScreen implements OptionsSearchScreen {
    @Shadow @Final private CeleritasVideoOptionsController controller;
    @Unique private GuiTextField celeritasExtra$field;
    @Unique private Dim2i celeritasExtra$fieldBounds;
    @Unique private Dim2i celeritasExtra$clearBounds;
    @Unique private boolean celeritasExtra$repeatBefore;
    @Unique private boolean celeritasExtra$opened;

    @Unique private SearchableOptionsController celeritasExtra$searchController() {
        return (SearchableOptionsController) controller;
    }

    @Inject(method = "initGui", at = @At("RETURN"), remap = true)
    private void celeritasExtra$initSearch(CallbackInfo ci) {
        String text = celeritasExtra$field == null ? "" : celeritasExtra$field.getText();
        boolean focused = celeritasExtra$field != null && celeritasExtra$field.isFocused();
        int cursor = celeritasExtra$field == null ? 0 : celeritasExtra$field.getCursorPosition();
        int selection = celeritasExtra$field == null ? 0 : celeritasExtra$field.getSelectionEnd();
        Dim2i bounds = celeritasExtra$searchController().celeritasExtra$searchBounds();
        int countWidth = fontRenderer.getStringWidth(Translations.format("celeritasextra.search.results",
                celeritasExtra$searchController().celeritasExtra$optionCount())) + 10;
        celeritasExtra$fieldBounds = new Dim2i(bounds.x(), bounds.y(), Math.max(40, bounds.width() - countWidth - 22), bounds.height());
        celeritasExtra$clearBounds = new Dim2i(bounds.getLimitX() - 18, bounds.y(), 18, bounds.height());
        celeritasExtra$field = new GuiTextField(0, fontRenderer, bounds.x(), bounds.y(),
                celeritasExtra$fieldBounds.width(), bounds.height());
        celeritasExtra$field.setMaxStringLength(128);
        celeritasExtra$field.setText(text);
        celeritasExtra$field.setCursorPosition(cursor);
        celeritasExtra$field.setSelectionPos(selection);
        celeritasExtra$field.setFocused(focused);
        if (!celeritasExtra$opened) {
            celeritasExtra$repeatBefore = Keyboard.areRepeatEventsEnabled();
            celeritasExtra$opened = true;
        }
        Keyboard.enableRepeatEvents(true);
    }

    @Inject(method = "drawScreen", at = @At("RETURN"), remap = true)
    private void celeritasExtra$drawSearch(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (celeritasExtra$field == null) return;
        celeritasExtra$field.drawTextBox();
        int baseline = celeritasExtra$fieldBounds.y() + (celeritasExtra$fieldBounds.height() - 8) / 2;
        if (celeritasExtra$field.getText().isEmpty() && !celeritasExtra$field.isFocused()) {
            fontRenderer.drawString(fontRenderer.trimStringToWidth(Translations.format("rso.search_bar_empty"),
                    celeritasExtra$fieldBounds.width() - 8), celeritasExtra$fieldBounds.x() + 4, baseline, 0x808080);
        }
        fontRenderer.drawString(Translations.format("celeritasextra.search.results",
                celeritasExtra$searchController().celeritasExtra$resultCount()),
                celeritasExtra$fieldBounds.getLimitX() + 5, baseline, 0xFFFFFF);
        var clear = celeritasExtra$clearBounds;
        Gui.drawRect(clear.x(), clear.y(), clear.getLimitX(), clear.getLimitY(),
                clear.containsCursor(mouseX, mouseY) ? 0xFF555555 : 0xFF333333);
        fontRenderer.drawString("X", clear.x() + 6, baseline, 0xFFFFFF);
        if (celeritasExtra$searchController().celeritasExtra$resultCount() == 0) {
            String empty = Translations.format("celeritasextra.search.no_results");
            fontRenderer.drawString(empty, (width - fontRenderer.getStringWidth(empty)) / 2,
                    clear.getLimitY() + 30, 0xAAAAAA);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = true)
    private void celeritasExtra$clickSearch(int mouseX, int mouseY, int button, CallbackInfo ci) {
        if (celeritasExtra$field == null) return;
        celeritasExtra$field.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && celeritasExtra$clearBounds.containsCursor(mouseX, mouseY)) {
            celeritasExtra$field.setText("");
            celeritasExtra$field.setFocused(true);
            celeritasExtra$searchController().celeritasExtra$search("");
            ci.cancel();
        } else if (celeritasExtra$fieldBounds.containsCursor(mouseX, mouseY)) ci.cancel();
    }

    @Override public boolean celeritasExtra$searchKey(char character, int key) {
        if (celeritasExtra$field == null) return false;
        if (GuiScreen.isCtrlKeyDown() && key == Keyboard.KEY_F) {
            celeritasExtra$field.setFocused(true);
            return true;
        }
        if (key == Keyboard.KEY_ESCAPE) {
            if (!celeritasExtra$field.getText().isEmpty()) {
                celeritasExtra$field.setText("");
                celeritasExtra$searchController().celeritasExtra$search("");
                return true;
            }
            if (celeritasExtra$field.isFocused()) {
                celeritasExtra$field.setFocused(false);
                return true;
            }
            return celeritasExtra$searchController().celeritasExtra$hasChanges();
        }
        if (!celeritasExtra$field.isFocused()) return false;
        String before = celeritasExtra$field.getText();
        celeritasExtra$field.textboxKeyTyped(character, key);
        if (!before.equals(celeritasExtra$field.getText()))
            celeritasExtra$searchController().celeritasExtra$search(celeritasExtra$field.getText());
        return true;
    }

    @Override public void celeritasExtra$tickSearch() {
        if (celeritasExtra$field != null) celeritasExtra$field.updateCursorCounter();
    }

    @Override public void celeritasExtra$closeSearch() {
        if (celeritasExtra$opened) {
            Keyboard.enableRepeatEvents(celeritasExtra$repeatBefore);
            celeritasExtra$opened = false;
        }
    }
}
