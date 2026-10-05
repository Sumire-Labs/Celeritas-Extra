package jp.s12kuma01.celeritasextra.compat.actinium;

import dhj.embeddedt.embeddium.api.OptionGUIConstructionEvent;
import dhj.embeddedt.embeddium.api.OptionGroupConstructionEvent;
import jp.s12kuma01.celeritasextra.compat.actinium.gui.CeleritasExtraOptionsListener;

/** Direct integration with Actinium alpha-0.0.12's relocated option API. */
public final class ActiniumOptionsAdapter {
    private ActiniumOptionsAdapter() { }

    public static void register() {
        OptionGUIConstructionEvent.BUS.addListener(CeleritasExtraOptionsListener::onCeleritasOptionsConstruct);
        // Actinium's native FULLSCREEN_MODE differs from the legacy FULLSCREEN
        // identifier, so the shared listener preserves it and enhances VSync only.
        OptionGroupConstructionEvent.BUS.addListener(CeleritasExtraOptionsListener::onOptionGroupConstruct);
    }
}
