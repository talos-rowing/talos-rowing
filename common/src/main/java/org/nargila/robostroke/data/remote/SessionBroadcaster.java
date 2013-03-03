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

package org.nargila.robostroke.data.remote;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.SensorBinder;
import org.nargila.robostroke.data.DataRecord;

public class SessionBroadcaster extends SensorBinder {
					
	private final DataTransport dataTransport;
	
	private boolean broadcast;
	
	public SessionBroadcaster(RoboStroke roboStroke, DataTransport dataTransport) {
		
		super(roboStroke);
		
		this.dataTransport = dataTransport;
	}
	
	public void setPort(int port) {
		dataTransport.setPort(port);
	}

	public void enable(boolean broadcast) {
				
		if (this.broadcast != broadcast) {
			if (broadcast) {
				connect();
			} else {
				disconnect();
			}
			this.broadcast = broadcast;
		}
	}
	
	@Override
	protected synchronized void connect() {
		
		super.connect();					
		
		try {
			dataTransport.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected synchronized void disconnect() {
		
		super.disconnect();
		
		dataTransport.stop();
	}
	
	@Override
	protected void onSensorData(DataRecord record) {		
		write(record);		
	}


	@Override
	public void onBusEvent(DataRecord record) {
		
		if (record.type.isExportableEvent) {
			write(record);
		}
	}

	public void write(DataRecord record) {
				
		if (dataTransport != null) {
			dataTransport.write(record.toString());
		}
	}
}
