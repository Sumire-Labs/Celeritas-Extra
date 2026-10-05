package jp.s12kuma01.celeritasextra.client.gui;

import org.embeddedt.embeddium.impl.util.Dim2i;

public interface SearchableOptionsController {
    void celeritasExtra$search(String query);
    Dim2i celeritasExtra$searchBounds();
    int celeritasExtra$resultCount();
    int celeritasExtra$optionCount();
    boolean celeritasExtra$hasChanges();
}
