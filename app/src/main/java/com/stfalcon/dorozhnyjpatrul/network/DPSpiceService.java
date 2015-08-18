package com.stfalcon.dorozhnyjpatrul.network;

import android.app.Application;
import android.content.Context;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.CacheManager;

/**
 * Created by alexandr on 18/08/15.
 */
public class DPSpiceService  extends SpiceService {

    @Override
    public CacheManager createCacheManager(Application application) {
        return new CacheManager();
    }

    @Override
    protected NetworkStateChecker getNetworkStateChecker() {
        return new NetworkStateChecker() {

            @Override
            public boolean isNetworkAvailable(Context context) {
                return true;
            }

            @Override
            public void checkPermissions(Context context) {
            }
        };
    }

    @Override
    public int getThreadCount() {
        return 3;
    }
}
