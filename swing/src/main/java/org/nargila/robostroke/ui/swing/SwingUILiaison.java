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
package org.nargila.robostroke.ui.swing;

import java.awt.Color;
import java.awt.Component;

import org.nargila.robostroke.ui.RSPaint;
import org.nargila.robostroke.ui.RSPath;
import org.nargila.robostroke.ui.UILiaison;

public class SwingUILiaison implements UILiaison {

  private final Component canvas;

  public SwingUILiaison(Component canvas) {
    this.canvas = canvas;
  }


  public RSPaint createPaint() {
    return new SwingPaint();
  }

  public RSPath createPath() {
    return new SwingPath();
  }

  public Object getComponent() {
    return canvas;
  }

  public int getGreenColor() {
    return Color.GREEN.getRGB();
  }

  public int getRedColor() {
    return Color.RED.getRGB();
  }

  public int getYellowColor() {
    return Color.YELLOW.getRGB();
  }

  public void repaint() {
    canvas.repaint();
  }

  @Override
  public void setVisible(boolean visible) {
    canvas.setVisible(visible);
  }
}
