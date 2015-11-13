package com.stfalcon.hromadskyipatrol.ui.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.camera.ICamera;
import com.stfalcon.hromadskyipatrol.ui.fragment.BaseCameraFragment;
import com.stfalcon.hromadskyipatrol.ui.fragment.Camera2VideoFragment;
import com.stfalcon.hromadskyipatrol.ui.fragment.CameraVideoFragment;
import com.stfalcon.hromadskyipatrol.utils.AnimationUtils;

public class VideoCaptureActivity extends AppCompatActivity implements ICamera, View.OnClickListener {

    private BaseCameraFragment cameraFragment;
    private TextView message;
    private TextView time;
    private ImageButton mainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        initViews();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (null == savedInstanceState) {
            initCameraFragmentForAPIVersion(Build.VERSION.SDK_INT);
        }
    }

    private void initViews() {
        message = (TextView) findViewById(R.id.tv_message);
        time = (TextView) findViewById(R.id.tv_message);
        mainMenu = (ImageButton)  findViewById(R.id.bt_main_screen);
        mainMenu.setOnClickListener(this);
    }


    public void initCameraFragmentForAPIVersion(int ver){
        cameraFragment = getSupportCamera(ver);
        cameraFragment.addCameraCallback(this);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, cameraFragment)
                .commit();
    }

    private BaseCameraFragment getSupportCamera(int ver) {
        return ver < Build.VERSION_CODES.LOLLIPOP ?
                CameraVideoFragment.newInstance() :
                Camera2VideoFragment.newInstance();
    }

    @Override
    public void onStartRecord() {
        showRecordMessage();
    }

    @Override
    public void onStopRecord() {
        hideRecordMessage();
    }

    @Override
    public void onTime() {

    }

    private void startMenuActivity() {
        Intent intent = new Intent();
        //intent.putExtra("moviesUrls", moviesUrls.toArray());
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void showRecordMessage() {
        message.startAnimation(AnimationUtils.getBlinkAnimation());
        message.setVisibility(View.VISIBLE);
    }

    public void hideRecordMessage() {
        message.clearAnimation();
        message.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_main_screen:
                startMenuActivity();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        startMenuActivity();
    }
}

