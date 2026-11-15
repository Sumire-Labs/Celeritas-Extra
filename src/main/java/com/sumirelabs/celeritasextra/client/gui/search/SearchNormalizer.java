/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 FlashyReese
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * Adapted from Reese's Sodium Options mc26.3-2.2.5 (7093d1d).
 * Package renamed; Minecraft identifier character check adapted for 1.12.2.
 */

package com.sumirelabs.celeritasextra.client.gui.search;

import java.text.Normalizer;
import java.util.Locale;

public final class SearchNormalizer {

    private final boolean foldDiacritics;

    public SearchNormalizer(boolean foldDiacritics) {
        this.foldDiacritics = foldDiacritics;
    }

    public String normalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKC)
            .toLowerCase(Locale.ROOT);
        if (this.foldDiacritics) {
            normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        }
        StringBuilder builder = new StringBuilder(normalized.length());
        boolean lastWasSpace = false;
        int length = normalized.length();
        for (int offset = 0; offset < length;) {
            int codePoint = normalized.codePointAt(offset);
            int type = Character.getType(codePoint);
            boolean isSpace = Character.isWhitespace(codePoint)
                || type == Character.SPACE_SEPARATOR
                || type == Character.LINE_SEPARATOR
                || type == Character.PARAGRAPH_SEPARATOR;
            boolean isPunctuation = type == Character.CONNECTOR_PUNCTUATION
                || type == Character.DASH_PUNCTUATION
                || type == Character.START_PUNCTUATION
                || type == Character.END_PUNCTUATION
                || type == Character.OTHER_PUNCTUATION
                || type == Character.INITIAL_QUOTE_PUNCTUATION
                || type == Character.FINAL_QUOTE_PUNCTUATION;
            if ((isSpace || isPunctuation)) {
                if (!lastWasSpace) {
                    builder.append(' ');
                    lastWasSpace = true;
                }
            } else {
                builder.appendCodePoint(codePoint);
                lastWasSpace = false;
            }
            offset += Character.charCount(codePoint);
        }
        String result = builder.toString().trim();
        return result.isEmpty() ? "" : result;
    }

}
