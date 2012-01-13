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
package org.nargila.robostroke.android.common;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class AutoFontSizeTextView extends TextView {

	private static final Float RIDICULOUSLY_LARGE_TEXT_SIZE = 1000000F;
	
	private static final String XML_SCHEMA = "http://nargila.org/android";

	static final float TEXT_HORIZONTAL_MARGIN_FACTOR = 1.25f;
	
	private final float maxTextSize;
	
	public AutoFontSizeTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		
		maxTextSize = attrs.getAttributeFloatValue(XML_SCHEMA, "maxTextSize", RIDICULOUSLY_LARGE_TEXT_SIZE);
	}

	public AutoFontSizeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		maxTextSize = attrs.getAttributeFloatValue(XML_SCHEMA, "maxTextSize", RIDICULOUSLY_LARGE_TEXT_SIZE);
	}

	public AutoFontSizeTextView(Context context) {
		super(context);
		
		maxTextSize = -1;
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int before,
			int after) {
		
		super.onTextChanged(text, start, before, after);
		autoFitText(getWidth(), getHeight());
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		autoFitText(w, h);
	}

	private void autoFitText(float w, int h) {  

		if (w > 0 && h > 0) {
			final String s = getText().toString();

			TextPaint paint = getPaint();
			
			float currentWidth = paint.measureText(s);
						
			while ((currentWidth * TEXT_HORIZONTAL_MARGIN_FACTOR) > w  || calcFontSize(paint) > h || getTextSize() > maxTextSize) {
				setTextSize(TypedValue.COMPLEX_UNIT_SP, getTextSize() - 0.25f);
				currentWidth = paint.measureText(s);				
			}
			

			while (w > (currentWidth * TEXT_HORIZONTAL_MARGIN_FACTOR) && h > calcFontSize(paint) && getTextSize() < maxTextSize) {
				setTextSize(TypedValue.COMPLEX_UNIT_SP, getTextSize() + 0.25f);
				currentWidth = paint.measureText(s);				
			}
			
		}
	}

	private float calcFontSize(TextPaint paint) {
		return paint.descent() - paint.ascent();
	}
}
