package com.stfalcon.hromadskyipatrol.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.ui.fragment.Camera2VideoFragment;

public class VideoCaptureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2VideoFragment.newInstance())
                    .commit();
        }
    }
}

