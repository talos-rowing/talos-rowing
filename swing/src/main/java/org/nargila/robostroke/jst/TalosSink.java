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



package org.nargila.robostroke.jst;

import org.nargila.robostroke.data.RecordDataInput;

import com.fluendo.jkate.Event;
import com.fluendo.jst.Buffer;
import com.fluendo.jst.Message;
import com.fluendo.jst.Pad;
import com.fluendo.jst.Sink;

public class TalosSink extends Sink
{

	private final RecordDataInput recordPlayer;
	
	public TalosSink(RecordDataInput recordPlayer) {
		this.recordPlayer = recordPlayer;
		setName("talossink");
	}

	@Override	
	protected int render (Buffer buf) {	  

		if (!(buf.object instanceof com.fluendo.jkate.Event)) {
			postMessage(Message.newError(this, "no com.fluendo.jkate.Event object found attached to buffer"));
			return Pad.UNEXPECTED;
		}

		com.fluendo.jkate.Event ke = (Event) buf.object;

		String line = new String(ke.text);

		recordPlayer.playRecord(line);

		return Pad.OK;
	}

	@Override	
	public String getFactoryName () 	{
		return "talossink";
	}
}
