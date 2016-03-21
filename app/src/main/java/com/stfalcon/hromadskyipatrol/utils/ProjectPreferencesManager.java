package com.stfalcon.hromadskyipatrol.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stfalcon.hromadskyipatrol.models.UserItem;

import java.lang.reflect.Type;

/**
 * Created by TROY!379 on 21.08.15.
 */
public final class ProjectPreferencesManager {

    private ProjectPreferencesManager() {
        throw new AssertionError();
    }

    private static final String PREFERENCES_TAG_UPLOAD_ONLY_WIFI = "use_only_wifi_state";
    private static final String PREFERENCES_TAG_UPLOAD_AUTOMATICALLY = "automatic_upload";
    private static final String PREFERENCES_TAG_START_RECORDER = "start_with_recorder";

    private static final String PREFERENCES_TAG_USER = "user";

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

    public static void setAutoUploadMode(Context context, boolean mode) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREFERENCES_TAG_UPLOAD_AUTOMATICALLY, mode)
                .commit();
    }

    public static boolean getAutoUploadMode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREFERENCES_TAG_UPLOAD_AUTOMATICALLY, false);
    }

    /*      USER    */

    public static void setUser(Context context, UserItem user) {
        putObject(context, PREFERENCES_TAG_USER, user);
    }

    public static UserItem getUser(Context context) {
        return getObject(
                context,
                PREFERENCES_TAG_USER,
                new TypeToken<UserItem>() {
                }.getType()
        );
    }

    /*      PRIVATE METHODS     */

    private static <T> void putObject(Context context, String key, T object) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(
                        key,
                        new Gson().toJson(object))
                .commit();
    }

    private static <T> T getObject(Context context, String key, Type type) {
        String json = PreferenceManager.getDefaultSharedPreferences(context).getString(key, null);
        return new Gson().fromJson(json, type);
    }

    public static void setRecorderMode(Context context, boolean mode) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREFERENCES_TAG_START_RECORDER, mode)
                .commit();
    }

    public static boolean getRecorderMode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREFERENCES_TAG_START_RECORDER, false);
    }
}