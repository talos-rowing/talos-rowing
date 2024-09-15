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
package org.nargila.robostroke.ui.graph;

import java.util.LinkedList;

import org.nargila.robostroke.common.DoubleGenerator;

public class MultiXYSeries {

  private final LinkedList<XYSeries> seriesList = new LinkedList<XYSeries>();

  private double xRange = 0; // -n: stop at range, n: cyclical range, 0: grow
  final XYSeries.XMode xMode;
  private DoubleGenerator xRangeGenerator;

  public MultiXYSeries(double xRange, XYSeries.XMode xMode) {
    this.xMode = xMode;
    setxRange(xRange);
    clear();
  }

  public LinkedList<XYSeries> getSeries() {
    return seriesList;
  }


  public synchronized double getMinY() {
    double res = 0;

    for (XYSeries ser: seriesList) {
      if (ser.getItemCount() > 0 && !ser.isIndependantYAxis()) {
        res = Math.min(res, ser.getMinY());
      }
    }

    return res;
  }

  public void setxRangeGenerator(DoubleGenerator xRangeGenerator) {
    this.xRangeGenerator = xRangeGenerator;
  }

  public synchronized double getMinX() {
    double res = Double.MAX_VALUE;

    for (XYSeries ser: seriesList) {
      if (ser.getItemCount() > 0) {
        res = Math.min(res, ser.getMinX());
      }
    }

    return res;
  }

  public synchronized double getMaxX() {
    double res = -Double.MAX_VALUE;

    for (XYSeries ser: seriesList) {
        if (ser.getItemCount() > 0) {
      res = Math.max(res, ser.getMaxX());
        }
    }

    return res;
  }

  public synchronized double getMaxY() {
    double res = 0;

    for (XYSeries ser: seriesList) {
      if (ser.getItemCount() > 0 && !ser.isIndependantYAxis()) {
        res = Math.max(res, ser.getMaxY());
      }
    }

    return res;
  }


  public double getxRange() {
    return xRangeGenerator == null ? xRange : xRangeGenerator.get();
  }


  public synchronized void setxRange(double xRange) {
    this.xRange = xRange;

    for (XYSeries series: seriesList) {
      series.setxRange(xRange);
    }
  }


  public synchronized double getyRange() {
    return Math.max(Math.abs(getMaxY()),  Math.abs(getMinY())) * 2;
  }

  public synchronized XYSeries addSeries(XYSeries series) {
    return addSeries(series, true);
  }

  public synchronized XYSeries addSeries(XYSeries series, boolean proxied) {
    seriesList.add(series);
    series.setXMode(xMode);
    series.setxRange(xRange);

    if (!proxied) {
      return series;
    } else {

      final MultiXYSeries ms = this;

      return new XSeriesProxy(series) {

        @Override
        public void add(double x, double y) {
          synchronized (ms) {
            super.add(x, y);
            ms.onAdd(x, y, impl);
          }
        }

        @Override
        public void remove(int index) {
          synchronized (ms) {
            super.remove(index);
            ms.onRemove(index, impl);
          }
        }
      };
    }
  }

  public synchronized void removeSeries(XYSeries series) {
    seriesList.remove(series);
  }


  protected void onAdd(double x, double y, XYSeries series) {}


  protected void onRemove(int index, XYSeries series) {}

  public synchronized void clear() {
    for (XYSeries series: seriesList) {
      series.clear();
    }
  }
}
