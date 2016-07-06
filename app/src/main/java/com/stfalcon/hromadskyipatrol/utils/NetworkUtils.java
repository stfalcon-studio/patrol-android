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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.stfalcon.hromadskyipatrol.models.VideoItem;

/**
 * Created by TROY!379 on 21.08.15.
 */
public final class NetworkUtils {
    private NetworkUtils() {
        throw new AssertionError();
    }

    public static final int CONNECTION_WIFI = 1;
    public static final int CONNECTION_MOBILE = 2;
    public static final int NOT_CONNECTED = 0;


    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null) {
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    return CONNECTION_WIFI;
                case ConnectivityManager.TYPE_MOBILE:
                    return CONNECTION_MOBILE;
            }
        }
        return NOT_CONNECTED;
    }

    public static boolean isConnectionAvailable(Context context) {
        int connectivityStatus = NetworkUtils.getConnectivityStatus(context);
        boolean isCanUpload = true;

        if (ProjectPreferencesManager.getUploadWifiOnlyMode(context))
            if (connectivityStatus != NetworkUtils.CONNECTION_WIFI) {
                isCanUpload = false;
            }

        return (isCanUpload && connectivityStatus != NetworkUtils.NOT_CONNECTED);
    }

    public static boolean isCanLoadItem(VideoItem.State state) {
        return state == VideoItem.State.READY_TO_SEND || state == VideoItem.State.ERROR;
    }

    public static boolean isCanDelete(VideoItem.State state) {
        return state != VideoItem.State.SAVING && state != VideoItem.State.SENDING;
    }
}
