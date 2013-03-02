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

import java.io.IOException;

import org.nargila.robostroke.data.remote.DataTransport;

import android.content.Context;
import android.content.Intent;

public class TalosTransmitterServiceClient  implements DataTransport {

	private final static String SERVICE_ID = "org.nargila.robostroke.android.remote.TalosTransmitterService";	
		
	private final Context owner;
	private final Intent service;
	
	public TalosTransmitterServiceClient(Context owner) throws ServiceNotExist {
		this.owner = owner;
		
		TalosServiceHelper helper = new TalosServiceHelper(owner, SERVICE_ID);
		
		this.service = helper.service;
	}

	@Override
	public void start() throws IOException {
		owner.startService(service);
	}

	@Override
	public void stop() {
		owner.stopService(service);
	}


	@Override
	public void write(String data) {
		
		Intent intent = new Intent(SERVICE_ID);
		intent.putExtra("data", data);
		owner.sendBroadcast(intent);
	}
}
