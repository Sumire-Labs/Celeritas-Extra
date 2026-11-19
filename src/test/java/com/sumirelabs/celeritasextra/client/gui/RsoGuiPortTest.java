/*
 * Copyright (c) 2026 Pepelys
 * Copyright (c) 2026 Sumire Labs
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.sumirelabs.celeritasextra.client.gui;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.embeddedt.embeddium.impl.gui.frame.AbstractFrame;
import org.embeddedt.embeddium.impl.gui.framework.InteractionContext;
import org.embeddedt.embeddium.impl.util.Dim2i;
import org.junit.jupiter.api.Test;

import com.sumirelabs.celeritasextra.client.gui.rso.frame.ScrollFrameLayout;
import com.sumirelabs.celeritasextra.client.gui.rso.frame.ScrollableFrame;
import com.sumirelabs.celeritasextra.client.gui.rso.layout.LayoutBounds;
import com.sumirelabs.celeritasextra.client.gui.rso.layout.ScreenLayout;
import com.sumirelabs.celeritasextra.client.gui.rso.widget.ScrollBarWidget;

import static org.junit.jupiter.api.Assertions.*;

class RsoGuiPortTest {

    @Test
    void nativeParentsKeepTheClickedControlCapturedUntilRelease() {
        var lastY = new AtomicInteger();
        var releases = new AtomicInteger();
        var offset = new AtomicReference<>(0);
        var scroll = new ScrollableFrame(new Dim2i(0, 0, 200, 100), 400, offset, dim -> new AbstractFrame(dim, false) {

            {
                children.add(new org.embeddedt.embeddium.impl.gui.widgets.AbstractWidget() {

                    public void render(org.embeddedt.embeddium.impl.gui.framework.DrawContext context, int x, int y,
                        float delta) {}
                    public boolean isMouseOver(double x, double y) {
                        return x >= 10 && x < 180 && y >= 10 && y < 28;
                    }
                    public boolean mouseClicked(InteractionContext c, double x, double y, int b) {
                        return b == 0 && isMouseOver(x, y);
                    }
                    public boolean mouseDragged(InteractionContext c, double x, double y, int b, double dx, double dy) {
                        lastY.set((int) y);
                        return true;
                    }
                    public boolean mouseReleased(InteractionContext c, double x, double y, int b) {
                        releases.incrementAndGet();
                        return true;
                    }

                });
                buildFrame();
            }

        });
        var parent = new AbstractFrame(new Dim2i(0, 0, 200, 100), false) {

            {
                children.add(scroll);
                buildFrame();
            }

        };
        var input = new InteractionContext() {
        };
        assertTrue(parent.mouseClicked(input, 30, 15, 0));
        assertTrue(parent.mouseDragged(input, 30, 1000, 0, 0, 985));
        assertEquals(1000, lastY.get());
        parent.mouseReleased(input, 30, 1000, 0);
        assertEquals(1, releases.get());
        assertFalse(parent.mouseDragged(input, 30, 1000, 0, 0, 1));
    }

    @Test
    void nativeParentsDeliverScrollbarDragsAndReleaseOutsideTheViewport() {
        var offset = new AtomicReference<>(0);
        var scroll = new ScrollableFrame(new Dim2i(0, 0, 200, 100), 400, offset, dim -> new AbstractFrame(dim, false) {
        });
        var parent = new AbstractFrame(new Dim2i(0, 0, 200, 100), false) {

            {
                children.add(scroll);
                buildFrame();
            }

        };
        var input = new InteractionContext() {
        };
        assertTrue(parent.mouseClicked(input, 195, 5, 0));
        assertTrue(parent.mouseDragged(input, 195, 1000, 0, 0, 995));
        assertEquals(300, offset.get());
        parent.mouseReleased(input, 195, 1000, 0);
        assertFalse(parent.mouseDragged(input, 195, -1000, 0, 0, -2000));
        assertEquals(300, offset.get());
    }

    @Test
    void upstreamWideLayoutAndLegacyNarrowScalesKeepContentVisible() {
        var wide = ScreenLayout.create(960, 240);
        assertEquals(426, wide.root().width());
        assertEquals(405, wide.tabs().width());
        assertEquals(20, wide.search().height());
        assertTrue(wide.search().getLimitY() < wide.tabs().y());
        assertEquals(960 / 2, wide.root().getCenterX());
        int rail = ScreenLayout.sidebarWidth(500, 160, true);
        assertEquals(80, rail);
        assertEquals(220, ScreenLayout.sidebarWidth(500, 1200, false));
    }
    @Test
    void upstreamViewportReservesTheScrollTrackAndKeepsSelectedRowsInView() {
        var view = new LayoutBounds(10, 20, 200, 100);
        var layout = ScrollFrameLayout.create(view, view.withHeight(400));
        assertTrue(layout.canScrollVertical());
        assertEquals(189, layout.contentWidth());
        assertEquals(new LayoutBounds(200, 20, 10, 100), layout.verticalScrollBarBounds());
        assertEquals(108, ScrollFrameLayout.scrollIntoViewOffset(view, new LayoutBounds(10, 110, 100, 18), 100));
    }
    @Test
    void clippedContentNeverReceivesClicksAndTranslatedSlidersGetLocalCoordinates() {
        var clicks = new AtomicInteger();
        var lastY = new AtomicInteger();
        var offset = new AtomicReference<>(60);
        var frame = new ScrollableFrame(new Dim2i(10, 20, 200, 100), 400, offset, dim -> new AbstractFrame(dim, false) {

            @Override
            public boolean mouseClicked(InteractionContext c, double x, double y, int b) {
                clicks.incrementAndGet();
                lastY.set((int) y);
                return true;
            }

        });
        var input = new InteractionContext() {
        };
        assertFalse(frame.mouseClicked(input, 30, 19, 0));
        assertFalse(frame.mouseClicked(input, 30, 121, 0));
        assertEquals(0, clicks.get());
        assertTrue(frame.mouseClicked(input, 30, 25, 0));
        assertEquals(85, lastY.get());
        assertEquals(1, clicks.get());
    }
    @Test
    void scrollbarCanReachBothEndsAndReleasesItsDragOutsideTheViewport() {
        var offset = new AtomicInteger();
        var input = new InteractionContext() {
        };
        var bar = new ScrollBarWidget(new Dim2i(100, 20, 10, 100), 400, 100, new Dim2i(0, 20, 100, 100), 0,
            offset::set);
        assertTrue(bar.mouseClicked(input, 105, 118, 0));
        bar.mouseDragged(input, 105, 1000, 0, 0, 882);
        assertEquals(300, offset.get());
        bar.mouseReleased(input, 105, 1000, 0);
        assertFalse(bar.mouseDragged(input, 105, 20, 0, 0, -980));
        assertEquals(300, offset.get());
        assertTrue(bar.mouseScrolled(input, 30, 30, 0, 100));
        assertEquals(0, offset.get());
    }

}
