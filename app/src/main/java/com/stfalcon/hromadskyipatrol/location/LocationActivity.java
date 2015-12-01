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
        locationApi.createLocationRequest(LocationApi.LOW_ACCURACY_LOCATION);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!locationApi.isWorked()) {
                    locationApi = new LocationManagerApi();
                    locationApi.init(LocationActivity.this);
                    locationApi.setLocationListener(LocationActivity.this);
                    locationApi.createLocationRequest(LocationApi.LOW_ACCURACY_LOCATION);
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

    }

    public void checkGPSEnabled() {
        if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                LocationDialog.showSettingsAlert(this);
        }
    }
}
