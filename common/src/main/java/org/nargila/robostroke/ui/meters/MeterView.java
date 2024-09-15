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

package org.nargila.robostroke.ui.meters;

import org.nargila.robostroke.ui.LayoutMode;
import org.nargila.robostroke.ui.RSTextView;
import org.nargila.robostroke.ui.RSView;

public interface MeterView {

  public abstract void updateLayout(LayoutMode meterLayout);

  public RSTextView getSplitTimeTxt();
  public RSTextView getSpmTxt();
  public RSTextView getSpeedTxt();
  public RSTextView getAvgSpeedTxt();
  public RSView getAccuracyHighlighter();
  public RSView getStrokeModeHighlighter();
  public RSTextView getStrokeCountTxt();
  public RSTextView getSplitDistanceTxt();
  public RSTextView getTotalDistanceTxt();
  public RSTextView getTotalTimeTxt();
  public RSTextView getSplitStrokesTxt();
  public RSTextView getAvgSpmTxt();
}
