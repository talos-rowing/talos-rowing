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
package org.nargila.robostroke.android.app;

import java.util.concurrent.TimeUnit;

import org.nargila.robostroke.RoboStrokeEventBus;
import org.nargila.robostroke.StrokeEvent.Type;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

public class HXMDataReceiver extends BroadcastReceiver implements RoboStrokeConstants {
	private boolean hadError;
	private final RoboStrokeEventBus bus;
	
	public HXMDataReceiver(RoboStrokeEventBus bus) {
		this.bus = bus;
	}
	@Override
	public void onReceive(Context context, Intent intent) {
				
		Bundle data = intent.getExtras();
		
		String error = data.getString("error");
		
		if (null != error) {
			if (!hadError) {
				int errorCode = data.getInt("errorCode");
			
				Log.w(TAG, String.format("received HRM errorCode %d: %s", errorCode, error));
				
				hadError = true;
			}
			
		} else {
			int bpm = data.getInt("bpm");
			
			hadError = false;

			bus.fireEvent(Type.HEART_BPM, TimeUnit.MILLISECONDS.toNanos(SystemClock.uptimeMillis()), bpm);
		}
		
	}
}
