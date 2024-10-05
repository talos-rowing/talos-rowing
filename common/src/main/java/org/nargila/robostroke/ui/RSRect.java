/*
 * Copyright (c) 2012 Tal Shalif
 *
 * This file is part of Talos-Rowing.
 *
 * Talos-Rowing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Talos-Rowing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Talos-Rowing.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.nargila.robostroke.ui;

public class RSRect {

    public int top;
    public int bottom;
    public int left;
    public int right;

    public RSRect() {
    }

    public RSRect(RSRect rect) {
        top = rect.top;
        bottom = rect.bottom;
        left = rect.left;
        right = rect.right;
    }

    public int height() {
        return bottom - top;
    }

    public int width() {
        return right - left;
    }

    @Override
    public String toString() {
        return "[RSRect: x=" + left + ", y=" + top + ", width=" + width() + ", height=" + height() + "]";
    }

}
