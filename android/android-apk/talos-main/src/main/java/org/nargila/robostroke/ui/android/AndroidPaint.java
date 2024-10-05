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
package org.nargila.robostroke.ui.android;

import android.graphics.Paint;
import org.nargila.robostroke.ui.PaintStyle;
import org.nargila.robostroke.ui.RSPaint;

public class AndroidPaint extends Paint implements RSPaint {

    @Override
    public void setStyle(PaintStyle style) {
        Style s = Style.valueOf(style.name());
        super.setStyle(s);
    }
}
