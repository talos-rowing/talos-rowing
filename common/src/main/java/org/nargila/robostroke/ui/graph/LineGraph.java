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

import org.nargila.robostroke.common.NumberHelper;
import org.nargila.robostroke.ui.*;

/**
 * Simple line graph plot view.
 *
 * @author tshalif
 */
public class LineGraph implements UpdatableGraphBase {
    protected MultiXYSeries multySeries;
    protected final UILiaison uiLiaison;
    private double yRangeMin;
    private double yRangeMax = Double.MAX_VALUE;
    private final double incr;

    protected boolean disabled = true;

    private final GraphMargines margines = new GraphMargines();

    protected boolean positiveOnly = false;

    private final RSPaint gridPaint;

    private final RSPaint centreLinePaint;

    public GraphMargines getMargines() {
        return margines;
    }

    public LineGraph(final UILiaison uiLiaison, double xRange, XYSeries.XMode xMode, double yScale,
                     double yGridInterval) {
        this(uiLiaison, yScale, yGridInterval, null);

        multySeries = new MultiXYSeries(xRange, xMode) {
            @Override
            protected void onAdd(double x, double y, XYSeries series) {
                uiLiaison.repaint();
            }

            @Override
            protected void onRemove(int index, XYSeries series) {
                uiLiaison.repaint();
            }

            @Override
            public void clear() {
                super.clear();
                uiLiaison.repaint();
            }
        };

    }

    /**
     * constructor with standard View context, attributes, data window size, y
     * scale and y data tic mark gap
     *
     * @param context    the Android Activity
     * @param attrs      layout and other common View attributes
     * @param windowSize size of data array to plot
     * @param yScale     y value to pixel scale
     * @param incr       y data tic mark gap
     */
    public LineGraph(UILiaison uiLiaison, double yRange,
                     double yGridInterval, MultiXYSeries multiSeries) {

        this.uiLiaison = uiLiaison;
        this.yRangeMin = yRange;
        this.incr = yGridInterval;
        this.multySeries = multiSeries;

        gridPaint = uiLiaison.createPaint();
        gridPaint.setARGB(0xff, 0x55, 0x55, 0x55);
        gridPaint.setStrokeWidth(0f);
        centreLinePaint = uiLiaison.createPaint();
        centreLinePaint.setColor(uiLiaison.getRedColor());
        centreLinePaint.setStrokeWidth(1f);
    }

    public UILiaison getUiLiaison() {
        return uiLiaison;
    }

    @Override
    public void draw(RSCanvas canvas) {

        synchronized (multySeries) {

            RSRect rect = canvas.getClipBounds();

            rect.top += margines.top;
            rect.bottom -= margines.bottom;
            rect.left += margines.left;
            rect.right -= margines.right;

            if (rect.width() > 0 && rect.height() > 0) {
                double xAxisSize = multySeries.getxRange();
                double yAxisSize = calcYAxisSize();

                drawGraph(canvas, rect, xAxisSize, yAxisSize);
            }
        }
    }

    public MultiXYSeries getSeries() {
        return multySeries;
    }

    protected void drawGraph(RSCanvas canvas, RSRect rect, double xAxisSize,
                             double yAxisSize) {

        drawGrid(canvas, yAxisSize, rect);
        drawCentreLine(canvas, rect);

        drawAllSeries(canvas, rect, xAxisSize, yAxisSize);
    }

    private void drawAllSeries(RSCanvas canvas, RSRect rect, double xAxisSize,
                               double yAxisSize) {
        for (XYSeries series : multySeries.getSeries()) {
            double seriesYAxisSize = series.getyAxisSize();
            drawSeries(canvas, rect, xAxisSize,
                    seriesYAxisSize > 0 ? seriesYAxisSize : yAxisSize, series);
        }
    }

    private double calcYAxisSize() {
        double res = NumberHelper.validRange(multySeries.getyRange(),
                yRangeMin, yRangeMax);

        return positiveOnly ? res / 2 : res;
    }

    protected void drawSeries(RSCanvas canvas, RSRect rect, double xAxisSize,
                              double yAxisSize, XYSeries series) {

        final int len = series.getItemCount();

        if (len > 0) {

            double scaleX = rect.width() / xAxisSize;
            final int height = rect.height();
            double scaleY = height / yAxisSize;
            RSPath path = uiLiaison.createPath();
            final int bottom = rect.bottom;
            final int hHalf = height / 2;
            double minX = multySeries.getMinX();
            float x = (float) ((series.getX(0) - minX) * scaleX);
            float y = (float) (bottom - (series.getY(0) * scaleY));

            if (!positiveOnly) {
                y -= hHalf;
            }

            path.moveTo(x, y);

            double prevYVal = 0;
            for (int i = 1; i < len; i++) {
                double yVal = series.getY(i);

                x = (float) ((series.getX(i) - minX) * scaleX);
                y = (float) (bottom - (yVal * scaleY));

                if (!positiveOnly) {
                    y -= hHalf;
                }

                if (prevYVal == 0 && yVal == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }

                prevYVal = yVal;
            }

            canvas.drawPath(path, series.getRenderer().strokePaint);
        }
    }


    protected void drawGrid(RSCanvas canvas, double yAxisSize, RSRect rect) {
        float y;
        final int top = rect.top;
        final int height = rect.height();

        for (double j = incr; j < yAxisSize; j += incr) {
            y = (float) ((j * height / yAxisSize) + top);
            canvas.drawLine(rect.left, y, rect.right, y, gridPaint);
        }
    }

    protected void drawLine(RSCanvas canvas, double yAxisSize, RSRect rect) {
        float y;
        final int top = rect.top;
        final int height = rect.height();

        for (double j = incr; j < yAxisSize; j += incr) {
            y = (float) ((j * height / yAxisSize) + top);
            canvas.drawLine(rect.left, y, rect.right, y, gridPaint);
        }
    }

    protected void drawCentreLine(RSCanvas canvas, RSRect rect) {
        final int yCenter = rect.top + rect.height() / 2;

        if (!positiveOnly) {
            canvas.drawLine(0, yCenter, rect.width(), yCenter, centreLinePaint);
        }
    }

    @Override
    public void reset() {
        multySeries.clear();
    }

    public double getyRangeMax() {
        return yRangeMax;
    }

    public void setyRangeMax(double yRangeMax) {
        this.yRangeMax = yRangeMax;
    }

    public void setyRangeMin(double yRangeMin) {
        this.yRangeMin = yRangeMin;
    }

    public double getyRangeMin() {
        return yRangeMin;
    }

    public double getxRange() {
        return multySeries.getxRange();
    }

    public void setXRange(double val) {
        multySeries.setxRange(val);
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void disableUpdate(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public void setVisible(boolean visible) {
        uiLiaison.setVisible(visible);
    }

    @Override
    public void repaint() {
        uiLiaison.repaint();
    }
}
