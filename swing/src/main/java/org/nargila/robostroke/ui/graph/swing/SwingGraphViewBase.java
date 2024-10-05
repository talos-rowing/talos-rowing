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

package org.nargila.robostroke.ui.graph.swing;

import org.nargila.robostroke.ui.RSClickListener;
import org.nargila.robostroke.ui.RSDoubleClickListener;
import org.nargila.robostroke.ui.RSLongClickListener;
import org.nargila.robostroke.ui.graph.DataUpdatable;
import org.nargila.robostroke.ui.graph.UpdatableGraphBase;
import org.nargila.robostroke.ui.swing.SwingCanvas;
import org.nargila.robostroke.ui.swing.SwingView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;


/**
 * Simple line graph plot view.
 *
 * @author tshalif
 */
public abstract class SwingGraphViewBase<T extends UpdatableGraphBase> extends JPanel implements DataUpdatable {


    private static final long serialVersionUID = 1L;

    protected T graph;

    private final boolean selfPaint;

    private final SwingView swingView;

    public SwingGraphViewBase() {
        this(true);
    }

    public SwingGraphViewBase(boolean selfPaint) {

        this.selfPaint = selfPaint;

        swingView = new SwingView(this);

//    setDoubleBuffered(false);

    }

    public void setOnLongClickListener(RSLongClickListener listener) {
        swingView.setOnLongClickListener(listener);
    }

    public void setOnClickListener(RSClickListener listener) {
        swingView.setOnClickListener(listener);
    }

    public void setOnDoubleClickListener(RSDoubleClickListener listener) {
        swingView.setOnDoubleClickListener(listener);
    }

    protected void setGraph(T _graph) {

        this.graph = _graph;

        addComponentListener(new ComponentListener() {

            public void componentShown(ComponentEvent e) {
                graph.disableUpdate(false);
            }

            public void componentResized(ComponentEvent e) {
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentHidden(ComponentEvent e) {
                graph.disableUpdate(true);
            }
        });
    }

    @Override
    public final void paint(Graphics g) {

        if (selfPaint) {
            Rectangle r = getBounds();
            g.setColor(Color.BLACK);
            g.fillRect(r.x, r.y, r.width, r.height);
            graph.draw(new SwingCanvas(this, g));
        } else {
            super.paint(g);
        }
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
