/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.mixin.options_search;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.embeddedt.embeddium.impl.gui.CeleritasVideoOptionsController;
import org.embeddedt.embeddium.impl.gui.frame.AbstractFrame;
import org.embeddedt.embeddium.impl.gui.frame.BasicFrame;
import org.embeddedt.embeddium.impl.gui.frame.tab.Tab;
import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.embeddedt.embeddium.impl.gui.widgets.FlatButtonWidget;
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.taumc.celeritas.api.options.structure.Option;
import org.taumc.celeritas.api.options.structure.OptionPage;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.sumirelabs.celeritasextra.client.CeleritasExtraClientMod;
import com.sumirelabs.celeritasextra.client.gui.CollapsibleTabFrame;
import com.sumirelabs.celeritasextra.client.gui.OptionActionFrame;
import com.sumirelabs.celeritasextra.client.gui.OptionSearchQuery;
import com.sumirelabs.celeritasextra.client.gui.OptionsCollapseState;
import com.sumirelabs.celeritasextra.client.gui.SearchableOptionsController;
import com.sumirelabs.celeritasextra.client.gui.rso.layout.ScreenLayout;
import com.sumirelabs.celeritasextra.client.gui.rso.theme.GuiThemes;
import com.sumirelabs.celeritasextra.client.gui.search.SearchIndex;
import com.sumirelabs.celeritasextra.compat.NativeOptionsSearch;

@Mixin(value = CeleritasVideoOptionsController.class, remap = false)
public abstract class MixinOptionsController implements SearchableOptionsController {

    @Shadow
    @Final private List<OptionPage> pages;
    @Shadow
    @Final private DrawContext font;
    @Shadow
    @Final private AtomicReference<Integer> optionPageScrollBarOffset;
    @Shadow
    @Final private AtomicReference<Integer> tabFrameScrollBarOffset;
    @Shadow
    @Final private static AtomicReference<TextComponent> tabFrameSelectedTab;
    @Shadow private int width;
    @Shadow private int height;
    @Shadow private FlatButtonWidget applyButton;
    @Shadow private FlatButtonWidget closeButton;
    @Shadow private FlatButtonWidget undoButton;
    @Shadow
    public abstract void init(int width, int height);

    @Unique private OptionSearchQuery celeritasExtra$query = new OptionSearchQuery("");
    @Unique private Dim2i celeritasExtra$bounds = new Dim2i(0, 0, 0, 0);
    @Unique private int celeritasExtra$results = -1;
    @Unique private int celeritasExtra$actionResults;
    @Unique private List<Option<?>> celeritasExtra$ranked;
    @Unique private SearchIndex<Option<?>> celeritasExtra$searchIndex;
    @Unique private final OptionsCollapseState celeritasExtra$collapse = new OptionsCollapseState();
    @Unique private boolean celeritasExtra$rso = true;
    @Unique private int celeritasExtra$searchCursor = -1;
    @Unique private Option<?> celeritasExtra$selectedResult;
    @Unique private int celeritasExtra$pageHeight;
    @Unique private int celeritasExtra$savedPageOffset;

    @Inject(method = "createTabFrame", at = @At("HEAD"))
    private void celeritasExtra$savePageOffset(Dim2i bounds, CallbackInfoReturnable<AbstractFrame> cir) {
        celeritasExtra$savedPageOffset = optionPageScrollBarOffset.get();
    }

    @ModifyConstant(method = "parentFrameBuilder", constant = @Constant(floatValue = 1.25F))
    private float celeritasExtra$wideLayout(float original) {
        return ScreenLayout.ASPECT_RATIO;
    }

    @ModifyConstant(method = "parentFrameBuilder", constant = @Constant(intValue = 550))
    private int celeritasExtra$allowWideLayoutAtSmallScales(int original) {
        return 0;
    }

    @Override
    public boolean celeritasExtra$usesRsoSearch() {
        return celeritasExtra$rso;
    }

    @Unique
    private boolean celeritasExtra$requestedRso() {
        return !NativeOptionsSearch.available(this)
            || CeleritasExtraClientMod.options().extraSettings.replaceSearchWithRso;
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void celeritasExtra$refreshCount(int width, int height, CallbackInfo ci) {
        celeritasExtra$rso = celeritasExtra$requestedRso();
        celeritasExtra$results = -1;
        celeritasExtra$ranked = null;
    }

    @Inject(method = "applyChanges", at = @At("RETURN"))
    private void celeritasExtra$applySearchMode(CallbackInfo ci) {
        if (celeritasExtra$rso == celeritasExtra$requestedRso())
            return;
        NativeOptionsSearch.clear(this);
        celeritasExtra$query = new OptionSearchQuery("");
        optionPageScrollBarOffset.set(0);
        tabFrameScrollBarOffset.set(0);
        init(width, height);
    }

    @Override
    public void celeritasExtra$search(String query) {
        celeritasExtra$searchCursor = -1;
        celeritasExtra$selectedResult = null;
        celeritasExtra$query = new OptionSearchQuery(query);
        celeritasExtra$ranked = null;
        celeritasExtra$results = -1;
        optionPageScrollBarOffset.set(0);
        tabFrameScrollBarOffset.set(0);
        // Rebuild frames, retaining the same Option objects. Apply/Undo still visit ALL pages.
        init(width, height);
    }

    @Override
    public void celeritasExtra$nextSearchResult(boolean reverse) {
        if (!celeritasExtra$rso || celeritasExtra$query.isEmpty())
            return;
        var results = celeritasExtra$rankedOptions();
        if (results.isEmpty())
            return;
        celeritasExtra$searchCursor = celeritasExtra$searchCursor < 0
            ? (reverse ? results.size() - 1 : 0)
            : Math.floorMod(celeritasExtra$searchCursor + (reverse ? -1 : 1), results.size());
        celeritasExtra$selectedResult = results.get(celeritasExtra$searchCursor);
        for (var page : pages)
            if (page.getOptions().contains(celeritasExtra$selectedResult)) {
                celeritasExtra$collapse.selectTab(page.getId().getModId() + ":" + page.getId().getPath());
                tabFrameSelectedTab.set(page.getName());
                var matches = results.stream().filter(page.getOptions()::contains).toList();
                optionPageScrollBarOffset.set(
                    Math.max(0, (matches.indexOf(celeritasExtra$selectedResult) + 1) * 18 - celeritasExtra$pageHeight));
                break;
            }
        init(width, height);
    }

    @Override
    public Dim2i celeritasExtra$searchBounds() {
        return celeritasExtra$bounds;
    }
    @Override
    public DrawContext celeritasExtra$drawContext() {
        return font;
    }
    @Override
    public int celeritasExtra$optionCount() {
        return pages.stream().mapToInt(page -> page.getOptions().size()).sum();
    }
    @Override
    public boolean celeritasExtra$hasChanges() {
        return pages.stream().flatMap(page -> page.getOptions().stream()).anyMatch(Option::hasChanged);
    }
    @Override
    public int celeritasExtra$resultCount() {
        if (celeritasExtra$results < 0)
            celeritasExtra$results = (celeritasExtra$query.isEmpty()
                ? celeritasExtra$optionCount()
                : celeritasExtra$rankedOptions().size()) + celeritasExtra$actionResults;
        return celeritasExtra$results;
    }

    @Unique
    private boolean celeritasExtra$matches(Option<?> option) {
        return celeritasExtra$query.isEmpty() || celeritasExtra$rankedOptions().contains(option);
    }

    @Unique
    private List<Option<?>> celeritasExtra$rankedOptions() {
        if (celeritasExtra$ranked == null) {
            if (celeritasExtra$searchIndex == null) {
                List<Option<?>> options = pages.stream().flatMap(page -> page.getOptions().stream()).distinct()
                    .toList();
                celeritasExtra$searchIndex = OptionSearchQuery.index(options,
                    option -> font.extractString(option.getName()) + " " + font.extractString(option.getTooltip()));
            }
            celeritasExtra$ranked = celeritasExtra$query.results(celeritasExtra$searchIndex)
                .stream().<Option<?>>map(result -> result.item()).toList();
        }
        return celeritasExtra$ranked;
    }

    @Inject(method = "parentBasicFrameBuilder", at = @At("HEAD"))
    private void celeritasExtra$searchLayout(Dim2i parent, Dim2i tabs, CallbackInfoReturnable<BasicFrame.Builder> cir) {
        celeritasExtra$pageHeight = tabs.height();
        celeritasExtra$bounds = new Dim2i(tabs.x(), Math.max(2, tabs.y() - 26), tabs.width(), 20);
        applyButton.setStyle(GuiThemes.buttonStyle());
        closeButton.setStyle(GuiThemes.buttonStyle());
        undoButton.setStyle(GuiThemes.buttonStyle());
    }

    @ModifyReturnValue(method = "createTabFrame", at = @At("RETURN"))
    private AbstractFrame celeritasExtra$filterOptions(AbstractFrame original, Dim2i bounds) {
        // Native inactive-page construction must not replace the active page's scroll offset.
        optionPageScrollBarOffset.set(celeritasExtra$savedPageOffset);
        // Let Celeritas build its full list first, including Shader Packs and other action tabs.
        if (!(original instanceof TabFrameAccess access))
            return original;
        Map<String, List<Tab<?>>> tabs = new LinkedHashMap<>();
        celeritasExtra$actionResults = 0;
        boolean rsoSearching = celeritasExtra$rso && !celeritasExtra$query.isEmpty();
        boolean searching = rsoSearching || (!celeritasExtra$rso && NativeOptionsSearch.hasQuery(this));
        java.util.function.Predicate<Option<?>> predicate = celeritasExtra$rso
            ? this::celeritasExtra$matches
            : NativeOptionsSearch.predicate(this);
        for (var group : access.celeritasExtra$nativeTabs().entrySet()) {
            for (Tab<?> tab : group.getValue()) {
                var page = pages.stream().filter(candidate -> candidate.getId().equals(tab.id())).findFirst()
                    .orElse(null);
                Tab<?> filtered;
                if (page != null && tab.frameFunction() != null) {
                    if (page.getOptions().stream().noneMatch(predicate))
                        continue;
                    var pageFilter = rsoSearching
                        ? new OptionActionFrame.Ranked(celeritasExtra$rankedOptions().stream()
                            .filter(page.getOptions()::contains).toList(), celeritasExtra$selectedResult)
                        : predicate;
                    filtered = new Tab<>(tab.id(), tab.title(), tab.onSelectFunction(),
                        dim -> OptionActionFrame.createRso(dim, page, pageFilter,
                            optionPageScrollBarOffset));
                } else {
                    if (rsoSearching && !celeritasExtra$query.matches(
                        font.extractString(tab.title()) + "\n" + tab.title() + "\n" + tab.id()))
                        continue;
                    filtered = tab;
                    celeritasExtra$actionResults++;
                }
                tabs.computeIfAbsent(group.getKey(), ignored -> new ArrayList<>()).add(filtered);
            }
        }
        celeritasExtra$results = -1;
        return new CollapsibleTabFrame(bounds, font, tabs, celeritasExtra$collapse,
            searching, tabFrameSelectedTab, tabFrameScrollBarOffset,
            () -> optionPageScrollBarOffset.set(0), () -> init(width, height), optionPageScrollBarOffset);
    }

}
