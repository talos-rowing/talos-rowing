package org.nargila.robostroke.ui.graph;

public enum RollOverlayType {
	BACKGROUND(1),
	TOP(0.2f),
	BOTTOM(0.2f);
	
	RollOverlayType(float clipHeightPercent) {
		this.clipHeightPercent = clipHeightPercent;
	}
	
	final float clipHeightPercent;
}