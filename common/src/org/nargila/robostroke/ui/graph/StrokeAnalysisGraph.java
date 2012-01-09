package org.nargila.robostroke.ui.graph;

import java.util.concurrent.TimeUnit;

import org.nargila.robostroke.input.SensorDataSink;
import org.nargila.robostroke.ui.PaintStyle;
import org.nargila.robostroke.ui.RSCanvas;
import org.nargila.robostroke.ui.RSPaint;
import org.nargila.robostroke.ui.RSRect;
import org.nargila.robostroke.ui.UILiaison;
import org.nargila.robostroke.ui.graph.XYSeries.XMode;

/**
 * subclass of LineGraphView for setting stroke specific parameters
 */
public class StrokeAnalysisGraph extends LineGraph {
	
	private static final long MAX_TIME_CAPTURE = TimeUnit.SECONDS.toNanos(10);
	private long timeCaptureStart;
	private final RollOverlayType rollOverlayType = RollOverlayType.TOP;
	
	
	private static final float Y_SCALE = 8f;
	private static final float INCR = 1f;
	private final CyclicArrayXYSeries accelSeries = new CyclicArrayXYSeries(XMode.GROWING, new XYSeries.Renderer(uiLiaison.createPaint()));
	private final CyclicArrayXYSeries rollPanelSeries = new CyclicArrayXYSeries(XMode.GROWING, new XYSeries.Renderer(uiLiaison.createPaint())) {
		{
			setIndependantYAxis(true);				
		}
	};
	
	private final RSPaint rollGraphPaint = uiLiaison.createPaint();
	
	
	private final CyclicArrayXYSeries rollSeries;
	
	private final RollGraphOverlay rollGraph;
	private final int next = 1;
	
	public StrokeAnalysisGraph(UILiaison factory) {
		super(factory, Y_SCALE, INCR, null);

		rollGraphPaint.setStyle(PaintStyle.STROKE);
		rollGraphPaint.setColor(uiLiaison.getYellowColor());
		rollGraphPaint.setAlpha(170);

		rollSeries = new CyclicArrayXYSeries(XMode.GROWING, new XYSeries.Renderer(rollGraphPaint)) {
			{
				setIndependantYAxis(true);
				setyAxisSize(Y_SCALE);
				setxRange(0);
			}
		};

		multySeries = new MultiXYSeries(0, XYSeries.XMode.GROWING) {
			@Override
			public double getxRange() {
				return accelSeries.getMaxX() - accelSeries.getMinX();
			}
		};			

		multySeries.addSeries(rollPanelSeries, false);
		multySeries.addSeries(accelSeries, false);
		multySeries.addSeries(rollSeries, false);

		rollGraph = new RollGraphOverlay(factory, multySeries);
	}

	@Override
	public void setXRange(double val) {
		// disable external call to setXRange()
	}
	
	
	@Override
	protected void drawSeries(RSCanvas canvas, RSRect rect, double xAxisSize,
			double yAxisSize, XYSeries series) {
		
		if (series != rollPanelSeries) { 
			super.drawSeries(canvas, rect, xAxisSize, yAxisSize, series);
		}
		
	}
	
	@Override
	protected void drawGraph(RSCanvas canvas, RSRect rect, double xAxisSize, double yAxisSize) {
		
		RSRect rollBarsRect = new RSRect(rect);
		
		int rollBarsHeight = (int) (rollOverlayType.clipHeightPercent * rect.height());
		
		switch (rollOverlayType) {
		case BACKGROUND:
			// nothing to do
			break;
		case BOTTOM:
			rollBarsRect.top = rollBarsRect.bottom - rollBarsHeight;
			rect.bottom -= rollBarsHeight;
			break;
		case TOP:
			rollBarsRect.bottom = rollBarsRect.top + rollBarsHeight;
			rect.top += rollBarsHeight;
			break;
		}
		
		rollGraph.drawRollPanels(canvas, rollBarsRect, xAxisSize);
		
		super.drawGraph(canvas, rect, xAxisSize, yAxisSize);
	}
	
	
	@Override
	public void reset() {
		synchronized (multySeries) {
			rollGraph.reset();
			timeCaptureStart = 0;
			super.reset();
		}
	}
	
	private boolean checkCaptureTimeInRange(long timestamp) {
		
		if (timeCaptureStart == 0) {
			timeCaptureStart = timestamp;
			return true;
		} 
		
		return (Math.abs(timestamp - timeCaptureStart) < MAX_TIME_CAPTURE);
	}
	
	SensorDataSink rollSink = new SensorDataSink() {
		
		@Override
		public void onSensorData(long timestamp, Object value) {
			if (!checkCaptureTimeInRange(timestamp)) {
				return;
			}
			
			rollGraph.onSensorData(timestamp, value);
		}
		
	};
	
	
	public SensorDataSink getRollSink() {
		return rollSink;
	}
	
	SensorDataSink accelSink = new SensorDataSink() {
		
		@Override
		public void onSensorData(long timestamp, Object value) {
			if (!checkCaptureTimeInRange(timestamp)) {
				return;
			}
			float[] values = (float[]) value;
			accelSeries.add(timestamp, values[0]);
		}
	};

	public SensorDataSink getAccelSink() {
		return accelSink;
	}
}