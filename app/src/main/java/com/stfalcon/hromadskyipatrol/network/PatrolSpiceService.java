package com.stfalcon.hromadskyipatrol.network;

import com.octo.android.robospice.retrofit.RetrofitGsonSpiceService;
import com.stfalcon.hromadskyipatrol.BuildConfig;

/**
 * Created by alexandr on 18/08/15.
 */
public class PatrolSpiceService extends RetrofitGsonSpiceService {

    @Override
    public void onCreate() {
        super.onCreate();
        addRetrofitInterface(PatrulatrulAPI.class);
    }

    @Override
    protected String getServerUrl() {
        return BuildConfig.BASE_URL;
    }
}
