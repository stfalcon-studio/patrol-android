package com.stfalcon.hromadskyipatrul.utils;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by TROY!379 on 21.08.15.
 */
public final class ProjectPreferecesManager {
    private ProjectPreferecesManager() {
        throw new AssertionError();
    }

    private static final String PREFERECES_TAG_UPLOAD_ONLY_WIFI = "use_only_wifi_state";

    public static void setUploadWifiOnlyMode(Context context, boolean mode) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREFERECES_TAG_UPLOAD_ONLY_WIFI, mode)
                .commit();
    }

    public static boolean getUploadWifiOnlyMode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREFERECES_TAG_UPLOAD_ONLY_WIFI, false);
    }
}