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
package org.nargila.robostroke.param;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Parameter object with param ID, value and default value
 * @author tshalif
 *
 * @param <T> data type of parameter value
 */
public class Parameter {
	
	private final ParameterInfo info;
	
	private Object value;
	private AtomicReference<Object> savedValue;
	
	ParameterService parameterService; // is set by ParameterService when this param instance is registered
	
	public Parameter(ParameterInfo info) {
		
		this.info = info;
		
		value = info.getDefaultValue();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getValue() {
		return (T) value;
	}
	
	/**
	 * save current value
	 */
	public synchronized void saveValue() {
		savedValue = new AtomicReference<Object>(this.value);		
	}
	
	/**
	 * restore value from savedValue
	 */
	public synchronized void restoreValue() {
		
		if (savedValue != null) {
			Object tmp = savedValue.get();
			savedValue = null;
			setValue(tmp);
		}
	}
	
	public final void setValue(Object value) {
		parameterService.setParam(this, value); 
	}
	
	boolean setParameterValue(Object value) {
		
		boolean hasChanged;
		
		if (this.value == null || value == null) {
			hasChanged = this.value != value; 
		} else {
			hasChanged = !this.value.equals(value);
		}
		
		this.value = value;
		
		return hasChanged;
	}
	
	public String getId() {
		return info.getId();
	}
	public String getName() {
		return info.getName();
	}
	public String getDescription() {
		return info.getDescription();
	}
	public String getCategory() {
		return info.getCategory();
	}
	public ParameterLevel getLevel() {
		return info.getLevel();
	}
	@SuppressWarnings("unchecked")
	public <T> T getDefaultValue() {
		return (T) info.getDefaultValue();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T convertFromString(String val) {
		return (T) info.convertFromString(val);
	}
	
	public String convertToString() {
		return info.convertToString(value);
	}
	
	protected Object[] makeChoices() {
		return info.makeChoices();
	}
	
	public final Object[] getChoices() {
		
		LinkedHashSet<Object> res = new LinkedHashSet<Object>();
		res.add(getDefaultValue());
		res.add(getValue());
		
		Object[] choices = makeChoices();
		
		if (choices != null) {
			res.addAll(Arrays.asList(choices));
		}
		
		return res.toArray();
	}
}
