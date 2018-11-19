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
                .apply();
    }

    public static boolean getUploadWifiOnlyMode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREFERENCES_TAG_UPLOAD_ONLY_WIFI, false);
    }

    public static void setAutoUploadMode(Context context, boolean mode) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREFERENCES_TAG_UPLOAD_AUTOMATICALLY, mode)
                .apply();
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
        UserItem item = getObject(
                context,
                PREFERENCES_TAG_USER,
                new TypeToken<UserItem>() {
                }.getType()
        );
        if (item != null) {
            return item;
        } else {
            UserItem userItem = new UserItem();
            userItem.setEmail(UserItem.NO_AUTHORIZED_EMAIL);
            userItem.setId(UserItem.NO_AUTHORIZED_ID);
            return userItem;
        }
    }

    /*      PRIVATE METHODS     */

    private static <T> void putObject(Context context, String key, T object) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(
                        key,
                        new Gson().toJson(object))
                .apply();
    }

    private static <T> T getObject(Context context, String key, Type type) {
        String json = PreferenceManager.getDefaultSharedPreferences(context).getString(key, null);
        return new Gson().fromJson(json, type);
    }

    public static void setRecorderMode(Context context, boolean mode) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREFERENCES_TAG_START_RECORDER, mode)
                .apply();
    }

    public static boolean getRecorderMode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREFERENCES_TAG_START_RECORDER, false);
    }
}