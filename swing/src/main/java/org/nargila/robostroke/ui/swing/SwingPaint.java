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
package org.nargila.robostroke.ui.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import org.nargila.robostroke.ui.PaintStyle;
import org.nargila.robostroke.ui.RSPaint;

public class SwingPaint implements RSPaint {

	boolean antiAlias;
	
	Color color = Color.BLACK;

	Stroke stroke = new BasicStroke(1);

	PaintStyle paintStyle;
	
	public void setARGB(int a, int r, int g, int b) {
		
		if (a < 0 || a > 255) {
			throw new IllegalArgumentException("HDIGH!");			
		}
		
		color = new Color(r, g, b, a);		
	}

	public void setAlpha(int abs) {
		setARGB(abs, color.getRed(), color.getGreen(), color.getBlue());
	}

	public void setAntiAlias(boolean antiAlias) {
		this.antiAlias = antiAlias;
	}

	public void setColor(int color) {
		this.color = new Color(color);
	}

	public void setStrokeWidth(float width) {
		this.stroke = new BasicStroke(width);

	}

	public Stroke getStroke() {
		return stroke;
	}
	
	public void setStyle(PaintStyle style) {
		this.paintStyle = style;

	}

}
