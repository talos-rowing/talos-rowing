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
import org.nargila.robostroke.data.remote.DataTransport;
import org.nargila.robostroke.data.remote.SocketDataTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;

public class TalosTransmitterService extends Service {
 
	private final static String BROADCAST_ID = TalosTransmitterService.class.getName();

	private static final Logger logger = LoggerFactory.getLogger(TalosTransmitterService.class);
	
	private static int SERVICE_NOTIFICATION_ID = 1;
	
	private NotificationManager notificationManager;

	private int port;

	private DataTransport impl;


	private BroadcastReceiver receiver;

	public TalosTransmitterService() {

	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		
	
		super.onStart(intent, startId);
		
		port = intent.getIntExtra("port", SessionRecorderConstants.BROADCAST_PORT);
		
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
	
	
	private void startService() {

		logger.debug("starting RemoteTalosService data service");

		impl = new SocketDataTransport(port);
		
		try {
			
			impl.start();
			
	   		receiver = new BroadcastReceiver() {
				

	   			@Override
	   			public void onReceive(Context context, Intent intent) {
	   				Bundle data = intent.getExtras();
	   				String l = data.getString("data");
					impl.write(l);
	   			}
	   		};
	   		

			registerReceiver(receiver, new IntentFilter(BROADCAST_ID));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

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
	
    private void stopService() {
    	if (null != impl) {
    		unregisterReceiver(receiver);
    		impl.stop();
    	}			
    }
}