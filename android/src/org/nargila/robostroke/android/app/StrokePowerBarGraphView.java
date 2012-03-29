/*
 * Copyright (c) 2011 Tal Shalif
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

import org.nargila.robostroke.BusEventListener;
import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.input.DataRecord;
import org.nargila.robostroke.ui.graph.DataUpdatable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.View;

/**
 * Simple line graph plot view.
 * @author tshalif
 *
 */
public class StrokePowerBarGraphView extends View implements DataUpdatable, RoboStrokeConstants {
	private static final int HISTORY_SIZE = 40;
	private final int windowSize;
	private final float[] powerValues;
	private int idx;

	private final Rect mRect = new Rect();
	private final Rect rect = new Rect();
	private boolean needRescale = false;
	private float maxY = -Float.MAX_VALUE;

	private float scaleY;
	private float barWidth;
	private float barStrokeWidth;
	private final RoboStroke roboStroke;

	private final BusEventListener privateBusListener = new BusEventListener() {

		@Override
		public void onBusEvent(DataRecord event) {

			switch (event.type) {
			case STROKE_POWER_END:
				float power = (Float) event.data;
				if (power > 0) {
					addValue(power);
				}
				break;
			}
		}
	};
	private boolean disabled = true;

	/**
	 * constructor with standard View context, attributes, data window size, y scale and y data tic mark gap
	 * @param context the Android Activity
	 * @param roboStroke 
	 * @param attrs layout and other common View attributes
	 */
	public StrokePowerBarGraphView(Context context, RoboStroke roboStroke) 
	{ 
		super(context); 
		
		this.roboStroke = roboStroke;
		this.windowSize = HISTORY_SIZE;
		powerValues = new float[windowSize];
	}

	public void reset() {
		idx = 0;
		maxY = 0;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.getClipBounds(rect);

		if (!rect.equals(mRect)) {
			mRect.set(rect);
			needRescale = true;
		}

		if (needRescale) {
			rescalePoints();
		}


		Paint paint = new Paint();
		paint.setARGB(0xff, 0xff, 0xff, 0xff);
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(false);
		paint.setStrokeWidth(barStrokeWidth);
		drawPath(canvas, paint);
	}

	private void rescalePoints() {
		maxY = -Float.MAX_VALUE;

		
		barWidth = ((float)mRect.width() / windowSize);
		
		barStrokeWidth = barWidth / 2;
		
		final int len = Math.min(idx, windowSize);

		int i = idx - len;

		for (int j = 0; j < len; j++) {
			final int k = (i + j) % windowSize;

			float y = powerValues[k];

			maxY = Math.max(y, maxY);
		}

		scaleY = mRect.height() / maxY;

		needRescale = false;
	}

	
	private void drawPath(Canvas canvas, Paint paint) {
		final int len = Math.min(idx, windowSize);
		final int bottom = mRect.bottom;
		float x, y;

		final int i = idx - len;

		float startY = powerValues[(i % windowSize)];

		float startX = barWidth / 2;//(windowSize - len) * ticWidth;

		y = bottom - (startY * scaleY);
		
		Path path = new Path();		

		for (int j = 0; j < len; j++) {
			final int k = (i + j) % windowSize;
			x = startX + j * barWidth;
			y = bottom - (powerValues[k] * scaleY);
			path.moveTo(x, y);
			path.lineTo(x , bottom);
		}

		canvas.drawPath(path, paint);

	}


	private void addValue(float newY) {		
		final float popY = powerValues[Math.max(0, idx - windowSize) % windowSize];

		powerValues[idx++ % windowSize] = newY;

		if (newY > maxY || popY == maxY) {
			needRescale = true;
		} 

		repaint();
	}

	public void repaint() {
		postInvalidate();
	}

	@Override
	protected void onAttachedToWindow() {
		disableUpdate(false);
		super.onAttachedToWindow();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		disableUpdate(true);
		super.onDetachedFromWindow();
	}
	
	@Override
	public boolean isDisabled() {
		return disabled;		
	}
	
	@Override
	public void disableUpdate(boolean disable) {
		if (this.disabled != disable) {
			if (!disable) {
				roboStroke.getBus().addBusListener(privateBusListener);
			} else {
				roboStroke.getBus().removeBusListener(privateBusListener);
			}

			this.disabled = disable;
		}
	}
}
