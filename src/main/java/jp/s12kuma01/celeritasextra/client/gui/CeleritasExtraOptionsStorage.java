package jp.s12kuma01.celeritasextra.client.gui;

import jp.s12kuma01.celeritasextra.client.CeleritasExtraClientMod;

import org.taumc.celeritas.api.options.structure.OptionStorage;

/**
 * Bridges Celeritas Extra configuration with Celeritas GUI system.
 * Provides access to configuration data and save functionality.
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
