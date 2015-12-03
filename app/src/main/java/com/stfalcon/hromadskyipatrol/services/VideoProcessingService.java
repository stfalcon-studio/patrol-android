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
        Realm realm = Realm.getInstance(this);

        RealmResults<VideoItem> videoList;

        Log.d(TAG, "onHandleIntent: start process video service");


        videoList = realm.where((VideoItem.class))
                .equalTo("state", VideoItem.STATE_SAVING)
                .findAll();

        for (int i = 0; i < videoList.size(); i++) {
            String id = videoList.get(i).getId();

            Log.d(TAG, "item: " + id);
            Log.d(TAG, "itemUrl: " + videoList.get(i).getVideoURL());
            File src = new File(videoList.get(i).getVideoURL());
            File dst = new File(CameraUtils.getOutputInternalMediaFile_App(CameraUtils.MEDIA_TYPE_VIDEO).getAbsolutePath());
            Log.d(TAG, "dst: " + dst.getAbsolutePath());
            try {
                realm.beginTransaction();
                TrimVideoUtils.trimToLast20sec(src, dst);
                videoList.get(i).setVideoURL(dst.getAbsolutePath());
                videoList.get(i).setState(VideoItem.STATE_READY_TO_SEND);
                realm.commitTransaction();
                updateUI(id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private void updateUI(String id) {
        Intent intent = new Intent(UploadService.UPDATE_VIDEO_UI);
        intent.putExtra("id", id);
        intent.putExtra("state", VideoItem.STATE_READY_TO_SEND);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
