package com.stfalcon.hromadskyipatrol.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.models.PhotoItem;
import com.stfalcon.hromadskyipatrol.models.UserItem;
import com.stfalcon.hromadskyipatrol.network.UploadService;
import com.stfalcon.hromadskyipatrol.ui.LocationDialog;
import com.stfalcon.hromadskyipatrol.ui.PhotoGridAdapter;
import com.stfalcon.hromadskyipatrol.utils.CameraUtils;
import com.stfalcon.hromadskyipatrol.utils.NetworkUtils;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by alexandr on 17/08/15.
 */
public class MainScreen extends BaseSpiceActivity implements View.OnClickListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    int REQUEST_CAMERA = 0;

    private TextView noPhotosTextView;
    private LinearLayout llSettings;
    private CheckBox onlyWiFiCheckBox;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private Uri imageUri;
    private Realm realm;
    private UserItem userData;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private boolean isGPSDialogShowed;


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        startLocationUpdates();
    }

    @Override
    protected void onStop() {
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        realm = Realm.getInstance(this);
        if (checkLocationManeger()) {
            openCamera();
        }
        initViews();
        buildGoogleApiClient();

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(UploadService.UPDATE_PHOTO_UI));
    }

    private boolean checkLocationManeger() {
        if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (!isGPSDialogShowed) {
            LocationDialog.showSettingsAlert(this);
                isGPSDialogShowed = true;
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initGrid();
    }

    private void initGrid() {
        // Calling the RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // The number of Columns
        mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        RealmResults<PhotoItem> photos = realm.where(PhotoItem.class).findAll();
        mAdapter = new PhotoGridAdapter(photos, this);
        mRecyclerView.setAdapter(mAdapter);
        setPhotosListVisibility(photos.size() > 0);
    }

    private void initViews() {
        findViewById(R.id.bt_settings).setOnClickListener(this);
        findViewById(R.id.snap).setOnClickListener(this);
        findViewById(R.id.logout).setOnClickListener(this);
        onlyWiFiCheckBox = (CheckBox)findViewById(R.id.onlyWiFiCheckBox);
        onlyWiFiCheckBox.setOnClickListener(this);
        onlyWiFiCheckBox.setEnabled(ProjectPreferencesManager.getUploadWifiOnlyMode(this));
        noPhotosTextView = (TextView) findViewById(R.id.noPhotosTextView);
        llSettings = (LinearLayout) findViewById(R.id.ll_settings);

        userData = realm.where(UserItem.class).findFirst();
        ((TextView) findViewById(R.id.title)).setText(userData.getEmail());
    }

    private void setPhotosListVisibility(Boolean isExists) {
        if (isExists) {
            noPhotosTextView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            noPhotosTextView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_settings:
                showOrHideSettingsBlock();
                getTracker().send(new HitBuilders.EventBuilder()
                        .setCategory("Settings")
                        .setAction("bt_settings")
                        .build());
                break;
            case R.id.snap:
                if (checkLocationManeger()) {
                    openCamera();
                    getTracker().send(new HitBuilders.EventBuilder()
                            .setCategory("Settings")
                            .setAction("snap")
                            .build());
                }
                break;
            case R.id.onlyWiFiCheckBox:
                ProjectPreferencesManager.setUploadWifiOnlyMode(this, onlyWiFiCheckBox.isChecked());
                getTracker().send(new HitBuilders.EventBuilder()
                        .setCategory("Settings")
                        .setAction("onlyWiFiCheckBox")
                        .setLabel(String.valueOf(onlyWiFiCheckBox.isChecked()))
                        .build());
                break;
            case R.id.logout:
                UserItem userData = new UserItem();
                userData.setEmail(((TextView) findViewById(R.id.title)).getText().toString());
                userData.setIsLogin(false);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(userData);
                realm.commitTransaction();

                getTracker().send(new HitBuilders.EventBuilder()
                        .setCategory("Settings")
                        .setAction("logout")
                        .build());

                startActivity(new Intent(MainScreen.this, LoginActivity.class));
                finish();
                break;
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = CameraUtils.getOutputMediaFileUri(CameraUtils.MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void showOrHideSettingsBlock() {
        if (llSettings.getVisibility() != View.VISIBLE) {
            llSettings.setVisibility(View.VISIBLE);
        } else {
            llSettings.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        String pathToInternallyStoredImage = CameraUtils.saveToInternalStorage(this, CameraUtils.MEDIA_TYPE_IMAGE, imageUri);
        // Transactions give you easy thread-safety
        realm.beginTransaction();

        PhotoItem photo = new PhotoItem();
        photo.setId(String.valueOf(System.currentTimeMillis()));
        photo.setState(PhotoItem.STATE_IN_PROCESS);
        photo.setPhotoURL(pathToInternallyStoredImage);
        if (mLastLocation != null) {
            photo.setLatitude(mLastLocation.getLatitude());
            photo.setLongitude(mLastLocation.getLongitude());
        }
        realm.copyToRealmOrUpdate(photo);
        realm.commitTransaction();

        ((PhotoGridAdapter) mAdapter).addItem(photo);
        setPhotosListVisibility(true);

        int connectivityStatus = NetworkUtils.getConnectivityStatus(this);
        boolean isCanUpload = true;

        if (ProjectPreferencesManager.getUploadWifiOnlyMode(this))
            if (connectivityStatus != NetworkUtils.CONNECTION_WIFI)
                isCanUpload = false;

        if (isCanUpload && connectivityStatus != NetworkUtils.NOT_CONNECTED) {
            startService(new Intent(MainScreen.this, UploadService.class));
        }
    }



    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String photoId = intent.getStringExtra("id");
            int photoState = intent.getIntExtra("state", PhotoItem.STATE_IN_PROCESS);
            ((PhotoGridAdapter) mAdapter).updateItem(photoId, photoState);
        }
    };

    //LOCATION

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }
}
