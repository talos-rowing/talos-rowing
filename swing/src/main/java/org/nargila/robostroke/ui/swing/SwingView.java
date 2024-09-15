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
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.nargila.robostroke.ui.RSClickListener;
import org.nargila.robostroke.ui.RSDoubleClickListener;
import org.nargila.robostroke.ui.RSLongClickListener;
import org.nargila.robostroke.ui.RSView;

public class SwingView implements RSView {

  private static final long LONG_PRESS_MIN_TIME = 1000; // 1sec
  private static final long DOUBLE_CLICK_MAX_TIME = 300;

  protected final Component impl;

  private RSClickListener clickListener;
  private RSLongClickListener longClickListener;
  private RSDoubleClickListener doubleClickListener;

  private Point pressPoint;

  private long pressTime;

  private MouseAdapter mouseAdapter;

  public SwingView(Component impl) {
    this.impl = impl;
  }


  private void initMouseAdapter() {

    if (mouseAdapter == null) {
      mouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {

          pressPoint = e.getPoint();

          pressTime = e.getWhen();

        }

        @Override
        public void mouseReleased(MouseEvent e) {

          if (e.getPoint().equals(pressPoint)) {
            if (e.getWhen() - pressTime > LONG_PRESS_MIN_TIME) {
              if (longClickListener != null) {
                longClickListener.onLongClick();
                return;
              }
            }

            if (clickListener != null) {
              clickListener.onClick();
            }
          }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
          pressPoint = null;
          pressTime = 0;
        }
      };

      impl.addMouseListener(mouseAdapter);
    }
  }

  @Override
  public void setOnLongClickListener(RSLongClickListener listener) {
    initMouseAdapter();
    longClickListener = listener;
  }

  @Override
  public void setOnClickListener(RSClickListener listener) {
    initMouseAdapter();
    clickListener = listener;

  }

  @Override
  public void setOnDoubleClickListener(RSDoubleClickListener listener) {
    initMouseAdapter();
    doubleClickListener = listener;
  }

  @Override
  public void setVisible(boolean visible) {
    impl.setVisible(visible);

  }

  @Override
  public void setBackgroundColor(int... argb) {
    impl.setBackground(new Color(argb[1], argb[2], argb[3], argb[0]));
  }
}
