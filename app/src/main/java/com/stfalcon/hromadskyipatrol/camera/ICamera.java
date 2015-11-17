package com.stfalcon.hromadskyipatrol.camera;

/**
 * Created by alex on 08.11.15.
 */
public interface ICamera {
    void onCameraPrepared();
    void onStartRecord();
    void onStopRecord();
    void onVideoPrepared();
    void onViolationDetected();
    void onTime(int sec);
}
