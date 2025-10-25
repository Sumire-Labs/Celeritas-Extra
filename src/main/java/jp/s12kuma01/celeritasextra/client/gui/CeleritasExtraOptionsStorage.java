package jp.s12kuma01.celeritasextra.client.gui;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;
import org.taumc.celeritas.api.options.structure.OptionStorage;

/**
 * Option storage implementation for Celeritas Extra
 * Provides access to configuration options
 */
public class CeleritasExtraOptionsStorage implements OptionStorage<CeleritasExtraGameOptions> {

    @Override
    public CeleritasExtraGameOptions getData() {
        return CeleritasExtraClientMod.options();
    }

    @Override
    public void save() {
        CeleritasExtraClientMod.options().writeChanges();
    }
}
