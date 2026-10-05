package com.sumirelabs.celeritasextra.compat;

import com.sumirelabs.celeritasextra.client.gui.CeleritasExtraOptionsListener;
import org.taumc.celeritas.api.OptionGUIConstructionEvent;
import org.taumc.celeritas.api.OptionGroupConstructionEvent;

/** Loaded only when Celeritas is installed. */
public final class CeleritasOptionsAdapter {
    private CeleritasOptionsAdapter() { }

    public static void register() {
        OptionGUIConstructionEvent.BUS.addListener(CeleritasExtraOptionsListener::onCeleritasOptionsConstruct);
        OptionGroupConstructionEvent.BUS.addListener(CeleritasExtraOptionsListener::onOptionGroupConstruct);
    }
}
