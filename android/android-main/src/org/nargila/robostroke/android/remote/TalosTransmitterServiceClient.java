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

import org.nargila.robostroke.data.SessionRecorderConstants;
import org.nargila.robostroke.data.remote.DataTransport;

import android.content.Context;
import android.content.Intent;

public class TalosTransmitterServiceClient  implements DataTransport {

	private final static String SERVICE_ID = "org.nargila.robostroke.android.remote.TalosTransmitterService";	
		
	private final Context owner;
	private Intent service;
	private boolean started;
	private int port = SessionRecorderConstants.BROADCAST_PORT; 
			
	public TalosTransmitterServiceClient(Context owner) {
		this.owner = owner;		
	}

	@Override
	public synchronized void start() throws Exception {

		TalosRemoteServiceHelper helper = new TalosRemoteServiceHelper(owner, SERVICE_ID);
		
		service = helper.service;

		service.putExtra("port", port);
		
		owner.startService(service);
		
		started = true;
	}

	@Override
	public synchronized void stop() {
		owner.stopService(service);
		
		started = false;
	}


	@Override
	public void write(String data) {
		
		if (started) {
			Intent intent = new Intent(SERVICE_ID);
			intent.putExtra("data", data);
			owner.sendBroadcast(intent);
		}
	}

	@Override
	public synchronized void setPort(int port) {
		
		this.port = port;
		
		if (started) {
			stop();
			try {
				start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
