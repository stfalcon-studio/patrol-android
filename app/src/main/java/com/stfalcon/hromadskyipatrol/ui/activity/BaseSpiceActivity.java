package com.stfalcon.hromadskyipatrol.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.octo.android.robospice.SpiceManager;
import com.stfalcon.hromadskyipatrol.PatrulApp;
import com.stfalcon.hromadskyipatrol.network.PatrolSpiceService;

/**
 * Created by alexandr on 20/08/15.
 */
public class BaseSpiceActivity extends AppCompatActivity {
    private SpiceManager spiceManager = new SpiceManager(PatrolSpiceService.class);
    private Tracker mTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PatrulApp application = (PatrulApp) getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this instanceof LoginActivity) {
            mTracker.setScreenName(LoginActivity.class.getSimpleName());
        } else if (this instanceof MainScreen) {
            mTracker.setScreenName(MainScreen.class.getSimpleName());
        }
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onStart() {
        spiceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    public Tracker getTracker() {
        return mTracker;
    }

    protected SpiceManager getSpiceManager() {
        return spiceManager;
    }
}
