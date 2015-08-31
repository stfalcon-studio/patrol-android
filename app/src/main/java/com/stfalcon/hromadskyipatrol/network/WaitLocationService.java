package com.stfalcon.hromadskyipatrol.network;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.stfalcon.hromadskyipatrol.models.PhotoAnswer;
import com.stfalcon.hromadskyipatrol.models.PhotoItem;
import com.stfalcon.hromadskyipatrol.utils.NetworkUtils;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by alexandr on 31/08/15.
 */
public class WaitLocationService extends IntentService {

    private static final String TAG = WaitLocationService.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private long MINUTE = 60000;
    private long ACTUAL_INTERVAL = MINUTE * 2; // 2 min
    private static final long MIN_TIME_BW_UPDATES = 2000; // 2 sec
    private float MIN_ACCURACY = 200; // 200m
    private LocationThread thread;
    private Location mLocation;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public WaitLocationService() {
        super(WaitLocationService.class.getName());
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (thread == null) {
            Log.d(TAG, "launching location thread");
            thread = new LocationThread();
            thread.start();
            try {
                thread.join(ACTUAL_INTERVAL);
            } catch (InterruptedException e) {
                Log.d(TAG, "timeout");
                thread.setItemsState(null);
                return;
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (thread != null) {
            thread.stopLocationUpdates();
        }
    }

    private class LocationThread extends Thread implements LocationListener,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        public LocationThread() {
            super("WaitLocationService-LocationThread");
            buildGoogleApiClient();
        }

        private void startLocationUpdates() {
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, createLocationRequest(), this);
            } catch (IllegalStateException e) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startLocationUpdates();
                    }
                }, 10000);  // Try after 10sec
            }
        }

        private void stopLocationUpdates() {
            try {
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        mGoogleApiClient, this);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        private LocationRequest createLocationRequest() {
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(MIN_TIME_BW_UPDATES);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            return mLocationRequest;
        }

        protected synchronized void buildGoogleApiClient() {
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(WaitLocationService.this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
                mGoogleApiClient.connect();
            }
        }


        @Override
        public void onConnected(Bundle bundle) {
            startLocationUpdates();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onLocationChanged(Location location) {
            if (location.getTime() > System.currentTimeMillis() - ACTUAL_INTERVAL &&
                    location.getAccuracy() < MIN_ACCURACY ) {
                mLocation = location;
                setItemsState(location);

                int connectivityStatus = NetworkUtils.getConnectivityStatus(WaitLocationService.this);
                boolean isCanUpload = true;

                if (ProjectPreferencesManager.getUploadWifiOnlyMode(WaitLocationService.this))
                    if (connectivityStatus != NetworkUtils.CONNECTION_WIFI)
                        isCanUpload = false;

                if (isCanUpload && connectivityStatus != NetworkUtils.NOT_CONNECTED) {
                    startService(new Intent(WaitLocationService.this, UploadService.class));
                }
                thread.interrupted();
            }
        }

        private void setItemsState(Location location) {
            //init objects
            Realm realmDB = Realm.getInstance(WaitLocationService.this);
            ArrayList<PhotoAnswer> answersList = new ArrayList<PhotoAnswer>();

            //get all photos for upload
            RealmResults<PhotoItem> photoList = realmDB.where((PhotoItem.class))
                    .equalTo("state", PhotoItem.STATE_SAVING)
                    .findAll();


            for (PhotoItem item : photoList) {

                if (location == null) {
                    answersList.add(new PhotoAnswer(item.getId(), PhotoItem.STATE_NO_GPS));
                } else {
                    long photoTime = Long.valueOf(item.getId());
                    if (photoTime + ACTUAL_INTERVAL > location.getTime()) {
                        PhotoAnswer answer = new PhotoAnswer(item.getId(), PhotoItem.STATE_IN_PROCESS);
                        answer.setLatitude(location.getLatitude());
                        answer.setLongitude(location.getLongitude());
                        answersList.add(answer);
                    } else {
                        answersList.add(new PhotoAnswer(item.getId(), PhotoItem.STATE_NO_GPS));
                    }
                }
                PhotoAnswer answer = answersList.get(answersList.size() - 1);
                UploadService.updateActivityUI(WaitLocationService.this, answer.getId(), answer.getState());
            }

            //update DB
            if (!answersList.isEmpty()) {
                for (PhotoAnswer answer : answersList) {
                    realmDB.beginTransaction();
                    PhotoItem photoInBase = realmDB.where(PhotoItem.class).contains("id", answer.getId()).findFirst();
                    photoInBase.setState(answer.getState());
                    photoInBase.setLatitude(answer.getLatitude());
                    photoInBase.setLongitude(answer.getLongitude());
                    realmDB.copyToRealmOrUpdate(photoInBase);
                    realmDB.commitTransaction();
                }
            }
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }

    }


}
