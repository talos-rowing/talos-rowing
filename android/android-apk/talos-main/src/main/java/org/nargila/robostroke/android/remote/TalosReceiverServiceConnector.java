/*
 * Copyright (c) 2012 Tal Shalif
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

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.common.ThreadedQueue;
import org.nargila.robostroke.data.RecordDataInput;
import org.nargila.robostroke.data.SessionRecorderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class TalosReceiverServiceConnector extends RecordDataInput {

	private static final Logger logger = LoggerFactory.getLogger(TalosReceiverServiceConnector.class);
			
	private final Context owner;

	private final Intent service;

	private final ThreadedQueue<String> recordQueue;

	private final BroadcastReceiver receiver;

	private boolean started;	
		
	public TalosReceiverServiceConnector(Context owner, RoboStroke roboStroke, String host) throws ServiceNotExist {
		this(owner, roboStroke, host, SessionRecorderConstants.BROADCAST_PORT);
	}
	
	public TalosReceiverServiceConnector(Context owner, RoboStroke roboStroke, String host, int port) throws ServiceNotExist {
		
		super(roboStroke);
		
		TalosRemoteServiceHelper helper = new TalosRemoteServiceHelper(owner, TalosRemoteServiceHelper.RECEIVER_SERVICE_ID);
		
		this.owner = owner;
		
   		service = helper.service;
   		
   		service.putExtra("host", host);
   		service.putExtra("port", port);   
   		
   		recordQueue = new ThreadedQueue<String>(getClass().getSimpleName(), 100) {
			
			@Override
			protected void handleItem(String o) {
				playRecord(o);
			}
		};
		
		receiver = new BroadcastReceiver() {


			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle data = intent.getExtras();
				String l = data.getString("data");
				recordQueue.put(l);
			}
		};
		
	}

	@Override
	public synchronized void stop() {
		
		if (started) {
			recordQueue.setEnabled(false);
			owner.unregisterReceiver(receiver);
			owner.stopService(service);
			super.stop();
			started = false;
		}
	}

	@Override
	public synchronized void start() {
		
		if (!started) {
			super.start();
			recordQueue.setEnabled(true);
			owner.registerReceiver(receiver, new IntentFilter(TalosRemoteServiceHelper.RECEIVER_SERVICE_ID), Context.RECEIVER_NOT_EXPORTED);
			owner.startService(service);
			started = true;
		}

	}

	@Override
	public void skipReplayTime(float velocityX) {
	}

	@Override
	public void setPaused(boolean pause) {
	}

	@Override
	protected void onSetPosFinish(double pos) {
	}
}
