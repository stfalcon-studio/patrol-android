package com.stfalcon.dorozhnyjpatrul.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.stfalcon.dorozhnyjpatrul.R;
import com.stfalcon.dorozhnyjpatrul.models.Photo;
import com.stfalcon.dorozhnyjpatrul.models.PhotoAnswer;
import com.stfalcon.dorozhnyjpatrul.models.UserData;
import com.stfalcon.dorozhnyjpatrul.network.tasks.UploadImageTask;
import com.stfalcon.dorozhnyjpatrul.utils.CameraUtils;

import io.realm.Realm;

/**
 * Created by alexandr on 17/08/15.
 */
public class MainScreen extends BaseSpiceActivity implements View.OnClickListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    int REQUEST_CAMERA = 0;

    private LinearLayout llSettings;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private Uri imageUri;
    private Realm realm;
    private UploadRequestListener requestListener = new UploadRequestListener();
    private UserData userData;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;


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
    }

    private boolean checkLocationManeger() {
        if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            LocationDialog.showSettingsAlert(this);
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

        mAdapter = new GridAdapter(realm.where(Photo.class).findAll());
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initViews() {
        findViewById(R.id.bt_settings).setOnClickListener(this);
        findViewById(R.id.snap).setOnClickListener(this);
        findViewById(R.id.logout).setOnClickListener(this);
        llSettings = (LinearLayout) findViewById(R.id.ll_settings);

        userData = realm.where(UserData.class).findFirst();
        ((TextView) findViewById(R.id.title)).setText(userData.getEmail());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_settings:
                showOrHideSettingsBlock();
                break;
            case R.id.snap:
                if (checkLocationManeger()) {
                    openCamera();
                }
                break;
            case R.id.logout:
                UserData userData = new UserData();
                userData.setEmail(((TextView) findViewById(R.id.title)).getText().toString());
                userData.setIsLogin(false);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(userData);
                realm.commitTransaction();
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

        Photo photo = new Photo();
        photo.setId(String.valueOf(System.currentTimeMillis()));
        photo.setState(Photo.STATE_IN_PROCESS);
        photo.setPhotoURL(pathToInternallyStoredImage);
        if (mLastLocation != null) {
            photo.setLatitude(mLastLocation.getLatitude());
            photo.setLongitude(mLastLocation.getLongitude());
        }
        realm.copyToRealmOrUpdate(photo);
        realm.commitTransaction();

        ((GridAdapter) mAdapter).addItem(photo);

        if (mLastLocation != null) {
            getSpiceManager().execute(new UploadImageTask(userData.getId(),
                    photo.getId(), photo), requestListener);
        }
    }


    public final class UploadRequestListener implements RequestListener<PhotoAnswer> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(MainScreen.this, "Exception", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccess(PhotoAnswer photoData) {
            realm.beginTransaction();
            Photo photo = realm.where(Photo.class).contains("id", String.valueOf(photoData.getId())).findFirst();
            photo.setState(photoData.getState());
            realm.copyToRealmOrUpdate(photo);
            realm.commitTransaction();

            ((GridAdapter)mAdapter).updateItem(photo);
        }
    }


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
