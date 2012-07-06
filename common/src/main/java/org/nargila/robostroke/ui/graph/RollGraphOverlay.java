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
package org.nargila.robostroke.ui.graph;

import org.nargila.robostroke.common.filter.LowpassFilter;
import org.nargila.robostroke.data.DataIdx;
import org.nargila.robostroke.data.SensorDataSink;
import org.nargila.robostroke.ui.PaintStyle;
import org.nargila.robostroke.ui.RSCanvas;
import org.nargila.robostroke.ui.RSPaint;
import org.nargila.robostroke.ui.RSRect;
import org.nargila.robostroke.ui.UILiaison;
import org.nargila.robostroke.ui.graph.XYSeries.XMode;

public class RollGraphOverlay implements SensorDataSink {
    private static final double ROLL_PANNEL_DIM_FACTOR = 0.60;

    private static final float Y_RANGE = 10f;

    private final int rollAccumSize = 2;
    private int rollAccumCount;
    private float rollAccum;

    private final LowpassFilter filter = new LowpassFilter(.5f);

    private long rollAccumTimestamp;

    private final MultiXYSeries multySeries;
    private final XYSeries rollSeries;
    private final CyclicArrayXYSeries rollPanelSeries;
    private final RSPaint rollGraphPaint;
		
    private final RSPaint rollBackgroundPaint;

    private final CyclicArrayXYSeries rollSeriesImpl;

	private final UILiaison uiFactory;

    public RollGraphOverlay(UILiaison uiFactory, MultiXYSeries multySeries) {

    	this.uiFactory = uiFactory;
        rollPanelSeries = new CyclicArrayXYSeries(multySeries.xMode, new XYSeries.Renderer(uiFactory.createPaint()));
        rollPanelSeries.setxRange(multySeries.getxRange());

        this.multySeries = multySeries;
			
        {
            rollBackgroundPaint = uiFactory.createPaint();
            rollBackgroundPaint.setStyle(PaintStyle.FILL);
            rollBackgroundPaint.setAntiAlias(false);
            rollBackgroundPaint.setStrokeWidth(0);
        }

        {
            rollGraphPaint = uiFactory.createPaint(); 
            rollGraphPaint.setStyle(PaintStyle.STROKE);
            rollGraphPaint.setColor(uiFactory.getYellowColor());
            rollGraphPaint.setAlpha(170);
        }
			
        {
            rollSeriesImpl = new CyclicArrayXYSeries(XMode.ROLLING, new XYSeries.Renderer(rollGraphPaint, null));
            rollSeriesImpl.setIndependantYAxis(true);
            rollSeriesImpl.setyAxisSize(Y_RANGE);
        }

        rollSeries = multySeries.addSeries(rollSeriesImpl);
    }

    public void setXRange(double val) {
        rollPanelSeries.setxRange(val);
    }

    public void drawRollPanels(RSCanvas canvas, RSRect rect, double xAxisSize) {
        XYSeries ser = rollPanelSeries;

        final int len = ser.getItemCount();

        if (len > 0) {
            final int red = uiFactory.getRedColor();
            final int green = uiFactory.getGreenColor();

            final double maxYValue = Y_RANGE / 2;
            final double scaleX = rect.width() / xAxisSize;

            final double minX = multySeries.getMinX();

            double startX = ser.getX(0);
            double stopX;

            for (int i = 1; i < len; ++i, startX = stopX) {
                stopX = ser.getX(i);

                double avgY = Math.min(ser.getY(i), maxYValue);

                int color = avgY > 0 ? green : red;
                int alpha = (int) ((avgY / maxYValue) * 255 * ROLL_PANNEL_DIM_FACTOR);

                rollBackgroundPaint.setColor(color);
                rollBackgroundPaint.setAlpha(Math.min(Math.abs(alpha), 255));

                float left = (float) ((startX - minX) * scaleX);
                float right = (float) (((stopX - minX) * scaleX));

                canvas.drawRect((int)left, rect.top, (int)right, rect.bottom, rollBackgroundPaint);
            }
        }
    }

    public void reset() {
        synchronized (multySeries) {
            resetRollAccum();
            rollPanelSeries.clear();
        }
    }

    private void resetRollAccum() {
        rollAccum = 0;
        rollAccumCount = 0;
    }

    @Override
    public void onSensorData(long timestamp, Object value) {
    	synchronized (multySeries) {
    		float[] values = (float[]) value;

    		float y = filter
    		.filter(new float[] { values[DataIdx.ORIENT_ROLL] })[0];

    		rollAccum += y;

    		if (rollAccumCount++ == 0) {
    			rollAccumTimestamp = timestamp;
    		}

    		if (rollAccumCount == rollAccumSize) {
    			rollPanelSeries.add(rollAccumTimestamp, rollAccum / rollAccumSize);
    			resetRollAccum();
    		}

    		rollSeries.add(timestamp, y);
    	}
    }
}
