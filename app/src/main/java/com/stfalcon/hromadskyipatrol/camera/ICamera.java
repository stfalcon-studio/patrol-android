package com.stfalcon.hromadskyipatrol.camera;

import com.stfalcon.hromadskyipatrol.models.ViolationItem;

/**
 * Created by alex on 08.11.15.
 */
public interface ICamera {
    void onCameraPrepared();
    void onStartRecord();
    void onStopRecord();
    void onVideoPrepared(ViolationItem violationItem);
    void onViolationDetected();
    void onTime(int sec);
}
