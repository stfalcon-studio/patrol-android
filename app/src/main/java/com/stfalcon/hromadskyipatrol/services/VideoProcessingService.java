package com.stfalcon.hromadskyipatrol.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.network.UploadService;
import com.stfalcon.hromadskyipatrol.utils.CameraUtils;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;
import com.stfalcon.hromadskyipatrol.utils.ProcessVideoUtils;

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
        if (ProjectPreferencesManager.getAutoUploadMode(getApplicationContext())) {
            startService(new Intent(VideoProcessingService.this, UploadService.class));
        }
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
        String videoPrevURL = video.getVideoPrevURL();
        File src2 = new File(videoPrevURL);
        File dst = new File(CameraUtils.getOutputInternalMediaFile_App(CameraUtils.MEDIA_TYPE_VIDEO).getAbsolutePath());
        File result = new File(CameraUtils.getOutputInternalMediaFile_App(CameraUtils.MEDIA_TYPE_VIDEO).getAbsolutePath());
        if (videoPrevURL != null && videoPrevURL.length() > 0) {
            ProcessVideoUtils.concatTwoVideos(src2, src, result);
        }
        Log.d(TAG, "dst: " + dst.getAbsolutePath());
        try {
            realm.beginTransaction();
            if (ProcessVideoUtils.trimToLast20sec(videoPrevURL != null && videoPrevURL.length() > 0 ? result : src, dst)) {
                video.setVideoURL(dst.getAbsolutePath());
            } else {
                video.setVideoURL(src.getAbsolutePath());
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


            video.setState(VideoItem.STATE_READY_TO_SEND);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            realm.commitTransaction();
            updateUI(id);
        }

        //try to process next video if present
        try {
            tryToProcessVideo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUI(String id) {
        Intent intent = new Intent(UploadService.UPDATE_VIDEO_UI);
        intent.putExtra("id", id);
        intent.putExtra("state", VideoItem.STATE_READY_TO_SEND);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
