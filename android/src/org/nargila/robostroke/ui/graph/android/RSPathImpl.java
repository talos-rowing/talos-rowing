package org.nargila.robostroke.ui.graph.android;

import org.nargila.robostroke.ui.RSPath;

import android.graphics.Path;

public class RSPathImpl extends Path implements RSPath {
	
	private final SwingPath impl = new SwingPath();
	
	@Override
	public void moveTo(float x, float y) {
		impl.moveTo(x, y);
		super.moveTo(x, y);
	}
	
	@Override
	public void lineTo(float x, float y) {
		impl.lineTo(x, y);
		super.lineTo(x, y);
	}
}
