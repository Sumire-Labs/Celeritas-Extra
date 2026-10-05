package jp.s12kuma01.celeritasextra.compat.actinium;

import dhj.embeddedt.embeddium.api.options.OptionIdentifier;
import dhj.embeddedt.embeddium.api.options.control.SliderControl;
import dhj.embeddedt.embeddium.api.options.structure.*;
import dhj.embeddedt.embeddium.impl.gui.framework.TextComponent;
import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraGameOptions;
import jp.s12kuma01.celeritasextra.compat.actinium.gui.CeleritasExtraGameOptionPages;
import net.minecraft.client.resources.I18n;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActiniumOptionVisibilityTest {
    @Test void nativeCloudOptionReplacesOnlyTheDuplicateAndConstructionScopeIsRestored() throws Exception {
        var configField = CeleritasExtraClientMod.class.getDeclaredField("CONFIG");
        configField.setAccessible(true);
        Object previousConfig = configField.get(null);
        var localeField = Arrays.stream(I18n.class.getDeclaredFields())
                .filter(f -> f.getType() == net.minecraft.client.resources.Locale.class).findFirst().orElseThrow();
        localeField.setAccessible(true);
        Object previousLocale = localeField.get(null);
        try {
            configField.set(null, new CeleritasExtraGameOptions());
            localeField.set(null, new net.minecraft.client.resources.Locale());
            ActiniumOptionsAdapter.register();
            var nativeClouds = nativeCloudPage();
            ActiniumOptionsAdapter.withNativeOptions(List.of(nativeClouds), () -> {
                var extra = CeleritasExtraGameOptionPages.clouds();
                assertFalse(hasOption(extra, "options.renderclouds"));
                assertTrue(hasOption(extra, "sodium-extra.option.cloud_distance"));
                assertTrue(hasOption(extra, "celeritasextra.option.modern_clouds"));
                assertTrue(extra.getGroups().stream().noneMatch(g -> g.getOptions().isEmpty()));
                assertEquals(2, nativeClouds.getOptions().size());
                var misc = CeleritasExtraGameOptionPages.misc();
                assertFalse(hasOption(misc, "celeritasextra.option.menu_fps_limit"));
                assertTrue(hasOption(misc, "celeritasextra.option.inactive_fps_limit"));
                assertTrue(hasOption(misc, "celeritasextra.option.minimized_fps_limit"));
            });
            assertTrue(hasOption(CeleritasExtraGameOptionPages.clouds(), "options.renderclouds"));
            assertTrue(hasOption(CeleritasExtraGameOptionPages.misc(), "celeritasextra.option.menu_fps_limit"));
            assertTrue(jp.s12kuma01.celeritasextra.client.gui.CeleritasExtraGameOptionPages.clouds()
                    .getOptions().stream().anyMatch(o -> o.getId().getPath().equals("options.renderclouds")));
            // Weather quality and the ability to disable rain/snow are different features.
            assertTrue(hasOption(CeleritasExtraGameOptionPages.sky(), "soundcategory.weather"));
            assertThrows(IllegalStateException.class, () -> ActiniumOptionsAdapter.withNativeOptions(
                    List.of(nativeClouds), () -> { throw new IllegalStateException("abort"); }));
            assertTrue(hasOption(CeleritasExtraGameOptionPages.clouds(), "options.renderclouds"));
        } finally {
            configField.set(null, previousConfig);
            localeField.set(null, previousLocale);
        }
    }

    private static boolean hasOption(OptionPage page, String path) {
        return page.getOptions().stream().anyMatch(o -> o.getId().getPath().equals(path));
    }

    private static OptionPage nativeCloudPage() {
        var storage = new OptionStorage<int[]>() {
            private final int[] value = {2};
            public int[] getData() { return value; }
        };
        var clouds = OptionImpl.createBuilder(int.class, storage)
                .setId(StandardOptions.Option.CLOUDS.cast())
                .setName(TextComponent.literal("Cloud quality"))
                .setTooltip(TextComponent.literal("Off / fast / fancy"))
                .setControl(option -> new SliderControl(option, 0, 2, 1, value -> TextComponent.literal("" + value)))
                .setBinding((data, value) -> data[0] = value, data -> data[0]).build();
        var menuFps = OptionImpl.createBuilder(int.class, storage)
                .setId(OptionIdentifier.create("actinium", "loading_screen_framerate_limit", int.class))
                .setName(TextComponent.literal("Loading screen FPS"))
                .setTooltip(TextComponent.literal("Menu FPS"))
                .setControl(option -> new SliderControl(option, 30, 240, 10, value -> TextComponent.literal("" + value)))
                .setBinding((data, value) -> data[0] = value, data -> data[0]).build();
        return new OptionPage(OptionIdentifier.create("actinium", "quality"), TextComponent.literal("Quality"),
                List.of(OptionGroup.createBuilder().add(clouds).add(menuFps).build()));
    }
}
