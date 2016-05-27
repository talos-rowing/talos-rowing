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

package org.nargila.android.hxm;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class HXMData extends Service implements HXMConstants {
 
	private static int SERVICE_NOTIFICATION_ID = 1;
	
	private final Handler mainHandler = new Handler();
	
	private HXMHeartDataInput dataInput;
	private final String broadcastId = getClass().getName();
	private final Timer timer = new Timer("HXM Service Restart Timer", true);
	
	private ErrorCode lastErrorCode = ErrorCode.NONE;
	
	class ServiceRestartTask extends TimerTask {
		
		@Override
		public void run() {
			mainHandler.post(new Runnable() {
				
				@Override
				public void run() {
					startHRMService();
				}
			});
		}
	};
	
	private ServiceRestartTask serviceRestartTask = new ServiceRestartTask();
	private NotificationManager notificationManager;


	@Override
	public IBinder onBind(Intent intent) {
		return null; // not using ipc... dont care about this method
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		startHRMService();
	}
   
	private void restartHRMService() {
		if (D) Log.d(TAG, "scheduling serviceRestartTask every 5 seconds");
		serviceRestartTask.cancel();
		serviceRestartTask = new ServiceRestartTask();
		timer.schedule(serviceRestartTask, 5000);
	}

	@Override
	public void onDestroy() {

		timer.cancel();
		serviceRestartTask.cancel();
		stopHRMService();
		notificationManager.cancel(SERVICE_NOTIFICATION_ID);
		super.onDestroy();
	}
	
	private void startHRMService() {
		if (D) Log.d(TAG, "starting HXM data service");

		HXMHeartDataInput tmp = new HXMHeartDataInput(new HXMHeartDataInput.BPMListener() {

			@Override
			public void onError(String deviceName, HRMError e) {
				if (D) Log.e(TAG, String.format("error in HXM data thread (device: %s)", deviceName == null ? "none" : deviceName), e);
				restartOnError(deviceName, e);
			}

			@Override
			public void onBPMUpdate(Bundle data) {
				Intent intent = new Intent(broadcastId);
				intent.putExtras(data);
				sendBroadcast(intent);				
			}

			@Override
			public void onConnect(String deviceName) {
				statusNotify("monitoring " + deviceName, "HRM Monitor",  "HRM Monitor", false);
			}
		});

		try {
			dataInput = tmp;
			dataInput.start();
			Log.i(TAG, "HXM data input started");
		} catch (HRMError e) {
			if (D) Log.e(TAG, "error starting HXM data service", e);
			restartOnError(null, e);
		} 
	}

	public void statusNotify(String msg, String contentTitle, String tickerText, boolean error) {
		long when = System.currentTimeMillis();

		Notification notification = new Notification(error ? R.drawable.noheart : R.drawable.heart, tickerText, when);
		
		
		CharSequence contentText = msg;
		Intent notificationIntent = new Intent(getClass().getName());
		PendingIntent contentIntent = PendingIntent.getService(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);	
		
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notificationManager.notify(SERVICE_NOTIFICATION_ID, notification);		
	}
	
    private void restartOnError(String deviceName, HRMError e) {
    	
    	if (lastErrorCode != e.code) {
    		statusNotify(e.getMessage(), "HRM Error",  "HRM Error", true);
    		lastErrorCode = e.code;
    	}
    	
    	Intent intent = new Intent(broadcastId);
    	intent.putExtra("errorCode", e.code.ordinal());
    	intent.putExtra("error", e.getMessage());
    	
    	if (deviceName != null) {
    		intent.putExtra("deviceName", deviceName);
    	}
    	
    	sendBroadcast(intent);
    	restartHRMService();
    }
    
    private void stopHRMService() {
    	if (null != dataInput) {
    		dataInput.stop();
    	}			
    }
}