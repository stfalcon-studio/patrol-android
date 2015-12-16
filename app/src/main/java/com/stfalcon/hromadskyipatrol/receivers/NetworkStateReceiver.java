package com.stfalcon.hromadskyipatrol.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.stfalcon.hromadskyipatrol.network.UploadService;
import com.stfalcon.hromadskyipatrol.utils.NetworkUtils;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;

public class NetworkStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int connectivityStatus = NetworkUtils.getConnectivityStatus(context);
        boolean isCanUpload = ProjectPreferencesManager.getAutoUploadMode(context);

        if (ProjectPreferencesManager.getUploadWifiOnlyMode(context))
            if (connectivityStatus != NetworkUtils.CONNECTION_WIFI)
                isCanUpload = false;

        if (isCanUpload  && connectivityStatus != NetworkUtils.NOT_CONNECTED)
            context.startService(new Intent(context, UploadService.class));
    }
}
