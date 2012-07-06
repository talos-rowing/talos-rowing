/* Copyright (C) <2004> Wim Taymans <wim@fluendo.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package org.nargila.robostroke.jst;

import org.nargila.robostroke.data.DataRecord;
import org.nargila.robostroke.data.RecordDataInput;

import com.fluendo.jkate.Event;
import com.fluendo.jst.Buffer;
import com.fluendo.jst.Message;
import com.fluendo.jst.Pad;
import com.fluendo.jst.Sink;
import com.fluendo.utils.Debug;

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
