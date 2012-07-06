package org.nargila.robostroke.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.nargila.robostroke.RoboStrokeEventBus;

public class RemoteDataInput extends RecordDataInput {

	private Socket socket;
	
	private final String host;
	private final int port;
		
	public RemoteDataInput(RoboStrokeEventBus bus, String host) throws IOException {
		this(bus, host, SessionRecorderConstants.BROADCAST_PORT);
	}
	
	public RemoteDataInput(RoboStrokeEventBus bus, String host, int port) throws IOException {
		
		super(bus);
		
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
	}

	@Override
	public void start() {
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
