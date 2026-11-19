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

package com.sumirelabs.celeritasextra.client.gui.rso.layout;

public record LayoutBounds(int x, int y, int width, int height) {

    public static LayoutBounds relativeTo(LayoutBounds parent, int x, int y, int width, int height) {
        return new LayoutBounds(parent.x() + x, parent.y() + y, width, height);
    }

    public boolean contains(double x, double y) {
        return x >= this.x && x < this.getLimitX() && y >= this.y && y < this.getLimitY();
    }

    public boolean overlaps(LayoutBounds other) {
        return this.x < other.getLimitX() && this.getLimitX() > other.x && this.y < other.getLimitY()
            && this.getLimitY() > other.y;
    }

    public int getLimitX() {
        return this.x + this.width;
    }

    public int getLimitY() {
        return this.y + this.height;
    }

    public int getCenterX() {
        return this.x + this.width / 2;
    }

    public int getCenterY() {
        return this.y + this.height / 2;
    }

    public LayoutBounds withX(int x) {
        return new LayoutBounds(x, this.y, this.width, this.height);
    }

    public LayoutBounds withY(int y) {
        return new LayoutBounds(this.x, y, this.width, this.height);
    }

    public LayoutBounds withWidth(int width) {
        return new LayoutBounds(this.x, this.y, width, this.height);
    }

    public LayoutBounds withHeight(int height) {
        return new LayoutBounds(this.x, this.y, this.width, height);
    }

    public LayoutBounds withSize(int width, int height) {
        return new LayoutBounds(this.x, this.y, width, height);
    }

    public LayoutBounds translate(int deltaX, int deltaY) {
        return new LayoutBounds(this.x + deltaX, this.y + deltaY, this.width, this.height);
    }

}
