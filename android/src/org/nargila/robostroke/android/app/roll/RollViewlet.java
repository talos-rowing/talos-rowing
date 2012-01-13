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
package org.nargila.robostroke.android.app.roll;

import org.nargila.robostroke.android.app.R;
import org.nargila.robostroke.common.NumberHelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

class RollViewlet  {
	
	private Handler mHandler = new Handler();
	private static final int LEFT_SIDE = -1;
	private static final int RIGHT_SIDE = 1;
	
	private float roll;
	private View bars;
	private TextView text_primary;
	private TextView text_secondary;
	private TextView type_text;
	private float range = 9.9f;
	private Paint rightPaint;
	private Paint leftPaint;
	private Paint middleLinePaint;
	private Paint backgroundPaint;
	private final View view;
	
	RollViewlet(Context context, View view) {
		this.view = view;
		
		bars = new View(context) {
			protected void onDraw(Canvas canvas) {
				Rect bounds = canvas.getClipBounds();				
				fillBackground(canvas, bounds);
				
				drawBar(canvas, bounds, RIGHT_SIDE);
				drawBar(canvas, bounds, LEFT_SIDE);
			}
		};
		
		text_primary = (TextView) view.findViewById(R.id.roll_text_primary);
		text_secondary = (TextView) view.findViewById(R.id.roll_text_secondary);
		type_text = (TextView) view.findViewById(R.id.roll_type_text);
		FrameLayout frame = (FrameLayout) view.findViewById(R.id.roll_canvas_frame);
		frame.addView(bars, new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.FILL_PARENT, 
				FrameLayout.LayoutParams.FILL_PARENT));
		
		rightPaint = new Paint();
		rightPaint.setColor(Color.GREEN);
		leftPaint = new Paint();
		leftPaint.setColor(Color.RED);
		backgroundPaint = new Paint();
		backgroundPaint.setColor(Color.BLACK);
		middleLinePaint = new Paint();
		middleLinePaint.setColor(Color.YELLOW);
	}

	void hide(boolean yes) {
		if (yes) {
			view.setVisibility(View.GONE);
		} else {
			view.setVisibility(View.VISIBLE);
		}
	}
	
	void setLabel(String s) {
		int visibility;
		
		if (s == null) {
			visibility = View.GONE;
		} else {
			type_text.setText(s);
			visibility = View.VISIBLE;
		}
		
		type_text.setVisibility(visibility);
	}
	
	public void setRoll(final float ... rollValues) {
		
	    this.roll = NumberHelper.validRange(rollValues[0], -range, range);
		
		mHandler.post(new Runnable() {
			public void run() {
				text_primary.setText(String.format("%02.1f", Math.abs(roll)));
				
				if (rollValues.length > 1) {
					float roll2 = NumberHelper.validRange(rollValues[1], -range, range); 
					text_secondary.setText(String.format("%02.1f", Math.abs(roll2)));
					text_secondary.setTextColor(roll2 > 0 ? Color.GREEN : Color.RED);
					text_secondary.setVisibility(View.VISIBLE);
				} else {
					text_secondary.setVisibility(View.INVISIBLE);					
				}
			}
		});

		bars.postInvalidate();
	}
	
	private void drawBar(Canvas canvas, Rect bounds, int side) {
		Paint paint = side == LEFT_SIDE ? leftPaint : rightPaint;
		
		float yScale = bounds.height() / (range * 2);
		float y = range - (side * roll);
		float pixelHeight = y * yScale;

		float width = bounds.right - bounds.left;
		float left = bounds.left;
		float right = left + width / 2;
		float top = bounds.top;
		float bottom = top + pixelHeight;
		
		float dy = bounds.height() - pixelHeight;
		float dx = side == RIGHT_SIDE ? 0 : width / 2;
		
		RectF rect = new RectF(left, top, right, bottom);
		rect.offset(dx, dy);
		canvas.drawRect(rect, paint);
	}
	
	private void fillBackground(Canvas canvas, Rect bounds) {
		canvas.drawRect(bounds, backgroundPaint);
	}
}
