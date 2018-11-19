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
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by alex on 02.10.15.
 */
public class LocationManagerApi implements LocationApi, LocationListener {

    private Location prev_location;
    private MyLocationListener callback;
    private LocationManager locationManager;
    private String provider = LocationManager.GPS_PROVIDER;
    private int MIN_TIME_TO_UPDATE;
    private static final int ACTUAL_POINT_TIME = 5000; //sec

    @Override
    public void init(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //prev_location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    @Override
    public boolean isWorked() {
        return false;
    }

    @Override
    public void createLocationRequest(int type) {
        switch (type) {

            case LocationApi.LOW_ACCURACY_LOCATION:
                MIN_TIME_TO_UPDATE = 30 * 1000; //0,5 min
                provider = LocationManager.NETWORK_PROVIDER;
                break;

            case LocationApi.HIGH_ACCURACY_LOCATION:
                MIN_TIME_TO_UPDATE = 2000; // 2 sec
                provider = LocationManager.GPS_PROVIDER;
                break;
        }
    }


    @Override
    public void startLocationUpdates() {
        // Register the listener with the Location Manager to receive location updates
        /*try {
            locationManager.requestLocationUpdates(provider, MIN_TIME_TO_UPDATE, 0, this);
        } catch (Exception e){
            Log.i("LocationApi", "provider doesn't exist: network");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_TO_UPDATE, 0, this);
        }*/
    }

    @Override
    public void stopLocationUpdates() {
        //locationManager.removeUpdates(this);
    }

    @Override
    public Location getPreviousLocation() {
        return prev_location;
    }

    @Override
    public void setLocationListener(MyLocationListener listener) {
        callback = listener;
    }


    @Override
    public void onLocationChanged(Location location) {
        if (callback != null) {
            callback.onLocationChanged(location);
            /*if (isBetterLocation(location, prev_location)) {
                callback.onLocationChanged(location);
                prev_location = location;
            }*/
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > ACTUAL_POINT_TIME;
        boolean isSignificantlyOlder = timeDelta < -ACTUAL_POINT_TIME;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
