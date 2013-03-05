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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.nargila.robostroke.data.SessionRecorderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class TalosReceiverService extends Service {
 
	private final static String BROADCAST_ID = TalosReceiverService.class.getName();

	private static final Logger logger = LoggerFactory.getLogger(TalosReceiverService.class);
	
	private static int SERVICE_NOTIFICATION_ID = 1;
	
	private NotificationManager notificationManager;

	private int port;

	private String host;

	private RemoteDataInput connection;


	public TalosReceiverService() {
		boolean stop = false;
		
		while (stop) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
	
	
	private void startService() {

		logger.info("creating connection with host={}, port={}", host, port);

		connection = new RemoteDataInput(host, port);
		
		connection.start();

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
    	if (null != connection) {
    		logger.info("stopping service");
    		connection.stop();
    	}			
    }
    
    
    private class RemoteDataInput {

    	private Socket socket;
    	
    	private final String host;
    	private final int port;
    			
    	RemoteDataInput(String host, int port) {
    				
    		this.host = host;
    		this.port = port;		
    	}

    	private synchronized void stop() {
    		if (socket != null) {
    			try {
    				socket.close();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			
    			socket = null;
    		}
    	}

    	private synchronized void start() {
    		new Thread("RemoteDataInput") {
    			public void run() {	
    				
    				Socket s = null;
    				
    				try {

    					s = socket = new Socket();		

    					logger.info("connecting >>");

    					s.connect(new InetSocketAddress(host, port), 1000);

    					logger.info("connecting << (OK)");

    					BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
    					String l;

    					while ((l = reader.readLine()) != null) {

    						Intent intent = new Intent(BROADCAST_ID);
    						intent.putExtra("data", l);
    						sendBroadcast(intent);
    						Thread.yield();
    					}
    				} catch (IOException e) {
    					e.printStackTrace();
    				} finally {
    					try {
    						if (s != null) s.close();
    					} catch (IOException e) {
    					}
    				}
    			}
    		}.start();
    	}
    }
}