package jp.s12kuma01.celeritasextra.client.gui;

import jp.s12kuma01.celeritasextra.CeleritasExtraMod;
import net.minecraft.client.resources.I18n;
import org.embeddedt.embeddium.impl.gui.framework.TextComponent;
import org.taumc.celeritas.api.OptionGUIConstructionEvent;
import org.taumc.celeritas.api.OptionGroupConstructionEvent;
import org.taumc.celeritas.api.options.control.CyclingControl;
import org.taumc.celeritas.api.options.structure.Option;
import org.taumc.celeritas.api.options.structure.OptionImpl;
import org.taumc.celeritas.api.options.structure.OptionImpact;
import org.taumc.celeritas.api.options.structure.StandardOptions;

import java.util.List;

/**
 * Listens for Celeritas Options GUI construction events
 * and adds Celeritas Extra option pages
 */
public class CeleritasExtraOptionsListener {

    private static final CeleritasExtraOptionsStorage celeritasExtraOpts = new CeleritasExtraOptionsStorage();

    public static void onCeleritasOptionsConstruct(OptionGUIConstructionEvent event) {
        CeleritasExtraMod.LOGGER.info("Registering Celeritas Extra options pages");

        // Register all Celeritas Extra option pages
        event.addPage(CeleritasExtraGameOptionPages.animation());
        event.addPage(CeleritasExtraGameOptionPages.particle());
        event.addPage(CeleritasExtraGameOptionPages.details());
        event.addPage(CeleritasExtraGameOptionPages.render());
        event.addPage(CeleritasExtraGameOptionPages.extra());

        CeleritasExtraMod.LOGGER.info("Successfully registered {} Celeritas Extra option pages", 5);
    }

    /**
     * Replaces vanilla boolean toggles in the WINDOW group with enhanced cycling controls:
     * - Fullscreen → 3-way Screen Mode (Windowed / Borderless / Fullscreen)
     * - VSync → 3-way VSync (Off / On / Adaptive)
     */
    public static void onOptionGroupConstruct(OptionGroupConstructionEvent event) {
        if (!event.getId().matches(StandardOptions.Group.WINDOW)) {
            return;
        }

        List<Option<?>> options = event.getOptions();
        for (int i = 0; i < options.size(); i++) {
            Option<?> option = options.get(i);
            if (option.getId() == null) continue;

            if (option.getId().matches(StandardOptions.Option.FULLSCREEN)) {
                options.set(i, createScreenModeOption());
                CeleritasExtraMod.LOGGER.debug("Replaced Fullscreen option with screen mode control");
            } else if (option.getId().matches(StandardOptions.Option.VSYNC)) {
                options.set(i, createVSyncOption());
                CeleritasExtraMod.LOGGER.debug("Replaced VSync option with adaptive VSync control");
            }
        }
    }

    private static OptionImpl<CeleritasExtraGameOptions, CeleritasExtraGameOptions.ScreenMode> createScreenModeOption() {
        return OptionImpl.createBuilder(CeleritasExtraGameOptions.ScreenMode.class, celeritasExtraOpts)
                .setId(StandardOptions.Option.FULLSCREEN.cast())
                .setName(TextComponent.literal(I18n.format("celeritasextra.option.screen_mode")))
                .setTooltip(TextComponent.literal(
                        I18n.format("celeritasextra.option.screen_mode.tooltip")))
                .setControl(option -> new CyclingControl<>(option, CeleritasExtraGameOptions.ScreenMode.class,
                        CeleritasExtraGameOptions.ScreenMode.values()))
                .setBinding(CeleritasExtraGameOptions::setScreenMode,
                        CeleritasExtraGameOptions::getScreenMode)
                .build();
    }

    private static OptionImpl<CeleritasExtraGameOptions, CeleritasExtraGameOptions.VerticalSyncOption> createVSyncOption() {
        return OptionImpl.createBuilder(CeleritasExtraGameOptions.VerticalSyncOption.class, celeritasExtraOpts)
                .setId(StandardOptions.Option.VSYNC.cast())
                .setName(TextComponent.translatable("options.vsync"))
                .setTooltip(TextComponent.literal(
                        I18n.format("celeritasextra.option.extra.vertical_sync.tooltip")))
                .setControl(option -> new CyclingControl<>(option, CeleritasExtraGameOptions.VerticalSyncOption.class,
                        CeleritasExtraGameOptions.VerticalSyncOption.getAvailableOptions()))
                .setBinding(CeleritasExtraGameOptions::setVerticalSyncOption,
                        CeleritasExtraGameOptions::getVerticalSyncOption)
                .setImpact(OptionImpact.VARIES)
                .build();
    }
}
