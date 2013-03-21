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
import org.nargila.robostroke.data.RecordDataInput;
import org.nargila.robostroke.data.SessionRecorderConstants;

public class RemoteDataInput extends RecordDataInput {
		
	private final DataReceiver receiver;

	public RemoteDataInput(RoboStroke roboStroke) throws IOException {
		this(roboStroke, new AutoDataReceiver(RemoteDataHelper.getAddr(roboStroke), RemoteDataHelper.getPort(roboStroke), null));
	}
	
	public RemoteDataInput(RoboStroke roboStroke, DataReceiver receiver) throws IOException {
		
		super(roboStroke);
		
		this.receiver = receiver;
		
		receiver.setListener(new DataReceiver.Listener() {
			
			@Override
			public void onDataReceived(String s) {
				playRecord(s, SessionRecorderConstants.END_OF_RECORD);
			}
		});
	}

	@Override
	public void stop() {
		
		receiver.stop();
		
		super.stop();
	}

	@Override
	public void start() {
		
		super.start();
		
				
		try {
			receiver.start();
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
