package com.stfalcon.hromadskyipatrol.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.stfalcon.hromadskyipatrol.database.DatabasePatrol;
import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.network.UploadService;
import com.stfalcon.hromadskyipatrol.utils.CameraUtils;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;
import com.stfalcon.hromadskyipatrol.utils.ProcessVideoUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


/**
 * Created by Anton Bevza on 12/1/15.
 */
public class VideoProcessingService extends IntentService {

    private static final String TAG = VideoProcessingService.class.getName();

    public VideoProcessingService() {
        super(VideoProcessingService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DatabasePatrol db = DatabasePatrol.get(this);

        Log.d(TAG, "onHandleIntent: start process video service");
        ArrayList<VideoItem> videoItems = db.getVideos(VideoItem.State.SAVING);
        for (int i = 0; i < videoItems.size(); i++) {
            tryToProcessVideo(videoItems.get(i), db);
        }

        if (ProjectPreferencesManager.getAutoUploadMode(getApplicationContext())) {
            startService(new Intent(VideoProcessingService.this, UploadService.class));
        }
    }

    private void tryToProcessVideo(VideoItem video, DatabasePatrol db) {
        String id = video.getId();
        Log.d(TAG, "item: " + id);
        Log.d(TAG, "itemUrl: " + video.getVideoURL());
        File src = new File(video.getVideoURL());
        String videoPrevURL = video.getVideoPrevURL();
        if (videoPrevURL != null) {
            File src2 = new File(videoPrevURL);
            File result = new File(CameraUtils.getOutputInternalMediaFile_App(CameraUtils.MEDIA_TYPE_VIDEO).getAbsolutePath());
            ProcessVideoUtils.concatTwoVideos(src2, src, result);
            deleteFile(src);
            deleteFile(src2);
            src = result;
        }
        File dst = new File(CameraUtils.getOutputInternalMediaFile_App(CameraUtils.MEDIA_TYPE_VIDEO).getAbsolutePath());
        Log.d(TAG, "dst: " + dst.getAbsolutePath());
        try {
            if (ProcessVideoUtils.trimToLast20sec(src, dst)) {
                deleteFile(src);
                video.setVideoURL(dst.getAbsolutePath());
            } else {
                deleteFile(dst);
                video.setVideoURL(src.getAbsolutePath());
            }
            db.updateVideo(id, video.getVideoURL());
            db.updateVideo(video.getId(), VideoItem.State.READY_TO_SEND);
            updateUI(id, video.getVideoURL());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteFile(File file) {
        try {
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUI(String id, String url) {
        Intent intent = new Intent(UploadService.UPDATE_VIDEO_UI);
        intent.putExtra("id", id);
        intent.putExtra("state", VideoItem.State.READY_TO_SEND.value());
        intent.putExtra("url", url);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
