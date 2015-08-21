package com.stfalcon.dorozhnyjpatrul.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.stfalcon.dorozhnyjpatrul.network.UploadService;
import com.stfalcon.dorozhnyjpatrul.utils.NetworkUtils;
import com.stfalcon.dorozhnyjpatrul.utils.ProjectPreferecesManager;

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
