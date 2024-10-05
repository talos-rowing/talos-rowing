/*
 * Copyright (c) 2024 Tal Shalif
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

package org.nargila.robostroke.ui.graph.android;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import org.nargila.robostroke.ui.android.AndroidCanvas;
import org.nargila.robostroke.ui.graph.DataUpdatable;
import org.nargila.robostroke.ui.graph.UpdatableGraphBase;


/**
 * Simple line graph plot view.
 *
 * @author tshalif
 */
public abstract class AndroidGraphViewBase<T extends UpdatableGraphBase> extends View implements DataUpdatable {


    protected T graph;

    private final AndroidCanvas canvasAPI = new AndroidCanvas(null);

    public AndroidGraphViewBase(Context context) {

        super(context);
    }

    @Override
    protected void onAttachedToWindow() {

        graph.disableUpdate(false);

        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {

        graph.disableUpdate(true);

        super.onDetachedFromWindow();
    }

    protected void setGraph(T _graph) {

        this.graph = _graph;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        graph.draw(canvasAPI.setCanvas(canvas));
    }

    public boolean isDisabled() {
        return graph.isDisabled();
    }

    public void disableUpdate(boolean disable) {
        graph.disableUpdate(disable);
    }

    public void reset() {
        graph.reset();
    }
}
