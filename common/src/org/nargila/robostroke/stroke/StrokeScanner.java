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
package org.nargila.robostroke.stroke;


import org.nargila.robostroke.RoboStrokeEventBus;
import org.nargila.robostroke.StrokeEvent;
import org.nargila.robostroke.common.filter.LowpassFilter;
import org.nargila.robostroke.input.SensorDataFilter;
import org.nargila.robostroke.stroke.HalfSinoidDetector.Dir;

public abstract class StrokeScanner  extends SensorDataFilter {

	private final LowpassFilter amplitudeFilter;
	private final HalfSinoidDetector decelerationAmplitudeDetector = new HalfSinoidDetector(Dir.DOWN);

	private final HalfSinoidDetector accelerationAmplitudeDetector = new HalfSinoidDetector(Dir.UP);
	
	protected final RoboStrokeEventBus bus;
	

	public StrokeScanner(RoboStrokeEventBus bus, float amplitudeFilterFactor) {
		this.bus = bus;
		amplitudeFilter = new LowpassFilter(amplitudeFilterFactor);		
	}


	@Override
	protected Object filterData(long timestamp, Object value) {
		
		float[] values = (float[]) value;
		
		float[] res = amplitudeFilter.filter(values);		
		
		float filteredAmplitude = res[0];
	
		switch (accelerationAmplitudeDetector.add(filteredAmplitude)) {
		case VALID_EXIT:
			onDropBelow(timestamp, accelerationAmplitudeDetector.maxVal);
			break;
		case TESHOLD_PASS:
			onAccelerationTreshold(timestamp, filteredAmplitude);
			break;
		}

		
		switch (decelerationAmplitudeDetector.add(filteredAmplitude)) {
		case VALID_EXIT:
			onRiseAbove(timestamp, decelerationAmplitudeDetector.maxVal);
			break;
		case TESHOLD_PASS:
			onDecelerationTreshold(timestamp, filteredAmplitude);
			break;
		}

		
		return res;		
	}


	protected void onDecelerationTreshold(long timestamp, float amplitude) {
		bus.fireEvent(StrokeEvent.Type.STROKE_DECELERATION_TRESHOLD, timestamp, amplitude);		
	}
	
	protected void onAccelerationTreshold(long timestamp, float amplitude) {
		bus.fireEvent(StrokeEvent.Type.STROKE_ACCELERATION_TRESHOLD, timestamp, amplitude);		
	}

	protected void onDropBelow(long timestamp, float maxVal) {
		bus.fireEvent(StrokeEvent.Type.STROKE_DROP_BELOW_ZERO, timestamp, maxVal);		
	}
	
	protected void onRiseAbove(long timestamp, float minVal) {
		bus.fireEvent(StrokeEvent.Type.STROKE_RISE_ABOVE_ZERO, timestamp, minVal);
	}

	public float getAmplitudeFiltering() {
		return amplitudeFilter.getFilteringFactor();
	}
	
	public void setAmplitudeFiltering(float factor) {
		amplitudeFilter.setFilteringFactor(factor);
	}
	
	public float getAmplitudeChangeAcceptFactor() {
		return decelerationAmplitudeDetector.getAmplitudeChangeAcceptFactor();
	}
	
	public void setAmplitudeChangeAcceptFactor(float factor) {
		decelerationAmplitudeDetector.setAmplitudeChangeAcceptFactor(factor);
	}
	
	
	public float getAmplitudeChangeDamperFactor() {
		return decelerationAmplitudeDetector.getAmplitudeChangeDamperFactor();
	}

	public void setAmplitudeChangeDamperFactor(float factor) {
		decelerationAmplitudeDetector.setAmplitudeChangeDamperFactor(factor);
	}

	public float getMinAmplitude() {
		return decelerationAmplitudeDetector.getMinAmplitude();
	}

	public void setMinAmplitude(float minAmplitude) {
		decelerationAmplitudeDetector.setMinAmplitude(minAmplitude);
	}
}