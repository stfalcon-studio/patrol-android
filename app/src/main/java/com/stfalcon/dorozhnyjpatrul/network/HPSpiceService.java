package com.stfalcon.dorozhnyjpatrul.network;

import android.app.Application;

import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.retrofit.RetrofitGsonSpiceService;

/**
 * Created by alexandr on 18/08/15.
 */
public class HPSpiceService extends RetrofitGsonSpiceService {

    @Override
    public CacheManager createCacheManager(Application application) {
        return new CacheManager();
    }

    //TEST
    private final static String BASE_URL = "http://192.168.0.29/app_dev.php";

    @Override
    public void onCreate() {
        super.onCreate();
        addRetrofitInterface(HPatrulAPI.class);
    }

    @Override
    protected String getServerUrl() {
        return BASE_URL;
    }
}
