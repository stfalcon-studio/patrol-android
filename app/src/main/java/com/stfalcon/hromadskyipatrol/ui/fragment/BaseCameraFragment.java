package com.stfalcon.hromadskyipatrol.ui.fragment;


import android.app.Fragment;

import com.stfalcon.hromadskyipatrol.camera.CircularEncoder;
import com.stfalcon.hromadskyipatrol.camera.ICamera;

/**
 * Created by alex on 08.11.15.
 */
public class BaseCameraFragment extends Fragment {
    public ICamera callback;
    private CircularEncoder mCircEncoder;

    public  void addCameraCallback(ICamera callback){
        this.callback = callback;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCircEncoder != null) {
            mCircEncoder.shutdown();
            mCircEncoder = null;
        }
    }

    public void onStartSecord(){
        if (callback != null){
            callback.onStartRecord();
        }
    }

    public void onStopRecord(){
        if (callback != null){
            callback.onStopRecord();
        }
    }
}
