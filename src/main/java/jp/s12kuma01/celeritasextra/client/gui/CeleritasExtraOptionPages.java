package jp.s12kuma01.celeritasextra.client.gui;

import org.taumc.celeritas.api.options.OptionIdentifier;

/**
 * Holds the {@link OptionIdentifier} constants that identify the option pages
 * contributed by Celeritas Extra.
 * <p>
 * Each identifier pairs the {@code celeritasextra} namespace with a page key and
 * serves as a stable handle for the extra pages when they are registered into the
 * Celeritas options GUI by {@link CeleritasExtraOptionsListener}. The pages cover
 * animation, particle, detail, render, and miscellaneous ("extra") settings.
 */
public class CeleritasExtraOptionPages {
    public static final OptionIdentifier<Void> ANIMATION = OptionIdentifier.create("celeritasextra", "animation");
    public static final OptionIdentifier<Void> PARTICLE = OptionIdentifier.create("celeritasextra", "particle");
    public static final OptionIdentifier<Void> DETAILS = OptionIdentifier.create("celeritasextra", "details");
    public static final OptionIdentifier<Void> RENDER = OptionIdentifier.create("celeritasextra", "render");
    public static final OptionIdentifier<Void> EXTRA = OptionIdentifier.create("celeritasextra", "extra");
}
