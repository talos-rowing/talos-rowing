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

import org.nargila.robostroke.data.remote.DataRemote;
import org.nargila.robostroke.data.remote.MulticastDataSender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class TalosBroadcastService extends TalosService {
 
	private final static String BROADCAST_ID = TalosBroadcastService.class.getName();
		
	private final BroadcastReceiver receiver;

	private MulticastDataSender impl;

	public TalosBroadcastService() {
		receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle data = intent.getExtras();
				String l = data.getString("data");
				impl.write(l);
			}
		};
	}
	
	@Override
	protected DataRemote makeImpl(String host, int port) {
		impl = new MulticastDataSender(host, port);
		return impl;
	}
	
	
	@Override
	protected void afterStart() {
		registerReceiver(receiver, new IntentFilter(BROADCAST_ID));		
	}

	
	@Override
	protected void beforeStop() {
    	unregisterReceiver(receiver);
    }
}