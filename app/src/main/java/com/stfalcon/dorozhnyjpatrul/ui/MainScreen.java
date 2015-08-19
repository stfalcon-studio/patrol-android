package com.stfalcon.dorozhnyjpatrul.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stfalcon.dorozhnyjpatrul.R;
import com.stfalcon.dorozhnyjpatrul.models.Photo;
import com.stfalcon.dorozhnyjpatrul.models.UserData;
import com.stfalcon.dorozhnyjpatrul.utils.CameraUtils;

import io.realm.Realm;

/**
 * Created by alexandr on 17/08/15.
 */
public class MainScreen extends AppCompatActivity implements View.OnClickListener {
    int REQUEST_CAMERA = 0;

    private LinearLayout llSettings;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private Uri imageUri;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        realm = Realm.getInstance(this);
        openCamera();
        initViews();
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

        UserData userData = realm.where(UserData.class).findFirst();
        ((TextView)findViewById(R.id.title)).setText(userData.getEmail());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_settings:
                showOrHideSettingsBlock();
                break;
            case R.id.snap:
                openCamera();
                break;
            case R.id.logout:
                UserData userData = new UserData();
                userData.setEmail(((TextView)findViewById(R.id.title)).getText().toString());
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
        /*Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);*/

        String pathToInternallyStoredImage = CameraUtils.saveToInternalStorage(this, CameraUtils.MEDIA_TYPE_IMAGE, imageUri);

        // Transactions give you easy thread-safety
        realm.beginTransaction();

        Photo photo = new Photo();
        photo.setId((int) System.currentTimeMillis());
        photo.setState(Photo.STATE_IN_PROCESS);
        photo.setPhotoURL(pathToInternallyStoredImage);
        realm.copyToRealmOrUpdate(photo);
        realm.commitTransaction();

        ((GridAdapter) mAdapter).addItem(photo);
    }
}
