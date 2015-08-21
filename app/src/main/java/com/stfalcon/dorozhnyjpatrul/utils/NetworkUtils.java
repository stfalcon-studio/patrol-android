package com.stfalcon.dorozhnyjpatrul.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

    public static String getConnectivityStatusString(Context context) {
        int conn = NetworkUtils.getConnectivityStatus(context);
        String status = null;
        if (conn == NetworkUtils.CONNECTION_WIFI) {
            status = "Wifi enabled";
        } else if (conn == NetworkUtils.CONNECTION_MOBILE) {
            status = "Mobile data enabled";
        } else if (conn == NetworkUtils.NOT_CONNECTED) {
            status = "Not connected to Internet";
        }
        return status;
    }
}
