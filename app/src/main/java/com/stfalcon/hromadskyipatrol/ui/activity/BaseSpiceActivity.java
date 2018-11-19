/*
 * Copyright (c) 2015 - 2016. Stepan Tanasiychuk
 *
 *     This file is part of Gromadskyi Patrul is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Found ation, version 3 of the License, or any later version.
 *
 *     If you would like to use any part of this project for commercial purposes, please contact us
 *     for negotiating licensing terms and getting permission for commercial use.
 *     Our email address: info@stfalcon.com
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stfalcon.hromadskyipatrol.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.octo.android.robospice.SpiceManager;
import com.stfalcon.hromadskyipatrol.PatrolApp;
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
        PatrolApp application = (PatrolApp) getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this instanceof LoginActivity) {
            mTracker.setScreenName(LoginActivity.class.getSimpleName());
        } else if (this instanceof MainActivity) {
            mTracker.setScreenName(MainActivity.class.getSimpleName());
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
