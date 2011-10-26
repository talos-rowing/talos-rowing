package org.nargila.robostroke.android.graph;

import org.nargila.robostroke.ui.PaintStyle;
import org.nargila.robostroke.ui.RSPaint;

import android.graphics.Paint;
import android.graphics.Paint.Style;

public class RSPaintProxy extends Paint implements RSPaint {

	@Override
	public void setStyle(PaintStyle style) {
		Style s = Style.valueOf(style.name());
		super.setStyle(s);			
	}
}
