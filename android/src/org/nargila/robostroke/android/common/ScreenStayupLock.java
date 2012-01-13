/*
 * Copyright (c) 2011 Tal Shalif
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

package org.nargila.robostroke.android.common;

import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;

/**
 * Manages device wake lock 
 * @author tshalif
 *
 */
public class ScreenStayupLock {
	private PowerManager.WakeLock wl;
	private boolean inited;
	private Activity owner;
	private String tag;
	
	public ScreenStayupLock(Activity owner, String tag) {
		this.owner = owner;
		this.tag = tag;
	}
	
	public void start() {
		if (!inited) {
			PowerManager pm = (PowerManager) owner.getSystemService(Context.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, tag);
			inited = true;
		}
		wl.acquire();		
	}
	
	public void stop() {
		wl.release();		
	}
}
