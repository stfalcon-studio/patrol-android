package com.stfalcon.dorozhnyjpatrul.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.stfalcon.dorozhnyjpatrul.network.UploadService;
import com.stfalcon.dorozhnyjpatrul.utils.NetworkUtils;

public class NetworkStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        /*example of receiver work.
        remove this lines later (including NetworkUtils.getConnectivityStatusString method)*/
        String status = NetworkUtils.getConnectivityStatusString(context);
        Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
        context.startService(new Intent(context, UploadService.class));

        //TODO upload new photos with condition of wifi usage (using NetworkUtils.getConnectivityStatus())
    }
}
