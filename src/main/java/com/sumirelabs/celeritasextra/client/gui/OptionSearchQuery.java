/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import java.text.Normalizer;
import java.util.Locale;

/** Scores exact names before partial matches and conservative Latin typo matches. */
public final class OptionSearchQuery {

    private final String[] terms;

    public OptionSearchQuery(String query) {
        String normalized = normalize(query).trim();
        terms = normalized.isEmpty() ? new String[0] : normalized.split("\\s+");
    }

    public boolean isEmpty() {
        return terms.length == 0;
    }

    public boolean matches(String text) {
        return score(text, "") > 0;
    }

    public int score(String name, String details) {
        if (isEmpty())
            return 1;
        String title = normalize(name);
        String body = normalize(details);
        int total = 0;
        for (String term : terms) {
            if (title.equals(term))
                total += 1000;
            else if (title.contains(term))
                total += 600;
            else if (body.contains(term))
                total += 300;
            else {
                int fuzzy = Math.max(typoScore(term, title), typoScore(term, body) / 2);
                if (fuzzy == 0)
                    return 0;
                total += fuzzy;
            }
        }
        return total;
    }

    private static int typoScore(String term, String text) {
        // Short words and Japanese substrings need exact matching to avoid unrelated results.
        if (term.length() < 4 || term.length() > 64 || !term.matches("[a-z0-9_-]+"))
            return 0;
        int allowed = term.length() >= 8 ? 2 : 1;
        for (String word : text.split("[^a-z0-9_-]+")) {
            if (Math.abs(term.length() - word.length()) > allowed)
                continue;
            int[] previous = new int[word.length() + 1];
            for (int j = 0; j <= word.length(); j++)
                previous[j] = j;
            int[] beforePrevious = previous;
            for (int i = 1; i <= term.length(); i++) {
                int[] current = new int[word.length() + 1];
                current[0] = i;
                for (int j = 1; j <= word.length(); j++) {
                    current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + (term.charAt(i - 1) == word.charAt(j - 1) ? 0 : 1));
                    if (i > 1 && j > 1 && term.charAt(i - 1) == word.charAt(j - 2)
                        && term.charAt(i - 2) == word.charAt(j - 1))
                        current[j] = Math.min(current[j], beforePrevious[j - 2] + 1);
                }
                beforePrevious = previous;
                previous = current;
            }
            if (previous[word.length()] <= allowed)
                return 100;
        }
        return 0;
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFKC)
            .replaceAll("§[0-9a-fk-orA-FK-OR]", "").toLowerCase(Locale.ROOT);
    }

}
