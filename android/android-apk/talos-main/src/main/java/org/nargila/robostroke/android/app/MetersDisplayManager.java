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


package org.nargila.robostroke.android.app;

import java.util.concurrent.TimeUnit;

import org.nargila.robostroke.BusEventListener;
import org.nargila.robostroke.ParamKeys;
import org.nargila.robostroke.RoboStrokeEventBus;
import org.nargila.robostroke.data.DataRecord;
import org.nargila.robostroke.data.DataRecord.Type;
import org.nargila.robostroke.data.SensorDataSink;
import org.nargila.robostroke.stroke.RowingSplitMode;
import org.nargila.robostroke.ui.LayoutMode;
import org.nargila.robostroke.way.GPSDataFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.Color;
import android.os.Handler;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Checks and updates rowing timer.
 * This class implements SensorDataSink so it can
 * update itself upon sensor events and does not have to
 * create a timer thread.
 *
 * FIXME: merge with MetersDisplayManager in robostroke-common
 */
public class MetersDisplayManager implements SensorDataSink {

  private static final Logger logger = LoggerFactory.getLogger(MetersDisplayManager.class);

  private static final int GPS_BAD_COLOUR = Color.argb(150, 255, 0, 0);//Color.RED;

  private static final int GPS_NOT_BAD_COLOUR = Color.argb(150, 255, 165, 0);//Color.ORANGE;

  private static final int GPS_FAIR_COLOUR = Color.argb(170, 255, 255, 0);//Color.YELLOW;

  private static final int GPS_GOOD_COLOUR = Color.argb(150, 0, 255, 0);//Color.GREEN;

  private static final int ROWING_OFF_COLOUR = Color.DKGRAY;

  private static final int ROWING_ON_COLOUR = Color.BLACK;

  private static final int ROWING_START_PENDING_COLOUR = Color.argb(170, 255, 165, 0);

  private static final int[] METERS = {R.id.spm_meter, R.id.speed_meter, R.id.distance_meter, R.id.time_meter};

  private final Handler handler;

  private final ViewGroup[] meterLayouts = {null, null};
  private ViewGroup currentMeterLayout;


  private final TextView timeTxt;
  private final TextView splitTimeTxt;
  private final TextView spmAvg;
  private final TextView spmTxt;
  private final TextView speedTxt;
  private final TextView speedAvg;
  private final View accuracyHighlighter;
  private final TextView strokeCountTxt;
  private final TextView strokeDistanceTxt;
  private final TextView distanceSubTxt;
  private final TextView distanceMainTxt;
  private TextView distanceTxt;
  private TextView splitDistanceTxt;

  private LayoutMode layoutMode = LayoutMode.COMPACT;

  private boolean splitTimerOn;


  /**
   * timestamp origin for global time
   */
  private long startTime;
  /**
   * timestamp start for split time
   */
  private long splitTimeStart;
  private int startStrokeCount;
  private int lastStrokeCount;
  private int spmAccum;
  private int spmCount;

  private long lastTime;

  private double accumulatedDistance;
  private double splitDistance;

  private boolean hasPower;

  private final RoboStrokeActivity owner;

  private final RoboStrokeEventBus bus;

  private boolean triggered;

  private boolean resetOnStart = true;

  private long lastStopTime = -1;

  private Long startTimestamp;

  private long baseDistanceTime;

  private double baseDistance;

  private long splitDistanceTime;

  protected boolean lockLayoutMode;

  private int spm;

  /**
   * @param owner the RoboStroke Activity
   */
  MetersDisplayManager(final RoboStrokeActivity owner) {

    this.owner = owner;

    handler = owner.handler;
    timeTxt = (TextView) owner.findViewById(R.id.time);
    splitTimeTxt = (TextView) owner.findViewById(R.id.split_time);
    strokeCountTxt = (TextView) owner.findViewById(R.id.stroke_count);
    strokeDistanceTxt = (TextView) owner.findViewById(R.id.stroke_distance);
    spmTxt = (TextView) owner.findViewById(R.id.spm);
    spmAvg = (TextView) owner.findViewById(R.id.spm_avg);
    speedTxt = (TextView) owner.findViewById(R.id.speed);
    speedAvg = (TextView) owner.findViewById(R.id.speed_avg);
    accuracyHighlighter = owner.findViewById(R.id.speed_meter).findViewWithTag("accuracyHighlighter");
    splitDistanceTxt = distanceSubTxt = (TextView) owner.findViewById(R.id.distance_sub);
    distanceTxt = distanceMainTxt = (TextView) owner.findViewById(R.id.distance_main);

    meterLayouts[LayoutMode.COMPACT.ordinal()] = (ViewGroup) owner.findViewById(R.id.meters_container_compact);
    meterLayouts[LayoutMode.EXPANDED.ordinal()] = (ViewGroup) owner.findViewById(R.id.meters_container_expanded);

    bus = owner.roboStroke.getBus();

    {
      this.currentMeterLayout = meterLayouts[LayoutMode.COMPACT.ordinal()];
      toggleMeterExtras(LayoutMode.COMPACT);
    }

    splitTimeTxt.setOnLongClickListener(new View.OnLongClickListener() {

      @Override
      public boolean onLongClick(View v) {
        resetSplit();

        updateCount();
        updateSplitDistance();
        resetAvgSpm();
        updateTime(lastTime, true);

        return true;
      }

    });

    splitTimeTxt.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        bus.fireEvent(Type.ROWING_START_TRIGGERED, true);
      }

    });

    owner.roboStroke.getBus().addBusListener(new BusEventListener() {

      @Override
      public void onBusEvent(DataRecord event) {
        switch (event.type) {
        case ROWING_START_TRIGGERED:
          triggered = (Boolean)event.data;
          break;
        case ROWING_START:
          logger.info("ROWING_START {}", event.timestamp);
          triggered = false;
          splitTimerOn = true;
          startTimestamp = (Long) event.data;
          splitDistanceTime = 0;
          splitDistance = 0;

          if (resetOnStart) {
            resetSplit();
            splitTimeStart = TimeUnit.NANOSECONDS.toSeconds(startTimestamp - startTime);
          } else {
            if (lastStopTime != -1) {
              splitTimeStart += TimeUnit.NANOSECONDS.toSeconds(startTimestamp - lastStopTime);
            }
          }

          updateCount();
          updateSplitDistance();

          break;
        case ROWING_STOP:
          logger.info("ROWING_STOP {}", event.timestamp);
          Object[] vals = (Object[]) event.data;
          triggered = false;
          splitTimerOn = false;
          long stopTime = (Long) vals[0];
          updateTime(TimeUnit.NANOSECONDS.toSeconds(stopTime - startTime), true);
          lastStopTime = stopTime;

          baseDistance += splitDistance;
          baseDistanceTime += splitDistanceTime;

          break;
        case ROWING_COUNT:
          lastStrokeCount++;
          updateCount();

          break;
        case STROKE_POWER_END:
          hasPower = (Float)event.data > 0;

          logger.info("STROKE_POWER_END (has power: {})", hasPower);

          break;
        case STROKE_RATE:
          if (hasPower) {
            int spm = (Integer) event.data;
            spmAccum += spm;
            spmCount++;

            updateSpm(spm);

            hasPower = false;
          }
          break;
        case BOOKMARKED_DISTANCE:
        {
          Object[] values = (Object[]) event.data;

          long travelTime = (Long)values[0];
          splitDistance = (Float)values[1];

          logger.info("BOOKMARKED_DISTANCE: elapsedDistance = {}", splitDistance);

          splitDistanceTime = TimeUnit.MILLISECONDS.toSeconds(travelTime);

          updateSplitDistance();

        }
        break;

        case WAY:
        {
          double[] values = (double[]) event.data;
          double distance = values[0];
          long speed = (long) values[1];
          double accuracy = values[2];

          updateSpeed(speed, accuracy);

        }
        break;
        case ACCUM_DISTANCE:
          updateDistance((Double)event.data);
          break;
        }
      }
    });
  }


  private void updateSpm(final int spm) {

    this.spm = spm;

    handler.post(new Runnable() {

      @Override
      public void run() {
        spmTxt.setText(spm + "");

        if (splitTimerOn) {
          float avgSpm = spmAccum / (float)spmCount;

          spmAvg.setText(String.format("%.01f", avgSpm));
        }

      }
    });
  }
  private void resetAvgSpm() {
    handler.post(new Runnable() {

      @Override
      public void run() {
        spmAvg.setText("0");
      }
    });
  }

  public void setResetOnStart(boolean resetOnStart) {
    this.resetOnStart = resetOnStart;
  }

  /**
   * format and display distance in the 'distanceTxt' text view
   *
   * @param distance
   *            in meters
   */
  private void updateDistance(final double distance) {
    accumulatedDistance = distance;

    handler.post(new Runnable() {

      @Override
      public void run() {
        distanceTxt.setText((int)accumulatedDistance + "");
      }
    });
  }

  /**
   * Update split distance, avg speed meters
   *
   */
  private void updateSplitDistance() {
      handler.post(new Runnable() {

        @Override
        public void run() {
          splitDistanceTxt.setText((int)(splitDistance + baseDistance) + "");
          setAvgSpeed();
        }

        private void setAvgSpeed() {

          long speed500ms = 0;

          if (splitDistance + baseDistance > 20) { // ni bother avg calculation for less than 2 strokes' worth..

            float avgSpeed = (float)(splitDistance + baseDistance) / (splitDistanceTime + baseDistanceTime);

            speed500ms = GPSDataFilter.calcMilisecondsPer500m(avgSpeed);
          }

          speedAvg.setText(formatSpeed(speed500ms));

        }
      });

  }



  private void updateSpeed(final long speed, double accuracy) {

    final String display = formatSpeed(speed);

    final int color = (accuracy <= 2.0 ? GPS_GOOD_COLOUR
        : (accuracy <= 4.0 ? GPS_FAIR_COLOUR
            : (accuracy <= 6.0 ? GPS_NOT_BAD_COLOUR : GPS_BAD_COLOUR)));


    handler.post(new Runnable() {

      @Override
      public void run() {
        speedTxt.setText(display);

        if (spm > 0 && speed > 0) {

          double millisPerMeter = speed / 500.0;

          double millisPerStroke = 60000.0 / spm;

          double metersPerStroke = millisPerStroke / millisPerMeter;

          int strokeDistanceColor = (metersPerStroke < 8 ? Color.RED
              : (metersPerStroke < 10 ? Color.YELLOW : Color.GREEN));

          strokeDistanceTxt.setText(String.format("%.1fm", metersPerStroke));
          strokeDistanceTxt.setTextColor(strokeDistanceColor);
        }

        accuracyHighlighter.setBackgroundColor(color);
      }});
  }

  private String formatSpeed(long speed) {
    String display;
    Time speedTime = new Time();

    speedTime.set(speed);

    display = String.format("%d:%02d", speedTime.minute, speedTime.second);
    return display;
  }

  private void updateTime(final long seconds, final boolean splitOnly) {

    if (!splitOnly) {
      lastTime = seconds;
    }

    if (splitTimeStart > seconds) { // can happen in replay mode + skip-back
      splitTimeStart = seconds;
    }

    handler.post(new Runnable() {

      @Override
      public void run() {

        if (!splitOnly) {
          timeTxt.setText(formatTime(seconds, false));
        }

        if (splitOnly || splitTimerOn) {
          splitTimeTxt.setText(formatTime(seconds - splitTimeStart, true));

        }

        int color;

        if (splitTimerOn) {
          color = ROWING_ON_COLOUR;
        } else if (triggered) {
          color = ROWING_START_PENDING_COLOUR;
        } else {
          color = ROWING_OFF_COLOUR;
        }

        highlightTimeMeter(color);
      }

      private void highlightTimeMeter(int color) {
        View highlightBar = (View)timeTxt.getParent();

        highlightBar.setBackgroundColor(color);
      }
    });
  }

  void resetSplit() {
    lastStopTime = -1;
    startStrokeCount = lastStrokeCount;
    splitTimeStart = lastTime;
    splitDistance = 0;
    spmAccum = spmCount = 0;
    baseDistance = 0;
    baseDistanceTime = 0;
    spm = 0;
  }

  void reset() {
    resetSplit();

    lastStrokeCount = startStrokeCount = 0;
    lastTime = startTime = splitTimeStart = 0;
    accumulatedDistance = 0;

    splitTimerOn = RowingSplitMode.CONTINUOUS ==  queryRowingMode() ? true : false;

    updateTime(lastTime, true);
    updateCount();
  }

  private RowingSplitMode queryRowingMode() {
    Object val = owner.getRoboStroke().getParameters().getValue(ParamKeys.PARAM_ROWING_MODE.getId());
    return RowingSplitMode.valueOf(val.toString());
  }

  private void updateCount() {
    if (splitTimerOn) {
      handler.post(new Runnable() {

        @Override
        public void run() {
          int strokeCount = lastStrokeCount - startStrokeCount;

          strokeCountTxt.setText(strokeCount + "");
        }
      });
    }
  }

  private String formatTime(long seconds, boolean trimed) {
    long hours = seconds / 3600;
    seconds -= hours * 3600;
    long minutes = seconds / 60;
    seconds -= minutes * 60;

    return (!trimed || hours > 0) ? String.format("%d:%02d:%02d", hours, minutes, seconds) : String.format("%d:%02d", minutes, seconds);
  }

  private void updateLayout(LayoutMode meterLayout) {

    ViewGroup nextMeterLayout = meterLayouts[meterLayout.ordinal()];

    if (currentMeterLayout != nextMeterLayout) {
      int[] containers = {R.id.spm_meter_container, R.id.speed_meter_container, R.id.distance_meter_container, R.id.time_meter_container};

      for (int i = 0; i < METERS.length; ++i) {
        View meter = currentMeterLayout.findViewById(METERS[i]);
        FrameLayout oldContainer = (FrameLayout) meter.getParent();
        FrameLayout newContainer = (FrameLayout) nextMeterLayout.findViewById(containers[i]);

        oldContainer.removeView(meter);
        newContainer.addView(meter);
      }

      currentMeterLayout.setVisibility(View.GONE);
      nextMeterLayout.setVisibility(View.VISIBLE);
      currentMeterLayout = nextMeterLayout;

      boolean swapped = true;
      switch (meterLayout) {
      case COMPACT:
        splitDistanceTxt = distanceSubTxt;
        distanceTxt = distanceMainTxt;
        break;
      case EXPANDED:
        distanceTxt = distanceSubTxt;
        splitDistanceTxt = distanceMainTxt;
        break;
      default:
        swapped = false;
        break;
      }

      if (swapped) {
        handler.post(new Runnable() {

          @Override
          public void run() {
            CharSequence tmp = distanceTxt.getText();
            distanceTxt.setText(splitDistanceTxt.getText());
            splitDistanceTxt.setText(tmp);
          }
        });
      }

      toggleMeterExtras(meterLayout);

    }
  }

  private void toggleMeterExtras(LayoutMode meterLayout) {
    for (int i: METERS) {
      owner.findViewById(i).findViewWithTag("extraView").setVisibility(meterLayout == LayoutMode.EXPANDED ? View.VISIBLE : View.GONE);
      owner.findViewById(i).findViewWithTag("extraLabel").setVisibility(meterLayout == LayoutMode.EXPANDED ? View.VISIBLE : View.GONE);
    }
  }

  @Override
  public void onSensorData(long timestamp, Object value) {

    if (startTime == 0) {
      startTime = timestamp;
    }

    final long timeSecs = TimeUnit.NANOSECONDS.toSeconds(timestamp - startTime);

    if (timeSecs != lastTime) {
      updateTime(timeSecs, false);
    }
  }

  public void onLayoutModeChange(LayoutMode newMode) {
    if (!lockLayoutMode) {
      setLayoutMode(newMode);
    }
  }

  private void setLayoutMode(LayoutMode newMode) {
    if (newMode != layoutMode) {
      updateLayout(newMode);
      layoutMode = newMode;
    }
  }

  void onUpdateGraphSlotCount(int slotCount) {
    onLayoutModeChange(slotCount == 1 ? LayoutMode.EXPANDED : LayoutMode.COMPACT);

  }

  public void setLayoutMode(String val) {

    if (val == null) {
      val = "AUTO";
    }

    lockLayoutMode = !val.equals("AUTO");

    if (lockLayoutMode) {
      setLayoutMode(LayoutMode.valueOf(val));
    }
  }
}
