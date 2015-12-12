package com.stfalcon.hromadskyipatrol.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.stfalcon.hromadskyipatrol.models.VideoItem;

/**
 * Created by TROY!379 on 21.08.15.
 */
public final class NetworkUtils {
    private NetworkUtils() { throw new AssertionError(); }

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
