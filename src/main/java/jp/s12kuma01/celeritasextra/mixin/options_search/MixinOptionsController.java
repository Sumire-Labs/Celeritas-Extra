package jp.s12kuma01.celeritasextra.mixin.options_search;

import jp.s12kuma01.celeritasextra.client.gui.OptionSearchQuery;
import jp.s12kuma01.celeritasextra.client.gui.SearchableOptionsController;
import org.embeddedt.embeddium.impl.gui.CeleritasVideoOptionsController;
import org.embeddedt.embeddium.impl.gui.frame.AbstractFrame;
import org.embeddedt.embeddium.impl.gui.frame.BasicFrame;
import org.embeddedt.embeddium.impl.gui.frame.tab.Tab;
import org.embeddedt.embeddium.impl.gui.frame.tab.TabFrame;
import org.embeddedt.embeddium.impl.gui.framework.DrawContext;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.taumc.celeritas.api.options.structure.Option;
import org.taumc.celeritas.api.options.structure.OptionPage;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(value = CeleritasVideoOptionsController.class, remap = false)
public abstract class MixinOptionsController implements SearchableOptionsController {
    @Shadow @Final private List<OptionPage> pages;
    @Shadow @Final private DrawContext font;
    @Shadow @Final private AtomicReference<Integer> optionPageScrollBarOffset;
    @Shadow @Final private AtomicReference<Integer> tabFrameScrollBarOffset;
    @Shadow @Final private static AtomicReference<TextComponent> tabFrameSelectedTab;
    @Shadow private int width;
    @Shadow private int height;
    @Shadow public abstract void init(int width, int height);

    @Unique private OptionSearchQuery celeritasExtra$query = new OptionSearchQuery("");
    @Unique private Dim2i celeritasExtra$bounds = new Dim2i(0, 0, 0, 0);
    @Unique private int celeritasExtra$results = -1;

    @Inject(method = "init", at = @At("HEAD"))
    private void celeritasExtra$refreshCount(int width, int height, CallbackInfo ci) {
        celeritasExtra$results = -1;
    }

    @Override public void celeritasExtra$search(String query) {
        celeritasExtra$query = new OptionSearchQuery(query);
        celeritasExtra$results = -1;
        optionPageScrollBarOffset.set(0);
        tabFrameScrollBarOffset.set(0);
        // Rebuild frames, retaining the same Option objects. Apply/Undo still visit ALL pages.
        init(width, height);
    }

    @Override public Dim2i celeritasExtra$searchBounds() { return celeritasExtra$bounds; }
    @Override public int celeritasExtra$optionCount() {
        return pages.stream().mapToInt(page -> page.getOptions().size()).sum();
    }
    @Override public boolean celeritasExtra$hasChanges() {
        return pages.stream().flatMap(page -> page.getOptions().stream()).anyMatch(Option::hasChanged);
    }
    @Override public int celeritasExtra$resultCount() {
        if (celeritasExtra$results < 0) celeritasExtra$results = (int) pages.stream()
                .flatMap(page -> page.getOptions().stream()).filter(this::celeritasExtra$matches).count();
        return celeritasExtra$results;
    }

    @Unique private boolean celeritasExtra$matches(Option<?> option) {
        if (celeritasExtra$query.isEmpty()) return true;
        return celeritasExtra$query.matches(font.extractString(option.getName()) + "\n"
                + font.extractString(option.getTooltip()) + "\n" + option.getName()
                + "\n" + (option.getId() == null ? "" : option.getId()));
    }

    @Inject(method = "parentBasicFrameBuilder", at = @At("HEAD"))
    private void celeritasExtra$searchLayout(Dim2i parent, Dim2i tabs, CallbackInfoReturnable<BasicFrame.Builder> cir) {
        int fieldHeight = Math.max(10, Math.min(18, tabs.y() - 4));
        celeritasExtra$bounds = new Dim2i(tabs.x(), Math.max(2, tabs.y() - fieldHeight - 4), tabs.width(), fieldHeight);
    }

    @Inject(method = "createTabFrame", at = @At("HEAD"), cancellable = true)
    private void celeritasExtra$filterOptions(Dim2i bounds, CallbackInfoReturnable<AbstractFrame> cir) {
        if (celeritasExtra$query.isEmpty()) return;
        Map<String, List<Tab<?>>> tabs = new LinkedHashMap<>();
        for (OptionPage page : pages) {
            if (page.getOptions().stream().noneMatch(this::celeritasExtra$matches)) continue;
            tabs.computeIfAbsent(page.getId().getModId(), ignored -> new ArrayList<>())
                    .add(Tab.from(page, this::celeritasExtra$matches, optionPageScrollBarOffset));
        }
        cir.setReturnValue(TabFrame.createBuilder().setDimension(bounds).shouldRenderOutline(false)
                .setTabSectionScrollBarOffset(tabFrameScrollBarOffset).setTabSectionSelectedTab(tabFrameSelectedTab)
                .addTabs(result -> result.putAll(tabs)).onSetTab(() -> optionPageScrollBarOffset.set(0)).build(font));
    }
}
