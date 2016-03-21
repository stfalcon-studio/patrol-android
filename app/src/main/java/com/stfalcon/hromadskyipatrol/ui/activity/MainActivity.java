package com.stfalcon.hromadskyipatrol.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.database.DatabasePatrol;
import com.stfalcon.hromadskyipatrol.location.LocationDialog;
import com.stfalcon.hromadskyipatrol.models.UserItem;
import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.services.UploadService;
import com.stfalcon.hromadskyipatrol.services.VideoProcessingService;
import com.stfalcon.hromadskyipatrol.ui.VideoGridAdapter;
import com.stfalcon.hromadskyipatrol.utils.Constants;
import com.stfalcon.hromadskyipatrol.utils.Extras;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by alexandr on 17/08/15.
 */
public class MainActivity extends BaseSpiceActivity
        implements View.OnClickListener, VideoGridAdapter.VideosListener {

    private TextView noVideosTextView;
    private LinearLayout llSettings;
    private CheckBox onlyWiFiCheckBox, autoUploadCheckBox, recorderCheckBox;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private VideoGridAdapter mAdapter;
    private UserItem userData;
    private boolean isGPSDialogShowed;
    private boolean mastShowClosingErrorExplanation = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkLocationManager() && ProjectPreferencesManager.getRecorderMode(this)) {
            openRegistratorMode();
        }
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initUIReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mastShowClosingErrorExplanation) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,
                            R.string.finish_record_error, Toast.LENGTH_LONG).show();
                    mastShowClosingErrorExplanation = false;
                }
            }, 3000);

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void initUIReceiver() {
        IntentFilter intentFilter = new IntentFilter(UploadService.UPDATE_VIDEO_UI);
        intentFilter.addAction(VideoProcessingService.ADD_VIDEO_UI);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, intentFilter);
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
        mRecyclerView.setItemAnimator(null);

        ArrayList<VideoItem> videos = DatabasePatrol.get(this).getVideos(userData);
        Collections.reverse(videos);
        mAdapter = new VideoGridAdapter(videos, this, this);
        mRecyclerView.setAdapter(mAdapter);
        setVideosListVisibility(videos.size() > 0);
    }


    private void initViews() {
        findViewById(R.id.bt_settings).setOnClickListener(this);
        findViewById(R.id.snap).setOnClickListener(this);
        findViewById(R.id.logout).setOnClickListener(this);
        findViewById(R.id.bt_add_video).setOnClickListener(this);
        findViewById(R.id.snapVideo).setOnClickListener(this);

        onlyWiFiCheckBox = (CheckBox) findViewById(R.id.onlyWiFiCheckBox);
        onlyWiFiCheckBox.setOnClickListener(this);
        onlyWiFiCheckBox.setChecked(ProjectPreferencesManager.getUploadWifiOnlyMode(this));

        autoUploadCheckBox = (CheckBox) findViewById(R.id.autoUploadCheckBox);
        autoUploadCheckBox.setOnClickListener(this);
        autoUploadCheckBox.setChecked(ProjectPreferencesManager.getAutoUploadMode(this));

        recorderCheckBox = (CheckBox) findViewById(R.id.startWithRecording);
        recorderCheckBox.setOnClickListener(this);
        recorderCheckBox.setChecked(ProjectPreferencesManager.getRecorderMode(this));

        noVideosTextView = (TextView) findViewById(R.id.noVideosTextView);
        llSettings = (LinearLayout) findViewById(R.id.ll_settings);

        userData = ProjectPreferencesManager.getUser(this);
        ((TextView) findViewById(R.id.title)).setText(getString(R.string.app_name));
        ((TextView) findViewById(R.id.userName)).setText(userData.getEmail());
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
                    openRegistratorMode();
                    getTracker().send(new HitBuilders.EventBuilder()
                            .setCategory("Settings")
                            .setAction("snap")
                            .build());
                } else {
                    LocationDialog.showSettingsAlert(this);
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
            case R.id.startWithRecording:
                ProjectPreferencesManager.setRecorderMode(this, recorderCheckBox.isChecked());
                getTracker().send(new HitBuilders.EventBuilder()
                        .setCategory("Settings")
                        .setAction("recorderCheckBox")
                        .setLabel(String.valueOf(autoUploadCheckBox.isChecked()))
                        .build());
                break;
            case R.id.logout:
                UserItem userData = new UserItem();
                userData.setEmail(((TextView) findViewById(R.id.title)).getText().toString());
                userData.setIsLogin(false);
                ProjectPreferencesManager.setUser(this, userData);


                getTracker().send(new HitBuilders.EventBuilder()
                        .setCategory("Settings")
                        .setAction("logout")
                        .build());

                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                break;
            case R.id.bt_add_video:
                startActivityForResult(new Intent(MainActivity.this, UploadVideoActivity.class),
                        UploadVideoActivity.UPLOAD_VIDEO);
                break;
            case R.id.snapVideo:
                if (checkLocationManager()) {
                    startActivityForResult(
                            new Intent(MainActivity.this, VideoModeActivity.class)
                                    .putExtra(Extras.VIDEO, VideoModeActivity.VIDEO_CAPTURE),
                            VideoModeActivity.REQUEST_VIDEO_CAPTURE);
                } else {
                    LocationDialog.showSettingsAlert(this);
                }
                break;
        }
    }

    private void openRegistratorMode() {
        Intent intent = new Intent(this, VideoCaptureActivity.class);
        startActivityForResult(intent, Constants.REQUEST_CAMERA);
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

        switch (requestCode) {
            case Constants.REQUEST_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    processNewContent();
                } else {
                    mastShowClosingErrorExplanation = true;
                }
                break;
            case Constants.REQUEST_GPS_SETTINGS:
                if (checkLocationManager()) {
                    openRegistratorMode();
                }
                break;
            case UploadVideoActivity.UPLOAD_VIDEO:
            case VideoModeActivity.REQUEST_VIDEO_CAPTURE:
                if (resultCode == Activity.RESULT_OK) {
                    processNewContent();
                }
        }
    }

    private void processNewContent() {
        initGrid();
        startService(new Intent(MainActivity.this, VideoProcessingService.class));
    }

    @Override
    public void onVideosEmpty() {
        setVideosListVisibility(false);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UploadService.UPDATE_VIDEO_UI)) {
                String id = intent.getExtras().getString(Extras.ID);
                VideoItem.State state = VideoItem.State.from(intent.getExtras().getInt(Extras.STATE));
                mAdapter.updateState(id, state);
                if (intent.hasExtra(Extras.URL)) {
                    mAdapter.updateUrl(id, intent.getExtras().getString(Extras.URL));
                }
            } else if (intent.getAction().equals(VideoProcessingService.ADD_VIDEO_UI)) {
                String id = intent.getExtras().getString(Extras.ID);
                mAdapter.addItem(DatabasePatrol.get(MainActivity.this).getVideo(id));
                setVideosListVisibility(true);
            }
        }
    };

}