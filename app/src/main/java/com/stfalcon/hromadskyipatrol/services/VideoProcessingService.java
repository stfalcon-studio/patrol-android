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
import java.io.IOException;
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
        File result = null;
        if (videoPrevURL != null) {
            File src2 = new File(videoPrevURL);
            result = new File(CameraUtils.getOutputInternalMediaFile_App(CameraUtils.MEDIA_TYPE_VIDEO).getAbsolutePath());
            ProcessVideoUtils.concatTwoVideos(src2, src, result);
        }
        File dst = new File(CameraUtils.getOutputInternalMediaFile_App(CameraUtils.MEDIA_TYPE_VIDEO).getAbsolutePath());
        Log.d(TAG, "dst: " + dst.getAbsolutePath());
        try {
            if (ProcessVideoUtils.trimToLast20sec(videoPrevURL != null && videoPrevURL.length() > 0 ? result : src, dst)) {
                db.updateVideo(id, dst.getAbsolutePath());
            } else {
                db.updateVideo(id, src.getAbsolutePath());
            }
            /*if (ProcessVideoUtils.trimToLast20sec(video.getVideoPrevURL() != null ? result : src, dst)) {
                video.setVideoURL(dst.getAbsolutePath());
                try {
                    Log.d(TAG, "remove + src: " + src.getAbsolutePath());
                    src.delete();
                    result.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                video.setVideoURL(video.getVideoPrevURL() != null ? result.getAbsolutePath() : src.getAbsolutePath());
                try {
                    Log.d(TAG, "remove + dst: " + dst.getAbsolutePath());
                    dst.delete();
                    result.delete();
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }*/

            db.updateVideo(video.getId(), VideoItem.State.READY_TO_SEND);
            updateUI(id);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            updateUI(id);
        }
    }

    private void updateUI(String id) {
        Intent intent = new Intent(UploadService.UPDATE_VIDEO_UI);
        intent.putExtra("id", id);
        intent.putExtra("state", VideoItem.State.READY_TO_SEND.value());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
