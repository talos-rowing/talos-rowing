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

import java.io.IOException;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.SensorBinder;
import org.nargila.robostroke.data.DataRecord;

public class SessionBroadcaster extends SensorBinder {
					
	private DataTransport dataTransmitter;
	
	private boolean broadcast;
	
	public SessionBroadcaster(RoboStroke roboStroke) {
		super(roboStroke);
	}

	public void setDataTransmitter(DataTransport dataTransmitter) {
		this.dataTransmitter = dataTransmitter;
	}
	
	public void setBoradcast(boolean broadcast) {
				
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
			dataTransmitter.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected synchronized void disconnect() {
		
		super.disconnect();
		
		dataTransmitter.stop();
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
				
		if (dataTransmitter != null) {
			dataTransmitter.write(record.toString());
		}
	}
}
