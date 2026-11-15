/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import org.taumc.celeritas.api.options.structure.Option;

/** Legacy equivalent of Sodium's default-value capability for third-party options. */
public interface DefaultValueOption<T> extends Option<T> {

    T getDefaultValue();

}
