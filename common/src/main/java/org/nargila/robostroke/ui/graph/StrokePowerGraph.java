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

import java.util.concurrent.TimeUnit;

import org.nargila.robostroke.BusEventListener;
import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.common.NumberHelper;
import org.nargila.robostroke.common.filter.LowpassFilter;
import org.nargila.robostroke.data.DataRecord;
import org.nargila.robostroke.data.SensorDataSink;
import org.nargila.robostroke.ui.UILiaison;
import org.nargila.robostroke.ui.graph.XYSeries.XMode;

/**
 * subclass of LineGraphView for setting acceleration specific parameters
 */
public class StrokePowerGraph extends LineGraph implements DataUpdatable {

  private static final float WINDOW_RESHRINK_DAMP_FACTOR = .2f;
  private static final double Y_RANGE = 3f;
  private static final double Y_RANGE_MAX = 12f;
  private static final double Y_RANGE_MIN = 2f;
  private static final double INCR = 1f;
  private static final long MAX_TIME_RANGE = TimeUnit.SECONDS.toNanos(3);
  private static final long MIN_TIME_RANGE = TimeUnit.MILLISECONDS.toNanos(300);
  private static final double INITIAL_XRANGE = (MAX_TIME_RANGE - MIN_TIME_RANGE) / 2;
  private double xRange = INITIAL_XRANGE;

  private final LowpassFilter windowSizeReshrinkDamper = new LowpassFilter(WINDOW_RESHRINK_DAMP_FACTOR);

  private final LowpassFilter yAxisReshrinkDamper = new LowpassFilter(.2f);

  private long powerStartTime;

  private final XYSeries powerSeries;
  private double yRange = Y_RANGE;
  private boolean validStrokePowerScope;
  private final RoboStroke roboStroke;
  private boolean hasStrokePower;

  private final SensorDataSink privateStrokePowerDataSink = new SensorDataSink() {

    @Override
    public void onSensorData(long timestamp, Object value) {
      if (validStrokePowerScope && (timestamp - powerStartTime) < MAX_TIME_RANGE) {
        float[] values = (float[]) value;
        powerSeries.add(timestamp, values[0]);
      }
    }
  };

  private int strokeRate;
  private final BusEventListener privateBusListener = new BusEventListener() {


    @Override
    public void onBusEvent(DataRecord event) {
      switch (event.type) {
      case STROKE_POWER_START:
        validStrokePowerScope = hasStrokePower && strokeRate > 10;
        if (validStrokePowerScope) {
          powerStartTime = event.timestamp;
          multySeries.clear();

          multySeries.setxRange(xRange);
          setyRangeMin(yRange);
        }
        break;
      case STROKE_POWER_END:

        if (validStrokePowerScope) {
          long powerEndTime = event.timestamp;
          long strokeTime = powerEndTime - powerStartTime;
          recalcXRange(strokeTime);
          recalcYRange();
        }

        hasStrokePower  = (Float)event.data > 0;
        break;
      case STROKE_RATE:

        strokeRate = (Integer)event.data;

        break;
      }
    }
  };
  private boolean disabled = true;

  public StrokePowerGraph(UILiaison uiLiaison, RoboStroke roboStroke)
  {
    super(uiLiaison, INITIAL_XRANGE, XYSeries.XMode.FIXED, Y_RANGE, INCR);

    this.roboStroke = roboStroke;

    powerSeries = multySeries.addSeries(new CyclicArrayXYSeries(XMode.FIXED, new XYSeries.Renderer(uiLiaison.createPaint())));

    positiveOnly = true;

    multySeries.setxRange(xRange);
    windowSizeReshrinkDamper.setFilteredValues(new float[] {(float) xRange});
    setyRangeMax(Y_RANGE_MAX);
    setyRangeMin(Y_RANGE_MIN);

    getMargines().top = 10;

    powerSeries.getRenderer().strokePaint.setStrokeWidth(4);
  }


  private void recalcYRange() {
    double currentYRange = NumberHelper.validRange(multySeries.getyRange(), Y_RANGE_MIN, Y_RANGE_MAX);

    yRange = yAxisReshrinkDamper.filter(new float[]{(float) currentYRange})[0];

  }

  private void recalcXRange(long strokeTime) {
    double validRange = NumberHelper.validRange(strokeTime, MIN_TIME_RANGE, MAX_TIME_RANGE);
    xRange = windowSizeReshrinkDamper.filter(new float[]{(float) validRange})[0] * 1.1f;
  }


  @Override
  public boolean isDisabled() {
    return disabled;
  }

  @Override
  public void disableUpdate(boolean disable) {

    if (this.disabled != disable) {
      if (!disable) {
        roboStroke.getStrokePowerScanner().addSensorDataSink(privateStrokePowerDataSink);
        roboStroke.getBus().addBusListener(privateBusListener);
      } else {
        validStrokePowerScope = hasStrokePower = false;
        strokeRate = 0;

        roboStroke.getStrokePowerScanner().removeSensorDataSink(privateStrokePowerDataSink);
        roboStroke.getBus().removeBusListener(privateBusListener);
      }

      this.disabled = disable;
    }
  }
}
