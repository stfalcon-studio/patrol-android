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
