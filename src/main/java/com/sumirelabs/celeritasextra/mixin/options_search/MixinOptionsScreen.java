package com.sumirelabs.celeritasextra.mixin.options_search;

import com.sumirelabs.celeritasextra.client.gui.Translations;

import com.sumirelabs.celeritasextra.client.gui.OptionsSearchScreen;
import com.sumirelabs.celeritasextra.client.gui.SearchableOptionsController;
import com.sumirelabs.celeritasextra.client.gui.CeleritasSearchBar;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.embeddedt.embeddium.impl.gui.CeleritasVideoOptionsController;
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.taumc.celeritas.impl.gui.VintageInteractionContext;
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
    @Unique private CeleritasSearchBar celeritasExtra$bar;
    @Unique private int celeritasExtra$cursorTicks;
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
        celeritasExtra$bar = new CeleritasSearchBar(bounds, this::celeritasExtra$clear);
        // Keep vanilla's editing/clipboard behavior, but never invoke its drawing methods.
        celeritasExtra$field = new GuiTextField(0, fontRenderer, bounds.x(), bounds.y(), bounds.width(), bounds.height());
        celeritasExtra$field.setEnableBackgroundDrawing(false);
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

    @Unique private void celeritasExtra$clear() {
        celeritasExtra$field.setText("");
        celeritasExtra$field.setFocused(true);
        celeritasExtra$cursorTicks = 0;
        celeritasExtra$searchController().celeritasExtra$search("");
    }

    @Unique private void celeritasExtra$updateBar() {
        celeritasExtra$bar.update(celeritasExtra$field.getText(), celeritasExtra$field.getCursorPosition(),
                celeritasExtra$field.getSelectionEnd(), celeritasExtra$field.isFocused(), celeritasExtra$cursorTicks / 6 % 2 == 0,
                Translations.format("rso.search_bar_empty"), Translations.format("celeritasextra.search.results",
                        celeritasExtra$searchController().celeritasExtra$resultCount()));
    }

    @Inject(method = "drawScreen", at = @At("RETURN"), remap = true)
    private void celeritasExtra$drawSearch(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (celeritasExtra$field == null) return;
        celeritasExtra$updateBar();
        var context = celeritasExtra$searchController().celeritasExtra$drawContext();
        celeritasExtra$bar.render(context, mouseX, mouseY, partialTicks);
        if (celeritasExtra$searchController().celeritasExtra$resultCount() == 0) {
            String empty = Translations.format("celeritasextra.search.no_results");
            context.drawString(empty, (width - context.getStringWidth(empty)) / 2,
                    celeritasExtra$bar.bounds().getLimitY() + 30, 0x90FFFFFF);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = true)
    private void celeritasExtra$clickSearch(int mouseX, int mouseY, int button, CallbackInfo ci) {
        if (celeritasExtra$field == null) return;
        celeritasExtra$updateBar();
        if (celeritasExtra$bar.clickClear(VintageInteractionContext.INSTANCE, mouseX, mouseY, button)) {
            ci.cancel();
        } else if (button == 0 && celeritasExtra$bar.isMouseOver(mouseX, mouseY)) {
            celeritasExtra$field.setFocused(true);
            int cursor = celeritasExtra$bar.cursorAt(celeritasExtra$searchController().celeritasExtra$drawContext(), mouseX);
            if (GuiScreen.isShiftKeyDown()) celeritasExtra$field.setSelectionPos(cursor);
            else celeritasExtra$field.setCursorPosition(cursor);
            celeritasExtra$cursorTicks = 0;
            ci.cancel();
        } else celeritasExtra$field.setFocused(false);
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
        celeritasExtra$cursorTicks = 0;
        String before = celeritasExtra$field.getText();
        celeritasExtra$field.textboxKeyTyped(character, key);
        if (!before.equals(celeritasExtra$field.getText()))
            celeritasExtra$searchController().celeritasExtra$search(celeritasExtra$field.getText());
        return true;
    }

    @Override public void celeritasExtra$tickSearch() {
        celeritasExtra$cursorTicks++;
    }

    @Override public void celeritasExtra$closeSearch() {
        if (celeritasExtra$opened) {
            Keyboard.enableRepeatEvents(celeritasExtra$repeatBefore);
            celeritasExtra$opened = false;
        }
    }
}
