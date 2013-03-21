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
import java.net.DatagramSocket;


public class UDPDataReceiver extends UDPData implements DataReceiver {
	
	private Listener dataListener;
	
	private final UnicastDataHelper udh = new UnicastDataHelper();
	
	UDPDataReceiver(String address, int port, Listener dataListener) throws DataRemoteError {
		super(address, port);
		
		this.dataListener = dataListener;
	}
	
	
	@Override
	public void setListener(Listener dataListener) {
		this.dataListener = dataListener;
	}


	private void onItemReceived(String received) {
		if (dataListener != null && received != null && !received.equals("")) {
			dataListener.onDataReceived(received);
		}
	}
	
	@Override
	protected DatagramSocket createSocket(String address, int port) throws IOException {
		return udh.createSocket(null, port);
	}
	
	@Override
	protected void initConnection(String address, int port, byte[] buf) throws IOException {		
	}

	@Override
	protected void processNextItem(DatagramSocket socket, byte[] buf) throws IOException {

		String received = getData(UnicastDataHelper.receivePacket(socket, buf));
		
		onItemReceived(received);
	}
}
