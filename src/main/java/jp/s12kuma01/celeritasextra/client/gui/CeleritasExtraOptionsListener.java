package jp.s12kuma01.celeritasextra.client.gui;

import jp.s12kuma01.celeritasextra.CeleritasExtraMod;
import org.taumc.celeritas.api.OptionGUIConstructionEvent;

/**
 * Listens for Celeritas Options GUI construction events
 * and adds Celeritas Extra option pages
 */
public class CeleritasExtraOptionsListener {

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
}
