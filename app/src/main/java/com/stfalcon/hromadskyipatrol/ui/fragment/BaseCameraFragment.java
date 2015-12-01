package com.stfalcon.hromadskyipatrol.ui.fragment;


import android.app.Fragment;
import android.os.Handler;
import android.util.SparseIntArray;
import android.view.Surface;

import com.stfalcon.hromadskyipatrol.camera.ICamera;
import com.stfalcon.hromadskyipatrol.models.ViolationItem;

/**
 * Created by alex on 08.11.15.
 */
public class BaseCameraFragment extends Fragment {

    private long detectViolationTime;
    protected String violationFileURI;

    private int TIME_RECORD_AFTER_TAP = 10 * 1000; //10sec
    //private int TIME_RECORD_SEGMENT = 2 * 60 * 1000;  //2 min
    private int TIME_RECORD_SEGMENT = 30 * 1000;// 30sec

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public ICamera callback;
    private Handler violationRecordHandler;
    private Handler segmentRecordHandler;
    private Runnable updateTimerRunnable = new Runnable() {
        public void run() {
            updateTimerView((int) (System.currentTimeMillis() - detectViolationTime) / 1000);
            if (violationRecording) {
                violationRecordHandler.postDelayed(updateTimerRunnable, 1000);
            }
        }
    };

    public boolean violationRecording = false;
    public boolean mIsRecordingVideo = false;


    public void addCameraCallback(ICamera callback) {
        this.callback = callback;
    }

    @Override
    public void onStart() {
        super.onStart();
        violationRecordHandler = new Handler();
        segmentRecordHandler = new Handler();
    }

    @Override
    public void onStop() {
        super.onStop();
        onStopRecord();
        violationRecordHandler.removeCallbacks(updateTimerRunnable);
        violationRecordHandler.removeCallbacks(stopRecordViolationRecordingRunnable);
        segmentRecordHandler.removeCallbacks(stopRecordSegmentRunnable);
    }

    protected void onCameraPrepared() {
        if (callback != null) {
            callback.onCameraPrepared();
        }
    }

    protected void onStartRecord() {
        if (callback != null) {
            callback.onStartRecord();
        }
    }

    protected void onStopRecord() {
        if (callback != null) {
            callback.onStopRecord();
        }
        violationRecording = false;
    }

    private void updateTimerView(int sec) {
        if (callback != null) {
            callback.onTime(sec);
        }
    }

    protected void startViolationRecording() {
        if (callback != null) {
            callback.onViolationDetected();
        }
        violationRecording = true;
        detectViolationTime = System.currentTimeMillis();
        violationRecordHandler.postDelayed(stopRecordViolationRecordingRunnable, TIME_RECORD_AFTER_TAP);
        violationRecordHandler.postDelayed(updateTimerRunnable, 1000);
    }


    public void startRecordSegment() {
        onStartRecord();
        segmentRecordHandler.postDelayed(stopRecordSegmentRunnable, TIME_RECORD_SEGMENT);
    }


    private Runnable stopRecordSegmentRunnable = new Runnable() {
        @Override
        public void run() {
            if (!violationRecording && mIsRecordingVideo) {
                onStopRecord();
            }
        }
    };

    private Runnable stopRecordViolationRecordingRunnable = new Runnable() {
        @Override
        public void run() {
            onStopRecord();
            if (callback != null) {
                callback.onVideoPrepared(new ViolationItem(detectViolationTime, violationFileURI));
            }
        }
    };
}
