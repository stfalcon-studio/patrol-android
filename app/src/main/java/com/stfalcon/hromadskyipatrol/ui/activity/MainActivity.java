package com.stfalcon.hromadskyipatrol.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.models.UserItem;
import com.stfalcon.hromadskyipatrol.network.UploadService;
import com.stfalcon.hromadskyipatrol.network.WaitLocationService;
import com.stfalcon.hromadskyipatrol.ui.LocationDialog;
import com.stfalcon.hromadskyipatrol.ui.VideoGridAdapter;
import com.stfalcon.hromadskyipatrol.utils.CameraUtils;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by alexandr on 17/08/15.
 */
public class MainActivity extends BaseSpiceActivity implements View.OnClickListener {
    int REQUEST_CAMERA = 0;

    private TextView noVideosTextView;
    private LinearLayout llSettings;
    private CheckBox onlyWiFiCheckBox, autoUploadCheckBox;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private Uri videoUri;
    private Realm realm;
    private UserItem userData;
    private boolean isGPSDialogShowed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        realm = Realm.getInstance(this);
        if (checkLocationManager()) {
            openCamera();
        }
        initViews();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(UploadService.UPDATE_VIDEO_UI));
    }


    private boolean checkLocationManager() {
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

        RealmResults<VideoItem> videos = realm.where(VideoItem.class).findAll();
        mAdapter = new VideoGridAdapter(videos, this);
        mRecyclerView.setAdapter(mAdapter);
        setVideosListVisibility(videos.size() > 0);
    }


    private void initViews() {
        findViewById(R.id.bt_settings).setOnClickListener(this);
        findViewById(R.id.snap).setOnClickListener(this);
        findViewById(R.id.logout).setOnClickListener(this);

        onlyWiFiCheckBox = (CheckBox) findViewById(R.id.onlyWiFiCheckBox);
        onlyWiFiCheckBox.setOnClickListener(this);
        onlyWiFiCheckBox.setChecked(ProjectPreferencesManager.getUploadWifiOnlyMode(this));

        autoUploadCheckBox = (CheckBox) findViewById(R.id.autoUploadCheckBox);
        autoUploadCheckBox.setOnClickListener(this);
        autoUploadCheckBox.setChecked(ProjectPreferencesManager.getAutoUploadMode(this));

        noVideosTextView = (TextView) findViewById(R.id.noVideosTextView);
        llSettings = (LinearLayout) findViewById(R.id.ll_settings);

        userData = realm.where(UserItem.class).findFirst();
        ((TextView) findViewById(R.id.title)).setText(userData.getEmail());
    }


    private void setVideosListVisibility(Boolean isExists) {
        if (isExists) {
            noVideosTextView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            noVideosTextView.setVisibility(View.VISIBLE);
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
                if (checkLocationManager()) {
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
            case R.id.autoUploadCheckBox:
                ProjectPreferencesManager.setAutoUploadMode(this, autoUploadCheckBox.isChecked());
                getTracker().send(new HitBuilders.EventBuilder()
                        .setCategory("Settings")
                        .setAction("autoUploadCheckBox")
                        .setLabel(String.valueOf(autoUploadCheckBox.isChecked()))
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

                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                break;
        }
    }

    private void openCamera() {
        Intent intent = new Intent(this, VideoCaptureActivity.class);
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
                onCaptureVideoResult(data);
        }
    }

    private void onCaptureVideoResult(Intent data) {
        String pathToInternallyStoredImage = CameraUtils.saveToInternalStorage(CameraUtils.MEDIA_TYPE_VIDEO, videoUri);
        // Transactions give you easy thread-safety
        realm.beginTransaction();

        VideoItem video = new VideoItem();
        video.setId(String.valueOf(System.currentTimeMillis()));
        video.setState(VideoItem.STATE_IN_PROCESS);
        video.setVideoURL(pathToInternallyStoredImage);
        video.setState(VideoItem.STATE_SAVING);
        realm.copyToRealmOrUpdate(video);
        realm.commitTransaction();

        ((VideoGridAdapter) mAdapter).addItem(video);
        setVideosListVisibility(true);

        startService(new Intent(MainActivity.this, WaitLocationService.class));
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String videoId = intent.getStringExtra("id");
            int videoState = intent.getIntExtra("state", VideoItem.STATE_IN_PROCESS);
            ((VideoGridAdapter) mAdapter).updateItem(videoId, videoState);
        }
    };

}