/* 
 * Copyright (C) 2010 Tal Shalif
 * 
 * This file is part of robostroke HRM.
 *
 * This code is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nargila.android.hxm.monitor;

import java.util.HashMap;
import java.util.Map;

import org.nargila.android.hxm.monitor.filter.LowpassFilter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HRMMonitor extends Activity {
	public static String HRM_SERVICE_ACTION = "org.nargila.android.hxm.HXMData";
	
	private final LowpassFilter batteryLevelStabilizer = new LowpassFilter(1.0f);
	private float batteryLevel = -1;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        final ProgressBar batteryProgress = (ProgressBar) findViewById(R.id.battery_progress);
        
        final HashMap<String, TextView> FIELDS = new HashMap<String, TextView>() {
        	{
        		put("bpm", (TextView) findViewById(R.id.bpm));
        		put("battery", (TextView) findViewById(R.id.battery));
        		put("deviceName", (TextView) findViewById(R.id.devicename));
        		put("beatNumber", (TextView) findViewById(R.id.beatnumber));
        		put("errorCode", (TextView) findViewById(R.id.errorcode));
        		put("error", (TextView) findViewById(R.id.error));
        	}
        };
            		   		
   		registerReceiver(new BroadcastReceiver() {

   			@Override
   			public void onReceive(Context context, Intent intent) {
   				Bundle data = intent.getExtras();
   				int battery = -1;
   				boolean hasError = data.get("error") != null;
   				
   				for (Map.Entry<String, TextView> entry: FIELDS.entrySet()) {				
   					Object val = data.get(entry.getKey());

   					TextView view = entry.getValue();

   					view.setText((null == val) ? "" : val.toString());	
   					
   					if (val != null && entry.getKey().equals("battery")) {
   						battery = (Integer)val;
   					}
   					
   				}
   				
   				if (battery != -1) {
   					batteryLevel = batteryLevelStabilizer.filter(new float[]{battery})[0];

   					if (batteryLevel > 0) {
   						batteryLevelStabilizer.setFilteringFactor(0.02f);
   					}
   				}
   				
   				batteryProgress.setProgress(hasError ? 0 : (int) batteryLevel);
   			}
   		}, new IntentFilter(HRM_SERVICE_ACTION));
    		
   		startService(new Intent(HRM_SERVICE_ACTION));
        
    }
}