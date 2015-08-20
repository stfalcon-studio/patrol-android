package com.stfalcon.dorozhnyjpatrul.ui;

import android.support.v7.app.AppCompatActivity;

import com.octo.android.robospice.SpiceManager;
import com.stfalcon.dorozhnyjpatrul.network.HPSpiceService;

/**
 * Created by alexandr on 20/08/15.
 */
public class BaseSpiceActivity extends AppCompatActivity {
    private SpiceManager spiceManager = new SpiceManager(HPSpiceService.class);

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

    protected SpiceManager getSpiceManager() {
        return spiceManager;
    }
}
