package com.stfalcon.hromadskyipatrul.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.stfalcon.hromadskyipatrul.R;

/**
 * Created by alexandr on 21/08/15.
 */
public class LocationDialog {
    /**
     * Function to show settings popup_sos dialog On pressing SettingsActivity button will
     * lauch SettingsActivity Options
     */
    public static void showSettingsAlert(final Context activity) {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
            alertDialog.setTitle(R.string.gps_dialog_title);
            alertDialog.setMessage(R.string.gps_dialog_text);

            // On pressing SettingsActivity button
            alertDialog.setPositiveButton(R.string.gps_turn_on,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            activity.startActivity(intent);
                        }
                    }
            );

            // on pressing cancel button
            alertDialog.setNegativeButton(activity.getString(android.R.string.cancel),
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
