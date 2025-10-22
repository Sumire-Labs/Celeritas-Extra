package jp.s12kuma01.celeritasextra.client.gui;

import jp.s12kuma01.celeritasextra.CeleritasExtraMod;

import org.taumc.celeritas.api.OptionGUIConstructionEvent;

/**
 * Listener for Celeritas GUI construction events.
 * Registers Celeritas Extra option pages to Celeritas settings menu.
 */
public class CeleritasExtraOptionsListener {

    /**
     * Called when Celeritas GUI is being constructed.
     * Adds all Celeritas Extra option pages to the GUI.
     */
    public static void onCeleritasOptionsConstruct(OptionGUIConstructionEvent event) {
        CeleritasExtraMod.LOGGER.info("Registering Celeritas Extra options pages");

        event.addPage(CeleritasExtraGameOptionPages.animation());
        event.addPage(CeleritasExtraGameOptionPages.particle());
        event.addPage(CeleritasExtraGameOptionPages.details());
        event.addPage(CeleritasExtraGameOptionPages.render());
        event.addPage(CeleritasExtraGameOptionPages.extra());

        CeleritasExtraMod.LOGGER.info("Successfully registered {} Celeritas Extra option pages", 5);
    }
}
