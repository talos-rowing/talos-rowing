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

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.app.AlertDialog;

public class RoboStrokePermissionsHelper {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private final Activity owner;
    private final Runnable onPermissionGranted;
    private int tryCount;

    RoboStrokePermissionsHelper(Activity owner, Runnable onPermissionGranted) {
        this.owner = owner;
        this.onPermissionGranted = onPermissionGranted;
    }

    void acquireLocationPermission() {
        if (owner.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted.run();
        } else {
            if (owner.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    owner.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                showDialog(
                        R.string.about_location_permission_title,
                        R.string.about_location_permission_message,
                        android.R.drawable.ic_menu_mylocation,
                        this::requestLocationPermissions
                );
            } else {
                requestLocationPermissions();
            }
        }
    }

    private void showDialog(int title, int message, int iconId, Runnable onDismissListener) {
        new AlertDialog.Builder(owner)
                .setMessage(message)
                .setTitle(title)
                .setIcon(iconId)
                .setNeutralButton(R.string.close, (dialog, which) -> {})
                .setOnDismissListener(d -> onDismissListener.run())
                .show();
    }

    private void requestLocationPermissions() {
        tryCount++;
        owner.requestPermissions(
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    void onRequestPermissionsResult(int requestCode) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (owner.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && tryCount > 2) {
                showDialog(
                        R.string.missing_location_permission_title,
                        R.string.missing_location_permission_message,
                        android.R.drawable.ic_dialog_alert,
                        this::acquireLocationPermission
                );
            } else {
                acquireLocationPermission();
            }
        }
    }
}
