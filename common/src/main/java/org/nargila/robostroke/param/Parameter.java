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

/**
 * Parameter object with param ID, value and default value
 * @author tshalif
 *
 * @param <T> data type of parameter value
 */
public abstract class Parameter<T> {
	
	public static class BOOLEAN extends Parameter<Boolean> {

		public BOOLEAN(String id, String name, String description, String category, ParameterLevel level,
				boolean defaultValue) {
			super(id, name, description, category, level, defaultValue);
		}

		@Override
		protected Boolean convertFromString(String val) {
			return new Boolean(val);
		}
	}
	
	public static class STRING extends Parameter<String> {

		
		public STRING(String id, String name, String description, String category, ParameterLevel level,
				String defaultValue) {
			super(id, name, description, category, level, defaultValue);
		}

		@Override
		protected String convertFromString(String val) {
			return val;
		}
	}
	
	public static class FLOAT extends Parameter<Float> {

		public FLOAT(String id, String name, String description, String category, ParameterLevel level,
				Float defaultValue) {
			super(id, name, description, category, level, defaultValue);
		}

		@Override
		protected Float convertFromString(String val) {
			return new Float(val);
		}
	}
	
	public static class INTEGER extends Parameter<Integer> {

		public INTEGER(String id, String name, String description, String category, ParameterLevel level,
				Integer defaultValue) {
			super(id, name, description, category, level, defaultValue);
		}
		@Override
		protected Integer convertFromString(String val) {
			return new Integer(val);
		}
	}
	public static class LONG extends Parameter<Long> {

		public LONG(String id, String name, String description, String category, ParameterLevel level,
				Long defaultValue) {
			super(id, name, description, category, level, defaultValue);
		}
		@Override
		protected Long convertFromString(String val) {
			return new Long(val);
		}
	}
	
	private final String id;
	private final String name;
	private final String description;
	private final String category;
	private final ParameterLevel level;
	private final T defaultValue;
	private T value;
	
	ParameterService parameterService; // is set by ParameterService when this param instance is registered
	
	public Parameter(String id, String name, String description, String category,
			ParameterLevel level, T defaultValue) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.category = category;
		this.level = level;
		this.defaultValue = value = defaultValue;
	}
	
	public Parameter(String id) {
		this(id, null, null, null, null, null);
	}
	
	public T getValue() {
		return value;
	}
	
	
	public final void setValue(T value) {
		parameterService.setParam(this, value); 
	}
	
	@SuppressWarnings("unchecked")
	boolean setParameterValue(Object value) {
		if (!this.value.equals(value)) {
			T newValue = (T) value;
			this.value = newValue;
			return true;
		}
		
		return false;
	}
	
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public String getCategory() {
		return category;
	}
	public ParameterLevel getLevel() {
		return level;
	}
	public T getDefaultValue() {
		return defaultValue;
	}
	
	protected abstract T convertFromString(String val);
	
	public String convertToString() {
		return value.toString();
	}
	
	protected T[] makeChoices() {
		return null;
	}
	
	public final Object[] getChoices() {
		
		LinkedHashSet<T> res = new LinkedHashSet<T>();
		res.add(getDefaultValue());
		res.add(getValue());
		
		T[] choices = makeChoices();
		
		if (choices != null) {
			res.addAll(Arrays.asList(choices));
		}
		
		return res.toArray();
	}
}
