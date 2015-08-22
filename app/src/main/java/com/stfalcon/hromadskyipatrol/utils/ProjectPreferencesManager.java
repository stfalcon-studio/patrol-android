package com.stfalcon.hromadskyipatrol.utils;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by TROY!379 on 21.08.15.
 */
public final class ProjectPreferencesManager {
    private ProjectPreferencesManager() {
        throw new AssertionError();
    }

    private static final String PREFERENCES_TAG_UPLOAD_ONLY_WIFI = "use_only_wifi_state";

    public static void setUploadWifiOnlyMode(Context context, boolean mode) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREFERENCES_TAG_UPLOAD_ONLY_WIFI, mode)
                .commit();
    }

    public static boolean getUploadWifiOnlyMode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREFERENCES_TAG_UPLOAD_ONLY_WIFI, false);
    }
}