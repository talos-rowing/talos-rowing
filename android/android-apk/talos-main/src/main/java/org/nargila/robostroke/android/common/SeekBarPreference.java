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
package org.nargila.robostroke.android.common;

import org.nargila.robostroke.android.app.R;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference { 
	
	private enum ValueType {
		FLOAT,
		INTEGER,
	}
	
	private static final String XML_SCHEMA = "http://nargila.org/android";
	private static final String XML_ANDROID = "http://schemas.android.com/apk/res/android";
	private final Context context; 
	private final double minValue;
	private final double maxValue;
	private double value;
	private final int seekRange;
	private final double defaultValue;
	private final String displayFormat;
	private final ValueType valueType;
	
	public SeekBarPreference(Context context, AttributeSet attrs) { 
		super(context, attrs); 
		this.context = context; 
		
		displayFormat = get(XML_SCHEMA, attrs, "displayFormat", "%.03f");
		seekRange = attrs.getAttributeIntValue(XML_SCHEMA, "seekRange", 100);
		minValue = Double.parseDouble(get(XML_SCHEMA, attrs, "minValue", 0+""));
		maxValue = Double.parseDouble(get(XML_SCHEMA, attrs, "maxValue", (minValue + seekRange)+""));
		defaultValue = Double.parseDouble(get(XML_ANDROID, attrs, "defaultValue", minValue+""));
		valueType = ValueType.valueOf(get(XML_SCHEMA, attrs, "valueType", "FLOAT"));
	} 
	
	private String get(String namespace, AttributeSet attrs, String name, String defaultValue) {
		String val = attrs.getAttributeValue(namespace, name);
		
		return null == val ? defaultValue : val;
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.seekbar_preference_dialog, null);
		
		final TextView text = (TextView) view.findViewById(R.id.text);
		final SeekBar seekbar = (SeekBar) view.findViewById(R.id.seekbar);
		final ImageView reset = (ImageView) view.findViewById(R.id.reset);
		
		seekbar.setMax(seekRange); 
		
		value = Double.parseDouble(getPersistedString(defaultValue + ""));

		text.setText(String.format(displayFormat, value));

		reset.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				seekbar.setProgress(calcProgress(defaultValue));
				value = defaultValue;				
				text.setText(String.format(displayFormat, value));
				reset.setVisibility(View.INVISIBLE);
			}
		});
		
		if (value == defaultValue) { // don't show reset button if defaultValue
			reset.setVisibility(View.INVISIBLE);			
		}
		
		seekbar.setProgress(calcProgress(value));
		
		seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				
				if (fromUser) {
					value = calcValue(progress);
					text.setText(String.format(displayFormat, value));

					if (value != defaultValue) { // display reset value when value changes
						reset.setVisibility(View.VISIBLE);			
					}
				}
			}
		});
		
		builder.setView(view); 
		super.onPrepareDialogBuilder(builder); 
	} 
	private double calcValue(int progress) {
		return ((double)progress / seekRange) * (maxValue - minValue) + minValue;
	}
	
	private int calcProgress(double value) {
		return (int) (seekRange * ((value - minValue) / (maxValue - minValue)));
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) { 
		if(positiveResult){
			String sval;
			
			switch (valueType) {
			case INTEGER:
				sval = "" + Math.round(value);
				break;
				default:
					sval = ""+ value;
			}
			
			persistString(sval);
			
			if (shouldCommit()) {
				getEditor().commit();
			}
		} 
	} 
} 
