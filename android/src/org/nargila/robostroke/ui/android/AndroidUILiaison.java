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
package org.nargila.robostroke.ui.android;

import org.nargila.robostroke.ui.RSPaint;
import org.nargila.robostroke.ui.RSPath;
import org.nargila.robostroke.ui.UILiaison;

import android.graphics.Color;
import android.os.Handler;
import android.view.View;

public class AndroidUILiaison implements UILiaison {

	private final View owner;
	private final Handler mainHanlder = new Handler();

	
	public AndroidUILiaison(View owner) {
		this.owner = owner;
	}


	@Override
	public RSPath createPath() {
		return new AndroidPath();
	}

	
	@Override
	public void repaint() {
		owner.postInvalidate();			
	}

	@Override
	public RSPaint createPaint() {
		return new AndroidPaint();
	}	
	
	@Override
	public int getYellowColor() {
		return Color.YELLOW;
	}
	
	@Override
	public int getGreenColor() {
		return Color.GREEN;
	}
	
	@Override
	public int getRedColor() {
		return Color.RED;
	}
	
	@Override
	public Object getComponent() {
		return owner;
	}
	
	@Override
	public void setVisible(final boolean visible) {
		mainHanlder.post(new Runnable() {
			
			@Override
			public void run() {
				owner.setVisibility(visible ? View.VISIBLE : View.GONE);		
			}
		});
	}
}
