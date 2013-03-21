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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MulticastDataSender extends UDPDataSender {

	private static final Logger logger = LoggerFactory.getLogger(MulticastDataSender.class);
	
	private final MulticastDataHelper mdh = new MulticastDataHelper();
		
	MulticastDataSender(String address, int port) throws DataRemoteError {
		super(address, port);				
	}
	


	
	@Override
	protected DatagramSocket createSocket(String address, int port) throws IOException {

		return mdh.createSocket(address, port);
		
	}
	
	@Override
	protected void initConnection(String address, int port, byte[] buf) throws IOException {
		
		mdh.initConnection();
				
		logger.info("trying to broadcast empty package");

		mdh.sendData("");
		
	}


	protected void processNextItem(DatagramSocket socket, byte[] buf) throws IOException {
		
		String data = getNextItem();

		if (data != null) {
			mdh.sendData(data);
		} else {
			Thread.yield();
		}	
	}
}
