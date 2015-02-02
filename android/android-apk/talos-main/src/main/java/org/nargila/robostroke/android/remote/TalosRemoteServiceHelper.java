package org.nargila.robostroke.android.remote;

import java.util.List;

import org.nargila.robostroke.android.app.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;

class TalosRemoteServiceHelper {
	
	final Intent service;
	final Context owner;
	
	protected TalosRemoteServiceHelper(Context owner, String serviceId) throws ServiceNotExist {
				
		this.owner = owner;
		this.service = new Intent(serviceId);
		
		List<ResolveInfo> res = owner.getPackageManager().queryIntentServices(service, 0);

		if (res.isEmpty()) {
			installTalosRemote();
			throw new ServiceNotExist(serviceId);
		}
	}
	
	private void installTalosRemote() {
		new AlertDialog.Builder(owner)
		.setMessage(owner.getString(R.string.talos_remote_missing_text))
		.setTitle(R.string.talos_remote_missing)
		.setIcon(R.drawable.icon)
		.setCancelable(true)
		.setPositiveButton(R.string.install, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				String appName = "org.nargila.robostroke.android.remote";

				try {
					owner.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
				} catch (android.content.ActivityNotFoundException anfe) {
					owner.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
				}
			}
		}).show();
	}

}
