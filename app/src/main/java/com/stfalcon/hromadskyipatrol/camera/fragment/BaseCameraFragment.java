package com.stfalcon.hromadskyipatrol.camera.fragment;


import android.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;

import com.stfalcon.hromadskyipatrol.camera.ICamera;
import com.stfalcon.hromadskyipatrol.models.ViolationItem;
import com.stfalcon.hromadskyipatrol.utils.FilesUtils;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by alex on 08.11.15.
 */
public class BaseCameraFragment extends Fragment {

    private static final String TAG = BaseCameraFragment.class.getName();
    private long detectViolationTime;
    public String violationFileURI;
    public String previousFileURI;

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
    private Handler handler;
    private Timer segmentTimer;
    private Timer violationTimer;
    private StopRecordSegmentTask stopRecordSegmentTask;
    private StopRecordViolationTask stopRecordViolationTask;


    private Runnable updateTimerRunnable = new Runnable() {
        public void run() {
            updateTimerView((int) (System.currentTimeMillis() - detectViolationTime) / 1000);
            if (violationRecording) {
                handler.postDelayed(updateTimerRunnable, 1000);
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
        handler = new Handler();
        segmentTimer = new Timer();
        violationTimer = new Timer();
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacks(updateTimerRunnable);
        prepareTimer();
        onStopRecord();
    }

    protected void initCamera() {
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
        violationRecording = false;
        handler.removeCallbacks(updateTimerRunnable);
        if (callback != null) {
            callback.onStopRecord();
        }
    }


    protected void createNewVideoFile() {
    }

    private void updateTimerView(int sec) {
        if (callback != null && violationRecording) {
            callback.onTime(sec);
        }
    }

    protected void startViolationRecording() {
        if (callback != null) {
            callback.onViolationDetected();
        }
        violationRecording = true;
        detectViolationTime = System.currentTimeMillis();

        stopRecordSegmentTask.cancel();
        stopRecordViolationTask = new StopRecordViolationTask();
        violationTimer.schedule(stopRecordViolationTask, TIME_RECORD_AFTER_TAP);
        handler.postDelayed(updateTimerRunnable, 1000);
    }


    public void startRecordSegment() {
        onStartRecord();
        stopRecordSegmentTask = new StopRecordSegmentTask();
        segmentTimer.schedule(stopRecordSegmentTask, TIME_RECORD_SEGMENT);
    }

    private void prepareTimer() {
        try {
            segmentTimer.cancel();
            violationTimer.cancel();
        } catch (IllegalStateException e) {
        }
    }


    class StopRecordSegmentTask extends TimerTask {

        @Override
        public void run() {
            if (!violationRecording && mIsRecordingVideo) {
                onStopRecord();
                if (previousFileURI != null) {
                    new File(previousFileURI).delete();
                }
                previousFileURI = violationFileURI;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initCamera();
                    }
                });
            }
        }
    }

    class StopRecordViolationTask extends TimerTask {

        @Override
        public void run() {
            if (callback != null) {
                Log.d(TAG, "run: " + violationFileURI);
                Log.d(TAG, "prev run: " + previousFileURI);
                if (previousFileURI != null) {
                    File prevVideo = new File(FilesUtils.getOutputInternalMediaFile(FilesUtils.MEDIA_TYPE_VIDEO).getAbsolutePath());
                    try {
                        FilesUtils.copyFile(new File(previousFileURI), prevVideo);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onVideoPrepared(new ViolationItem(violationFileURI, previousFileURI, detectViolationTime));
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onVideoPrepared(new ViolationItem(detectViolationTime, violationFileURI));
                        }
                    });
                }
                previousFileURI = violationFileURI;
            }
            if (violationRecording && mIsRecordingVideo) {

                onStopRecord();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initCamera();
                    }
                });
            }
        }
    }
}
