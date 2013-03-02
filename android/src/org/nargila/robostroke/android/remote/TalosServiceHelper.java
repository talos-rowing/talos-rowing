package org.nargila.robostroke.android.remote;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

class TalosServiceHelper {
	
	final Intent service;
	
	protected TalosServiceHelper(Context owner, String serviceId) throws ServiceNotExist {
				
		service = new Intent(serviceId);
		
		List<ResolveInfo> res = owner.getPackageManager().queryIntentServices(service, 0);

		if (res.isEmpty()) {
			throw new ServiceNotExist(serviceId);
		}
	}
}
