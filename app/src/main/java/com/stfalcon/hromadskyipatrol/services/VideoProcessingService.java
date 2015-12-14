package com.stfalcon.hromadskyipatrol.services;

import android.app.IntentService;
import android.content.Intent;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.stfalcon.hromadskyipatrol.camera.VideoCaptureActivity;
import com.stfalcon.hromadskyipatrol.database.DatabasePatrol;
import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.models.ViolationItem;
import com.stfalcon.hromadskyipatrol.network.UploadService;
import com.stfalcon.hromadskyipatrol.utils.Extras;
import com.stfalcon.hromadskyipatrol.utils.Constants;
import com.stfalcon.hromadskyipatrol.utils.FilesUtils;
import com.stfalcon.hromadskyipatrol.utils.ProcessVideoUtils;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;
import com.stfalcon.hromadskyipatrol.utils.VideoThumbUtils;

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
    public static final String ADD_VIDEO_UI = "videoAdded";

    public VideoProcessingService() {
        super(VideoProcessingService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DatabasePatrol db = DatabasePatrol.get(this);

        // add new video to db if need
        if (intent.hasExtra(VideoCaptureActivity.MOVIES_RESULT)) {
            addVideos(intent);
        }

        Log.d(TAG, "onHandleIntent: start process video service");
        ArrayList<VideoItem> videoItems = db.getVideos(
                VideoItem.State.SAVING,
                ProjectPreferencesManager.getUser(this)
        );
        for (int i = 0; i < videoItems.size(); i++) {
            tryToProcessVideo(videoItems.get(i), db);
        }

        if (ProjectPreferencesManager.getAutoUploadMode(getApplicationContext())) {
            startService(new Intent(VideoProcessingService.this, UploadService.class));
        }
    }

    private void addVideos(Intent data) {

        DatabasePatrol db = DatabasePatrol.get(this);

        ArrayList<ViolationItem> violationItems
                = data.getParcelableArrayListExtra(VideoCaptureActivity.MOVIES_RESULT);
        String ownerEmail = data.getStringExtra(Constants.EXTRAS_OWNER_EMAIL);

        if (!violationItems.isEmpty()) {
            int i = 0;
            for (ViolationItem item : violationItems) {
                VideoItem video = new VideoItem();
                video.setId(String.valueOf(System.currentTimeMillis() + i++));
                video.setVideoPrevURL(item.videoUrlPrev);
                video.setVideoURL(item.videoUrl);
                video.setLatitude(item.getLat());
                video.setLongitude(item.getLon());
                video.setState(VideoItem.State.SAVING);
                video.setOwnerEmail(ownerEmail);

                String thumbUrl = VideoThumbUtils.makeThumb(ThumbnailUtils.createVideoThumbnail(video.getVideoURL(),
                        MediaStore.Images.Thumbnails.MINI_KIND));

                video.setThumb(thumbUrl);

                db.addVideo(video);

                addVideoToUI(video.getId());
            }
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
            File result = new File(FilesUtils.getOutputInternalMediaFile(FilesUtils.MEDIA_TYPE_VIDEO).getAbsolutePath());
            ProcessVideoUtils.concatTwoVideos(src2, src, result);
            deleteFile(src);
            deleteFile(src2);
            src = result;
        }
        File dst = new File(FilesUtils.getOutputInternalMediaFile(FilesUtils.MEDIA_TYPE_VIDEO).getAbsolutePath());

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
            db.updateVideo(video.getId(), VideoItem.State.ERROR);
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
        intent.putExtra(Extras.ID, id);
        intent.putExtra(Extras.STATE, VideoItem.State.READY_TO_SEND.value());
        intent.putExtra(Extras.URL, url);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void addVideoToUI(String id) {
        Intent intent = new Intent(ADD_VIDEO_UI);
        intent.putExtra(Extras.ID, id);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
