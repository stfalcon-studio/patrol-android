/*
 * Copyright (c) 2015 - 2016. Stepan Tanasiychuk
 *
 *     This file is part of Gromadskyi Patrul is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Found ation, version 3 of the License, or any later version.
 *
 *     If you would like to use any part of this project for commercial purposes, please contact us
 *     for negotiating licensing terms and getting permission for commercial use.
 *     Our email address: info@stfalcon.com
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stfalcon.hromadskyipatrol.location;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.utils.Constants;

/**
 * Created by alexandr on 19/05/15.
 */
public final class LocationDialog {
    private LocationDialog() { throw new AssertionError(); }

    /**
     * Function to show settings popup_sos dialog On pressing SettingsActivity button will
     * launch SettingsActivity Options
     */
    public static void showSettingsAlert(final Activity activity) {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
            alertDialog.setTitle(R.string.gps_dialog_title);
            alertDialog.setMessage(R.string.gps_dialog_text);

            // On pressing SettingsActivity button
            alertDialog.setPositiveButton(
                    R.string.gps_turn_on,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            activity.startActivityForResult(intent, Constants.REQUEST_GPS_SETTINGS);
                        }
                    }
            );

            // on pressing cancel button
            alertDialog.setNegativeButton(
                    activity.getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }
            );

            // Showing Alert Message
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
