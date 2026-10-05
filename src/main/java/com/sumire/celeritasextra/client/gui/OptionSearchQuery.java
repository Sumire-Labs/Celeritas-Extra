package com.sumire.celeritasextra.client.gui;

import java.text.Normalizer;
import java.util.Locale;

/** All query terms must occur in the option's searchable text. */
public final class OptionSearchQuery {
    private final String[] terms;

    public OptionSearchQuery(String query) {
        String normalized = normalize(query).trim();
        terms = normalized.isEmpty() ? new String[0] : normalized.split("\\s+");
    }

    public boolean isEmpty() { return terms.length == 0; }

    public boolean matches(String text) {
        String normalized = normalize(text);
        for (String term : terms) if (!normalized.contains(term)) return false;
        return true;
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFKC)
                .replaceAll("§[0-9a-fk-orA-FK-OR]", "").toLowerCase(Locale.ROOT);
    }
}
