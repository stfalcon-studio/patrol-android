package com.stfalcon.hromadskyipatrul.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.stfalcon.hromadskyipatrul.network.UploadService;
import com.stfalcon.hromadskyipatrul.utils.NetworkUtils;
import com.stfalcon.hromadskyipatrul.utils.ProjectPreferecesManager;

public class NetworkStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int connectivityStatus = NetworkUtils.getConnectivityStatus(context);
        boolean isCanUpload = true;

        if (ProjectPreferecesManager.getUploadWifiOnlyMode(context))
            if (connectivityStatus != NetworkUtils.CONNECTION_WIFI)
                isCanUpload = false;

        if (isCanUpload) context.startService(new Intent(context, UploadService.class));
    }
}
