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
 * Adapted from Reese's Sodium Options 97fff483846c7a162aeebffd48373c903d730c38.
 * Minecraft rendering, input and Sodium dependencies are adapted to Celeritas 1.12.2.
 */

package com.sumirelabs.celeritasextra.client.gui.rso.frame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.taumc.celeritas.api.options.structure.Option;
import org.taumc.celeritas.api.options.structure.OptionPage;

import com.sumirelabs.celeritasextra.client.gui.OptionSections;
import com.sumirelabs.celeritasextra.client.gui.OptionsCollapseState;

/** RSO page/search rows with Extra-owned section metadata for the legacy Celeritas API. */
public record PageLayout(List<Row> rows, int contentHeight) {

    public record Row(Option<?> option, OptionSections.Section section, int y) {

        public Row(Option<?> option, int y) {
            this(option, null, y);
        }

    }
    public static PageLayout create(OptionPage page, Predicate<Option<?>> filter, List<Option<?>> ranked) {
        return create(page, filter, ranked, new OptionsCollapseState(), ranked != null);
    }
    public static PageLayout create(OptionPage page, Predicate<Option<?>> filter, List<Option<?>> ranked,
        OptionsCollapseState state, boolean searching) {
        var rows = new ArrayList<Row>();
        int y = 0;
        if (ranked != null) {
            for (Option<?> option : ranked) {
                rows.add(new Row(option, y));
                y += 18;
            }
        } else {
            var emitted = new java.util.HashSet<String>();
            int[] position = {0};
            for (var group : page.getGroups()) {
                var options = group.getOptions().stream().filter(filter).toList();
                if (options.isEmpty())
                    continue;
                int before = rows.size(), oldY = position[0];
                if (before > 0)
                    position[0] += 4;
                var section = OptionSections.of(group);
                if (section == null || emit(section, rows, position, emitted, state, searching))
                    for (var option : options) {
                        rows.add(new Row(option, position[0]));
                        position[0] += 18;
                    }
                if (rows.size() == before)
                    position[0] = oldY;
            }
            y = position[0];
        }
        return new PageLayout(List.copyOf(rows), y);
    }
    private static boolean emit(OptionSections.Section section, List<Row> rows, int[] y, java.util.Set<String> emitted,
        OptionsCollapseState state, boolean searching) {
        if (section.parent() != null && !emit(section.parent(), rows, y, emitted, state, searching))
            return false;
        if (emitted.add(section.id())) {
            rows.add(new Row(null, section, y[0]));
            y[0] += 18;
        }
        return searching || !state.groupCollapsed(section);
    }

}
