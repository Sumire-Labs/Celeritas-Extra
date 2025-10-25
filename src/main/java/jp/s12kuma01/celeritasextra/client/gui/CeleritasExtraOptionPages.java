package jp.s12kuma01.celeritasextra.client.gui;

import org.taumc.celeritas.api.options.OptionIdentifier;

/**
 * Defines option page identifiers for Celeritas Extra
 */
public class CeleritasExtraOptionPages {
    public static final OptionIdentifier<Void> ANIMATION = OptionIdentifier.create("celeritasextra", "animation");
    public static final OptionIdentifier<Void> PARTICLE = OptionIdentifier.create("celeritasextra", "particle");
    public static final OptionIdentifier<Void> DETAILS = OptionIdentifier.create("celeritasextra", "details");
    public static final OptionIdentifier<Void> RENDER = OptionIdentifier.create("celeritasextra", "render");
    public static final OptionIdentifier<Void> EXTRA = OptionIdentifier.create("celeritasextra", "extra");
}
