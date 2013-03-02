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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.data.RecordDataInput;
import org.nargila.robostroke.data.SessionRecorderConstants;

public class RemoteDataInput extends RecordDataInput {

	private Socket socket;
	
	private final String host;
	private final int port;
		
	public RemoteDataInput(RoboStroke roboStroke, String host) throws IOException {
		this(roboStroke, host, SessionRecorderConstants.BROADCAST_PORT);
	}
	
	public RemoteDataInput(RoboStroke roboStroke, String host, int port) throws IOException {
		
		super(roboStroke);
		
		this.host = host;
		this.port = port;		
	}

	@Override
	public void stop() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		super.stop();
	}

	@Override
	public void start() {
		
		super.start();
		
		socket = new Socket();		
		
		try {
			socket.connect(new InetSocketAddress(host, port), 1000);
			new Thread("RemoteDataInput") {
				public void run() {					
					try {
						BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String l;
						
						while ((l = reader.readLine()) != null) {
							playRecord(l, SessionRecorderConstants.END_OF_RECORD);
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							socket.close();
						} catch (IOException e) {
						}
					}
				}
			}.start();
		} catch (IOException e) {
			e.printStackTrace();
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
