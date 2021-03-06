/*
 * Copyright (c) 2015 - 2016. Stepan Tanasiychuk
 *
 *     This file is part of Gromadskyi Patrul is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Found ation, version 3 of the License, or any later version.
 *
 *     If you would like to use any part of this project for commercial purposes, please contact us
 *     for negotiating licensing terms and getting permission for commercial use.
 *     Our email address: info@stfalcon.com
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stfalcon.hromadskyipatrol.camera.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.camera.CameraHelper;
import com.stfalcon.hromadskyipatrol.utils.FilesUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by alex on 08.11.15.
 */


@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class CameraVideoFragment extends BaseCameraFragment implements View.OnClickListener, TextureView.SurfaceTextureListener {

    private TextureView mPreview;
    private static final String TAG = "Recorder";
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private AudioManager mgr;

    public static CameraVideoFragment newInstance() {
        return new CameraVideoFragment();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.camera_screen, container, false);
        initViews(rootView);
        return rootView;
    }


    private void initViews(View rootView) {
        mPreview = (TextureView) rootView.findViewById(R.id.texture);
        mPreview.setOnClickListener(this);
        mPreview.setSurfaceTextureListener(this);
    }

    @Override
    protected void onStartRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.start();
        }
        mIsRecordingVideo = true;
        super.onStartRecord();
    }

    @Override
    protected void onStopRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
        }
        releaseMediaRecorder(); // release the MediaRecorder object
        mIsRecordingVideo = false;
        releaseCamera();
        super.onStopRecord();
        mgr = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mgr.setStreamMute(AudioManager.STREAM_SYSTEM, false);
    }

    @Override
    protected void initCamera() {
        if (mPreview.isAvailable()) {
            prepareCamera(mPreview.getWidth(), mPreview.getHeight());
        } else {
            mPreview.setSurfaceTextureListener(this);
        }
    }

    /**
     * The capture button controls all user interaction. When recording, the button click
     * stops recording, releases {@link android.media.MediaRecorder} and {@link android.hardware.Camera}. When not recording,
     * it prepares the {@link android.media.MediaRecorder} and starts recording.
     */
    public void onCaptureClick() {
        if (mIsRecordingVideo && !violationRecording) {
            startViolationRecording();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (violationFileURI != null) {
            try {
                new File(violationFileURI).delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // if we are using MediaRecorder, release it first
        releaseMediaRecorder();
        // release the camera immediately on pause event
        releaseCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        initCamera();
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera.lock();
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private boolean prepareVideoRecorder(int width, int height) {

        // BEGIN_INCLUDE (configure_preview)
        mCamera = Camera.open();
        mgr = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mgr.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalPreviewSize(mSupportedPreviewSizes,
                width, height);

        // Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;

        // likewise for the camera object itself.
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mCamera.setParameters(parameters);
        try {
            // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
            // with {@link SurfaceView}
            mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
        } catch (IOException e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
            return false;
        }
        mCamera.startPreview();

        // END_INCLUDE (configure_preview)


        // BEGIN_INCLUDE (configure_media_recorder)
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(profile);

        // Step 4: Set output file
        violationFileURI = FilesUtils.getOutputExternalMediaFile(
                FilesUtils.MEDIA_TYPE_VIDEO).getAbsolutePath();

        mMediaRecorder.setOutputFile(violationFileURI);
        // END_INCLUDE (configure_media_recorder)

        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }


    private void prepareCamera(int width, int height) {
        if (prepareVideoRecorder(width, height)) {
            onCameraPrepared();
        } else {
            releaseMediaRecorder();
            releaseCamera();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.texture:
                onCaptureClick();
                break;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        prepareCamera(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        releaseCamera();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

}
