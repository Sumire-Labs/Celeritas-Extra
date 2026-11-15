/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import org.taumc.celeritas.api.options.structure.OptionStorage;

/** Lets an add-on expose factory settings for all its bound options without changing live settings. */
public interface DefaultOptionStorage<S> extends OptionStorage<S> {

    S getDefaultData();

}
