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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 
	private static final Logger logger = LoggerFactory.getLogger(HXMData.class);
	
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

	private BluetoothLeConncect btConnect;


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
		logger.debug("scheduling serviceRestartTask every 5 seconds");
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
		logger.debug("starting HXM data service");

		if (this.btConnect != null) {
			this.btCo
		}
		BluetoothLeConncect btConnect = new BluetoothLeConncect(this, new BluetoothLeConncect.HeartRateListener() {
			
			int count = 0;
			
			@Override
			public void onHeartRate(int rate) {
				Bundle data = new Bundle();
				
				data.putInt("bpm", rate);
				data.putInt("battery", 100);
				data.putInt("beatNumber", ++count);
				data.putString("deviceName", "-");

				Intent intent = new Intent(broadcastId);
				intent.putExtras(data);
				sendBroadcast(intent);				
				
			}
			@Override
			public void onError(String deviceName, HRMError e) {
				logger.error(String.format("error in HXM data thread (device: %s)", deviceName == null ? "none" : deviceName), e);
				restartOnError(deviceName, e);
			}
			@Override
			public void onConnect(String deviceName) {
				statusNotify("monitoring " + deviceName, "HRM Monitor",  "HRM Monitor", false);
			}
		});
		


		try {
			this.btConnect = btConnect;
			btConnect.initialize();
			dataInput.start();
			logger.info("HXM data input started");
		} catch (HRMError e) {
			logger.error("error starting HXM data service", e);
			restartOnError(null, e);
		} 
	}

	public void statusNotify(String msg, String contentTitle, String tickerText, boolean error) {
		long when = System.currentTimeMillis();

		
		
		CharSequence contentText = msg;
		Intent notificationIntent = new Intent(getClass().getName());
		PendingIntent contentIntent = PendingIntent.getService(this, 0, notificationIntent, 0);

		Notification.Builder builder =
			    new Notification.Builder(this)
			    .setSmallIcon(error ? R.drawable.noheart : R.drawable.heart)
			    .setContentTitle(contentTitle)
			    .setContentText(contentText)
			    .setContentIntent(contentIntent)
			    .setWhen(when)
			    .setTicker(tickerText);

		Notification notification = builder.build();				
		
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