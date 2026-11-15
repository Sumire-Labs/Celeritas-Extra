/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */

package com.sumirelabs.celeritasextra.client.gui;

import java.util.List;
import java.util.function.Function;

import com.sumirelabs.celeritasextra.client.gui.search.SearchIndex;
import com.sumirelabs.celeritasextra.client.gui.search.SearchNormalizer;
import com.sumirelabs.celeritasextra.client.gui.search.SearchResult;

/** RSO 2.2.5's search engine and screen defaults, adapted to legacy option objects. */
public final class OptionSearchQuery {

    private final String query;

    public OptionSearchQuery(String query) {
        this.query = query == null ? "" : query;
    }

    public boolean isEmpty() {
        return new SearchNormalizer(true).normalize(query).isEmpty();
    }

    public <T> List<SearchResult<T>> rank(List<T> items, Function<T, String> text) {
        return results(index(items, text));
    }

    public static <T> SearchIndex<T> index(List<T> items, Function<T, String> text) {
        return SearchIndex.builder((T item) -> clean(text.apply(item)))
            .addAll(items).foldDiacritics(true).maxResults(15).minScore(0.3)
            .rerankWithEditDistance(true).rerankLimit(50).rerankWeight(0.1).build();
    }

    public <T> List<SearchResult<T>> results(SearchIndex<T> index) {
        return index.newSession(query).results();
    }

    public boolean matches(String text) {
        return isEmpty() || !rank(List.of(text == null ? "" : text), Function.identity()).isEmpty();
    }

    private static String clean(String text) {
        return text == null ? "" : text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }

}
