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

package com.stfalcon.hromadskyipatrol.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by alex on 19.10.15.
 */
public class LocationActivity extends AppCompatActivity implements MyLocationListener {

    private LocationApi locationApi;
    private Location userLastLocation;

    private synchronized void initLocationApi() {
        locationApi = new PlayServicesLocationApi();
        locationApi.init(this);
        locationApi.setLocationListener(this);
        locationApi.createLocationRequest(LocationApi.HIGH_ACCURACY_LOCATION);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!locationApi.isWorked()) {
                    locationApi = new LocationManagerApi();
                    locationApi.init(LocationActivity.this);
                    locationApi.setLocationListener(LocationActivity.this);
                    locationApi.createLocationRequest(LocationApi.HIGH_ACCURACY_LOCATION);
                }

                userLastLocation = locationApi.getPreviousLocation();
                locationApi.startLocationUpdates();
            }
        }, 1500);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLocationApi();
    }

    @Override
    public void onStop() {
        super.onStop();
        locationApi.stopLocationUpdates();
    }

    public Location getLastUserLocation() {
        return userLastLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        userLastLocation = location;
    }

    public void checkGPSEnabled() {
        if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                LocationDialog.showSettingsAlert(this);
        }
    }

    public void stopLocationUpdates(){
        locationApi.stopLocationUpdates();
    }
}
