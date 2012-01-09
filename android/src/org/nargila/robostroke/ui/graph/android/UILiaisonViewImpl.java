package org.nargila.robostroke.ui.graph.android;

import org.nargila.robostroke.ui.RSPaint;
import org.nargila.robostroke.ui.RSPath;
import org.nargila.robostroke.ui.UILiaison;

import android.graphics.Color;
import android.view.View;

public class UILiaisonViewImpl implements UILiaison {

	private final View owner;
	
	
	public UILiaisonViewImpl(View owner) {
		this.owner = owner;
	}


	@Override
	public RSPath createPath() {
		return new RSPathImpl();
	}

	
	@Override
	public void repaint() {
		owner.postInvalidate();			
	}

	@Override
	public RSPaint createPaint() {
		return new RSPaintImpl();
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
}
