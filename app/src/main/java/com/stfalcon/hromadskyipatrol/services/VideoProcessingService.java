package com.stfalcon.hromadskyipatrol.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.stfalcon.hromadskyipatrol.database.DatabasePatrol;
import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.network.UploadService;
import com.stfalcon.hromadskyipatrol.utils.FilesUtils;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;
import com.stfalcon.hromadskyipatrol.utils.TrimVideoUtils;

import java.io.File;
import java.io.IOException;


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

        Log.d(TAG, "onHandleIntent: start process video service");

        tryToProcessVideo();
        if (ProjectPreferencesManager.getAutoUploadMode(getApplicationContext())) {
            startService(new Intent(VideoProcessingService.this, UploadService.class));
        }
    }

    private void tryToProcessVideo() {
        DatabasePatrol db = DatabasePatrol.get(this);
        VideoItem video = db.getVideo(VideoItem.State.SAVING);

        if (video == null) {
            return;
        }

        String id = video.getId();

        Log.d(TAG, "item: " + id);
        Log.d(TAG, "itemUrl: " + video.getVideoURL());
        File src = new File(video.getVideoURL());
        File dst = new File(FilesUtils.getOutputInternalMediaFile_App(FilesUtils.MEDIA_TYPE_VIDEO).getAbsolutePath());
        Log.d(TAG, "dst: " + dst.getAbsolutePath());
        try {
            if (TrimVideoUtils.trimToLast20sec(src, dst)) {
                db.updateVideo(video.getId(), dst.getAbsolutePath());
                try {
                    Log.d(TAG, "remove + src: " + src.getAbsolutePath());
                    src.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Log.d(TAG, "remove + dst: " + dst.getAbsolutePath());
                    dst.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            db.updateVideo(video.getId(), VideoItem.State.READY_TO_SEND);
            updateUI(id);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //try to process next video if present
        tryToProcessVideo();
    }

    private void updateUI(String id) {
        Intent intent = new Intent(UploadService.UPDATE_VIDEO_UI);
        intent.putExtra("id", id);
        intent.putExtra("state", VideoItem.State.READY_TO_SEND.value());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
