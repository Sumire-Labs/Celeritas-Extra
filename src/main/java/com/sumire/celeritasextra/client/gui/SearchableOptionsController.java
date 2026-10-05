package com.sumire.celeritasextra.client.gui;

import org.embeddedt.embeddium.impl.util.Dim2i;
import org.embeddedt.embeddium.impl.gui.framework.DrawContext;

public interface SearchableOptionsController {
    void celeritasExtra$search(String query);
    Dim2i celeritasExtra$searchBounds();
    int celeritasExtra$resultCount();
    int celeritasExtra$optionCount();
    boolean celeritasExtra$hasChanges();
    DrawContext celeritasExtra$drawContext();
}
