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
package org.nargila.robostroke.android.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import org.nargila.robostroke.RoboStrokeEventBus;
import org.nargila.robostroke.data.DataRecord.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

public class HXMDataReceiver extends BroadcastReceiver implements RoboStrokeConstants {

    private static final Logger logger = LoggerFactory.getLogger(HXMDataReceiver.class);

    private final RoboStrokeEventBus bus;

    public HXMDataReceiver(RoboStrokeEventBus bus) {
        this.bus = bus;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle data = intent.getExtras();

        final String action = intent.getAction();

        int bpm = -1;

        try {
            if (action.equals(BLE_SAMPLE_HRV_DATA_SERVICE)) {
                final String dataUuid = data.getString(BLE_SAMPLE_HRV_DATA_SERVICE_EXTRA_CHARACTERISTIC_UUI);

                if (dataUuid.equals(BLE_SAMPLE_HRV_HEART_RATE_DATA_UUID)) { // Heart rate data type
                    final String txt = data.getString(BLE_SAMPLE_HRV_DATA_SERVICE_EXTRA_TEXT);

                    StringTokenizer tok = new StringTokenizer(txt, "\n");

                    logger.info(txt);

                    while (tok.hasMoreElements()) {
                        String[] nv = tok.nextToken().split("=");
                        if ("heart rate".equals(nv[0])) {
                            bpm = (int) Double.parseDouble(nv[1]);
                            break;
                        }
                    }
                }
            }

            if (bpm != -1) {
                bus.fireEvent(Type.HEART_BPM, TimeUnit.MILLISECONDS.toNanos(SystemClock.uptimeMillis()), bpm);
            }

        } catch (Exception e) {
            logger.error("error in heart rate receiver", e);
        }
    }
}
