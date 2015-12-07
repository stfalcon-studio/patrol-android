package com.stfalcon.hromadskyipatrol.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.network.UploadService;
import com.stfalcon.hromadskyipatrol.utils.CameraUtils;
import com.stfalcon.hromadskyipatrol.utils.TrimVideoUtils;

import java.io.File;
import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmResults;

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

        RealmResults<VideoItem> videoList;

        Log.d(TAG, "onHandleIntent: start process video service");

        tryToProcessVideo();
    }

    private void tryToProcessVideo() {
        Realm realm = Realm.getInstance(this);
        VideoItem video = realm.where((VideoItem.class))
                .equalTo("state", VideoItem.STATE_SAVING)
                .findFirst();

        if (video == null) {
            return;
        }

        String id = video.getId();

        Log.d(TAG, "item: " + id);
        Log.d(TAG, "itemUrl: " + video.getVideoURL());
        File src = new File(video.getVideoURL());
        File dst = new File(CameraUtils.getOutputInternalMediaFile_App(CameraUtils.MEDIA_TYPE_VIDEO).getAbsolutePath());
        Log.d(TAG, "dst: " + dst.getAbsolutePath());
        try {
            realm.beginTransaction();
            if (TrimVideoUtils.trimToLast20sec(src, dst)) {
                video.setVideoURL(dst.getAbsolutePath());
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
            video.setState(VideoItem.STATE_READY_TO_SEND);
            realm.commitTransaction();
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
        intent.putExtra("state", VideoItem.STATE_READY_TO_SEND);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
