package org.nargila.robostroke.ui.meters;

import org.nargila.robostroke.ui.LayoutMode;
import org.nargila.robostroke.ui.RSTextView;
import org.nargila.robostroke.ui.RSView;

public interface MeterView {
	
	public abstract void updateLayout(LayoutMode meterLayout);

	public RSTextView getSplitTimeTxt();
	public RSTextView getSpmTxt();
	public RSTextView getSpeedTxt();
	public RSTextView getAvgSpeedTxt();
	public RSView getAccuracyHighlighter();
	public RSView getStrokeModeHighlighter();
	public RSTextView getStrokeCountTxt();
	public RSTextView getSplitDistanceTxt();
	public RSTextView getTotalDistanceTxt();
	public RSTextView getTotalTimeTxt();
	public RSTextView getSplitStrokesTxt();
	public RSTextView getAvgSpmTxt();
}
