/*
 * Copyright (c) 2024 Tal Shalif
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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * helper class to display error notifications
 * @author tshalif
 *
 */
public class NotificationHelper {
	private final Context owner;
	private final int icon;
	private final NotificationManager mNotificationManager;
	
	public NotificationHelper(Context owner, int icon) {
		this.owner = owner;
		this.icon = icon;
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) this.owner.getSystemService(ns);
	}
	
	public void toast(String msg) {
		Toast.makeText( owner, msg, Toast.LENGTH_LONG).show();	          		
	}
	
	public void notifyError(int errorId, String msg, String contentTitle, String tickerText) {
		long when = System.currentTimeMillis();
		Intent notificationIntent = new Intent(this.owner, this.owner.getClass());
		PendingIntent contentIntent = PendingIntent.getActivity(
				this.owner,
				0,
				notificationIntent,
				PendingIntent.FLAG_MUTABLE
		);
		Context context = this.owner.getApplicationContext();
		Notification notification = new Notification.Builder(context)
				.setContentTitle(contentTitle)
				.setSmallIcon(icon)
				.setTicker(tickerText)
				.setWhen(when)
				.setContentText(msg)
				.setContentIntent(contentIntent)
				.build();


		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		mNotificationManager.notify(errorId, notification);
	}

	public void cancel(int id) {
		mNotificationManager.cancel(id);
	}
}
