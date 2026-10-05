package jp.s12kuma01.celeritasextra.client.gui;

import jp.s12kuma01.celeritasextra.mixin.options_search.MixinOptionsController;
import jp.s12kuma01.celeritasextra.mixin.options_search.MixinSliderScroll;
import org.embeddedt.embeddium.impl.gui.frame.AbstractFrame;
import org.embeddedt.embeddium.impl.gui.framework.*;
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.taumc.celeritas.api.options.OptionIdentifier;
import org.taumc.celeritas.api.options.control.*;
import org.taumc.celeritas.api.options.structure.*;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

/** Exercises Celeritas's real TabFrame/OptionPageFrame builders without an OpenGL window. */
class OptionsGuiIntegrationTest {
    @Test void groupHeaderCollapseKeepsPendingOptionsAndSearchTemporarilyExpandsThem() throws Exception {
        var sun = new TestOption("Sun", "Render sun");
        var moon = new TestOption("Moon", "Render moon");
        var fog = new TestOption("Fog", "Distance");
        var first = OptionGroup.createBuilder().add(sun).add(moon).build();
        var second = OptionGroup.createBuilder().add(fog).build();
        var page = new OptionPage(OptionIdentifier.create("test", "collapsible"), TextComponent.literal("Sky"), List.of(first, second));
        var state = new OptionsCollapseState();
        var rebuilds = new java.util.concurrent.atomic.AtomicInteger();
        var bounds = new Dim2i(10, 20, 200, 100);
        var frame = new CollapsibleOptionPageFrame(bounds, page, option -> true, state, false, rebuilds::incrementAndGet);
        sun.setValue(9);
        assertEquals(3, controls(frame).size());
        assertTrue(frame.mouseClicked(new InteractionContext(){}, 15, 25, 0));
        assertEquals(1, rebuilds.get());
        var collapsed = new CollapsibleOptionPageFrame(bounds, page, option -> true, state, false, rebuilds::incrementAndGet);
        assertEquals(List.of(fog), controls(collapsed).stream().map(ControlElement::getOption).toList());
        assertTrue(sun.hasChanged());
        assertEquals(9, sun.getValue());
        assertTrue(collapsed.getDimensions().height() < frame.getDimensions().height());
        var searching = new CollapsibleOptionPageFrame(bounds, page, option -> option == sun, state, true, rebuilds::incrementAndGet);
        assertEquals(List.of(sun), controls(searching).stream().map(ControlElement::getOption).toList());
        assertFalse(searching.mouseClicked(new InteractionContext(){}, 15, 25, 0));
        var restored = new CollapsibleOptionPageFrame(bounds, page, option -> true, state, false, rebuilds::incrementAndGet);
        assertEquals(List.of(fog), controls(restored).stream().map(ControlElement::getOption).toList());
        page.getOptions().forEach(Option::reset);
        assertFalse(sun.hasChanged());
    }

    @Test void modHeadersCollapseTabsWhileRetainingTheSelectedPage() throws Exception {
        var option = new TestOption("Sun", "Render sun");
        var controller = controller(List.of(page("sky", option), page("fog", new TestOption("Fog", "Distance"))));
        var frame = filteredFrame(controller);
        assertTrue(frame instanceof CollapsibleTabFrame);
        // The native sidebar ScrollableFrame transforms coordinates before dispatch.
        assertTrue(frame.mouseClicked(new InteractionContext(){}, 10, 35, 0));
        var collapsed = filteredFrame(controller);
        assertEquals(1, controls(collapsed).size(), "The active page remains visible when its mod rail collapses");
        var stateField = MixinOptionsController.class.getDeclaredField("celeritasExtra$collapse");
        stateField.setAccessible(true);
        var state = (OptionsCollapseState) stateField.get(controller);
        assertTrue(state.modCollapsed("test", false));
        controller.celeritasExtra$search("fog");
        assertFalse(state.modCollapsed("test", true));
        assertEquals("Fog", controls(filteredFrame(controller)).getFirst().getOption().getName().toString());
        controller.celeritasExtra$search("");
        assertTrue(state.modCollapsed("test", false));
    }

    private static final DrawContext FONT = (DrawContext) Proxy.newProxyInstance(DrawContext.class.getClassLoader(),
            new Class<?>[]{DrawContext.class}, (proxy, method, args) -> switch (method.getName()) {
                case "extractString", "substrByWidth" -> args[0].toString();
                case "getStringWidth" -> args[0].toString().length() * 6;
                case "lineHeight" -> 9;
                case "split" -> List.of(args[0]);
                case "getFriendlyModName" -> TextComponent.literal(args[0].toString());
                default -> method.getReturnType() == int.class ? 0 : null;
            });

    @Test void filteringBuildsNativeFramesAndPreservesHiddenPendingValues() throws Exception {
        var sun = new TestOption("Sun", "Render the sun");
        var moon = new TestOption("Moon", "Render the moon");
        var fog = new TestOption("Fog", "Atmospheric distance");
        var pages = List.of(page("sky", sun, moon), page("fog", fog));
        var controller = controller(pages);
        sun.setValue(8);
        controller.celeritasExtra$search("moon");
        assertEquals(1, controller.celeritasExtra$resultCount());
        assertTrue(controller.celeritasExtra$hasChanges(), "Hidden settings remain pending");
        AbstractFrame frame = filteredFrame(controller);
        assertEquals(List.of(moon), controls(frame).stream().map(ControlElement::getOption).toList());
        assertEquals(8, sun.getValue());
        assertEquals(3, controller.celeritasExtra$optionCount());
        controller.celeritasExtra$search("");
        assertEquals(3, controller.celeritasExtra$resultCount());
        assertEquals(8, sun.getValue());
        pages.stream().flatMap(p -> p.getOptions().stream()).forEach(Option::reset);
        assertFalse(controller.celeritasExtra$hasChanges());
    }

    @Test void noResultsBuildsAnEmptyNativeFrameRatherThanThrowing() throws Exception {
        var controller = controller(List.of(page("sky", new TestOption("Sun", "Render sun"))));
        controller.celeritasExtra$search("does not exist");
        assertEquals(0, controller.celeritasExtra$resultCount());
        assertTrue(controls(filteredFrame(controller)).isEmpty());
    }

    @Test void shiftWheelUsesIntervalAndDoesNotChangeDisabledOrUnhoveredSliders() throws Exception {
        var option = new TestOption("Distance", "Blocks");
        var slider = new MixinSliderScroll(option, new Dim2i(0, 0, 200, 20)) {};
        set(MixinSliderScroll.class, slider, "min", 0);
        set(MixinSliderScroll.class, slider, "max", 12);
        set(MixinSliderScroll.class, slider, "interval", 3);
        InteractionContext shift = new InteractionContext() {
            public boolean isSpecialKeyDown(SpecialKey key) { return key == SpecialKey.SHIFT; }
        };
        assertFalse(slider.mouseScrolled(new InteractionContext(){}, 10, 10, 0, 1));
        assertFalse(slider.mouseScrolled(shift, 210, 10, 0, 1));
        assertTrue(slider.mouseScrolled(shift, 10, 10, 0, 1));
        assertEquals(6, option.getValue());
        option.available = false;
        assertFalse(slider.mouseScrolled(shift, 10, 10, 0, 1));
        assertEquals(6, option.getValue());
        option.available = true;
        option.setValue(12);
        assertTrue(slider.mouseScrolled(shift, 10, 10, 0, 1), "Boundary scroll must be consumed");
        assertEquals(12, option.getValue());
        assertEquals(Integer.MAX_VALUE, SliderScroll.adjust(Integer.MAX_VALUE, 0, Integer.MAX_VALUE, 100, 1));
    }

    private static MixinOptionsController controller(List<OptionPage> pages) throws Exception {
        var controller = new MixinOptionsController() { public void init(int width, int height) {} };
        set(MixinOptionsController.class, controller, "pages", pages);
        set(MixinOptionsController.class, controller, "font", FONT);
        set(MixinOptionsController.class, controller, "optionPageScrollBarOffset", new AtomicReference<>(0));
        set(MixinOptionsController.class, controller, "tabFrameScrollBarOffset", new AtomicReference<>(0));
        set(MixinOptionsController.class, null, "tabFrameSelectedTab", new AtomicReference<TextComponent>());
        return controller;
    }
    private static AbstractFrame filteredFrame(MixinOptionsController controller) throws Exception {
        Method method = MixinOptionsController.class.getDeclaredMethod("celeritasExtra$filterOptions", Dim2i.class, CallbackInfoReturnable.class);
        method.setAccessible(true);
        var callback = new CallbackInfoReturnable<AbstractFrame>("test", true);
        method.invoke(controller, new Dim2i(0, 30, 400, 200), callback);
        return callback.getReturnValue();
    }
    @SuppressWarnings("unchecked") private static List<ControlElement<?>> controls(AbstractFrame frame) throws Exception {
        Field field = AbstractFrame.class.getDeclaredField("controlElements");
        field.setAccessible(true);
        return (List<ControlElement<?>>) field.get(frame);
    }
    private static void set(Class<?> type, Object object, String name, Object value) throws Exception {
        Field field = type.getDeclaredField(name); field.setAccessible(true); field.set(object, value);
    }
    private static OptionPage page(String id, TestOption... options) {
        var group = OptionGroup.createBuilder();
        for (var option : options) group.add(option);
        return new OptionPage(OptionIdentifier.create("test", id), TextComponent.literal(id), List.of(group.build()));
    }
    private static final class TestOption implements Option<Integer> {
        private final String name, tooltip;
        private int value = 3;
        private boolean available = true;
        TestOption(String name, String tooltip) { this.name = name; this.tooltip = tooltip; }
        public TextComponent getName() { return TextComponent.literal(name); }
        public TextComponent getTooltip() { return TextComponent.literal(tooltip); }
        public OptionImpact getImpact() { return OptionImpact.LOW; }
        public Control<Integer> getControl() { return new SliderControl(this, 0, 12, 3, ControlValueFormatter.number()); }
        public Integer getValue() { return value; }
        public void setValue(Integer value) { this.value = value; }
        public void reset() { value = 3; }
        public OptionStorage<?> getStorage() { return null; }
        public boolean isAvailable() { return available; }
        public boolean hasChanged() { return value != 3; }
        public void applyChanges() {}
        public Collection<OptionFlag> getFlags() { return List.of(); }
    }
}
