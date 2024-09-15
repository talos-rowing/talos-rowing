/*
 * Copyright (c) 2024 Tal Shalif
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

package org.nargila.robostroke.common.filter;

/**
 * base class for low/high pass sensor data filter implementations
 * @author tshalif
 *
 */
public abstract class PassFilterBase {
	protected static final float FILTER_FACTOR = 0.1f;
	protected float filteringFactor;


	protected float[] filteredValues;

	/**
	 * default constructor
	 */
	protected PassFilterBase() {
		this(FILTER_FACTOR);
	}
	
	/**
	 * constructor with initial filteringFactor
	 * @param filteringFactor
	 */
	protected PassFilterBase(float filteringFactor) {
		this.filteringFactor = filteringFactor;
	}
	
	/**
	 * get current filtering facgtor
	 * @return current filtering factor
	 */
	public float getFilteringFactor() {
		return filteringFactor;
	}
	
	/**
	 * set filtering factor
	 * @param filteringFactor new filtering factor
	 */
	public void setFilteringFactor(float filteringFactor) {
		if (filteringFactor > 1.0f || filteringFactor <= 0.0f) {
			throw new IllegalArgumentException("filteringFactor value must be between 0 and 1");
		}
		this.filteringFactor = filteringFactor;
	}

	/**
	 * filter input data  - filtering algorithm is implementation dependant.
	 * filteringFactor may of may not be used by sub-classes.
	 * @param values input data
	 * @return filtered data
	 */
	public float[] filter(float[] values) {
		if (this.filteredValues == null) {
			this.filteredValues = new float[values.length];
		} else if (this.filteredValues.length != values.length) {
			throw new IllegalArgumentException("values array length passed to SensorFilter.filter() has changed"); 
		}

		return doFilter(values);
	}


	public float[] getFilteredValues() {
		return filteredValues;
	}

	public void setFilteredValues(float[] filteredValues) {
		this.filteredValues = filteredValues;
	}

	public void reset() {
		setFilteredValues(null);
	}
	
	/**
	 * do actual filtering
	 * @param values data to filter
	 * @return filtered data
	 */
	protected abstract float[] doFilter(float[] values);
}
