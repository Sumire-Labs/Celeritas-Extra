/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import org.taumc.celeritas.api.options.OptionIdentifier;

/**
 * Holds the {@link OptionIdentifier} constants that identify the option pages
 * contributed by Celeritas Extra.
 * <p>
 * Each identifier pairs the {@code celeritasextra} namespace with a page key and
 * serves as a stable handle for the extra pages when they are registered into the
 * Celeritas options GUI by {@link CeleritasExtraOptionsListener}. The pages cover
 * animations, particles, details, rendering, and extras. Legacy page identifiers remain available
 * for callers of the individual page factories.
 */
public class CeleritasExtraOptionPages {

    public static final OptionIdentifier<Void> ANIMATION = OptionIdentifier.create("celeritasextra", "animation");
    public static final OptionIdentifier<Void> PARTICLE = OptionIdentifier.create("celeritasextra", "particle");
    public static final OptionIdentifier<Void> DETAILS = OptionIdentifier.create("celeritasextra", "details");
    public static final OptionIdentifier<Void> RENDER = OptionIdentifier.create("celeritasextra", "render");
    public static final OptionIdentifier<Void> EXTRAS = OptionIdentifier.create("celeritasextra", "extra");
    public static final OptionIdentifier<Void> SKY = OptionIdentifier.create("celeritasextra", "sky");
    public static final OptionIdentifier<Void> CLOUDS = OptionIdentifier.create("celeritasextra", "clouds");
    public static final OptionIdentifier<Void> FOG = OptionIdentifier.create("celeritasextra", "fog");
    public static final OptionIdentifier<Void> DIMENSION_FOG = OptionIdentifier.create("celeritasextra",
        "dimension_fog");
    public static final OptionIdentifier<Void> ENTITIES = OptionIdentifier.create("celeritasextra", "entities");
    public static final OptionIdentifier<Void> BLOCKS = OptionIdentifier.create("celeritasextra", "blocks");
    public static final OptionIdentifier<Void> OVERLAY = OptionIdentifier.create("celeritasextra", "overlay");
    public static final OptionIdentifier<Void> MISC = EXTRAS;

}
