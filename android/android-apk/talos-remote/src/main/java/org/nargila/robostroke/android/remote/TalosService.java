/*
 * Copyright (c) 2013 Tal Shalif
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

package org.nargila.robostroke.android.remote;

import org.nargila.robostroke.data.SessionRecorderConstants;
import org.nargila.robostroke.data.remote.AutoData;
import org.nargila.robostroke.data.remote.DataRemote;
import org.nargila.robostroke.data.remote.DataRemote.DataRemoteError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.IBinder;

public abstract class TalosService extends Service {
 
	private static final Logger logger = LoggerFactory.getLogger(TalosService.class);
	
	private static int SERVICE_NOTIFICATION_ID = 1;
	
	private NotificationManager notificationManager;

	protected int port;

	protected String host;
	
	private DataRemote impl;

	private boolean started;

	private MulticastLock multicastLock;
	

	protected TalosService() {
	}
	
	@Override
	public void onStart(Intent intent, int startId) {		
	
		super.onStart(intent, startId);
		
		port = intent.getIntExtra("port", SessionRecorderConstants.BROADCAST_PORT);
		
		host = intent.getStringExtra("host");
		
		if (host == null) {
			host = SessionRecorderConstants.BROADCAST_HOST;
		}

		startService();

	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null; // not using ipc... dont care about this method
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
	}
   
	@Override
	public void onDestroy() {

		stopService();
		
		notificationManager.cancel(SERVICE_NOTIFICATION_ID);
		
		super.onDestroy();
	}
	
	
	private synchronized void startService() {

		if (!started) {
			
			logger.info("starting {} service", getClass().getSimpleName());
			
			try {
				
				impl = makeImpl(host, port);
				
				if (impl instanceof AutoData) {
					if (((AutoData)impl).isMulticast()) {
						
						WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
						
						multicastLock = wifi.createMulticastLock(getClass().getSimpleName());
						multicastLock.setReferenceCounted(false);
						multicastLock.acquire();
					}
				}
				impl.start();
			} catch (DataRemoteError e) {
				String msg = "failed to start " + getClass().getSimpleName();
				logger.error(msg, e);
				statusNotify(msg, "error", "service start failed", true);
				
				return;
			}

			afterStart();
			
			started = true;

		}
	}
	

	protected abstract DataRemote makeImpl(String host, int port) throws DataRemoteError;
	
	protected abstract void afterStart() ;

	protected abstract void beforeStop() ;

	public void statusNotify(String msg, String contentTitle, String tickerText, boolean error) {
		
		long when = System.currentTimeMillis();

		Notification notification = new Notification(error ? R.drawable.icon : android.R.drawable.ic_dialog_alert, tickerText, when);
		
		
		CharSequence contentText = msg;
		Intent notificationIntent = new Intent(getClass().getName());
		PendingIntent contentIntent = PendingIntent.getService(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);	
		
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notificationManager.notify(SERVICE_NOTIFICATION_ID, notification);		
	}
	
    private synchronized void stopService() {
    	if (started) {
    		try {
				beforeStop();
				impl.stop();
			} catch (Exception e) {
				logger.error("failed to stop " + getClass().getSimpleName(), e);
			}

    		if (multicastLock != null) {
    			multicastLock.release();
    		}
    		
    		started = false;
    	}			
    }
}