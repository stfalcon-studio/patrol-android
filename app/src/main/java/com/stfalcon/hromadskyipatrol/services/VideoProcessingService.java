package com.stfalcon.hromadskyipatrol.services;

import android.app.IntentService;
import android.content.Intent;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.stfalcon.hromadskyipatrol.ui.activity.VideoCaptureActivity;
import com.stfalcon.hromadskyipatrol.database.DatabasePatrol;
import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.models.ViolationItem;
import com.stfalcon.hromadskyipatrol.utils.Extras;
import com.stfalcon.hromadskyipatrol.utils.FilesUtils;
import com.stfalcon.hromadskyipatrol.utils.ProcessVideoUtils;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;
import com.stfalcon.hromadskyipatrol.utils.VideoThumbUtils;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by Anton Bevza on 12/1/15.
 */
public class VideoProcessingService extends IntentService {

    private static final String TAG = VideoProcessingService.class.getName();
    public static final String ADD_VIDEO_UI = "videoAdded";
    public static final String DELETE_MOVIE = "delete_movie";

    public VideoProcessingService() {
        super(VideoProcessingService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: start process video service");

        DatabasePatrol db = DatabasePatrol.get(this);

        // add new video to db if need
        if (intent.hasExtra(VideoCaptureActivity.MOVIES_TO_SAVE)) {
            addVideo(intent);
        }

        //delete video
        else if (intent.hasExtra(DELETE_MOVIE)) {
            deleteVideo(intent);
        }

        //process all videos
        else {
            ArrayList<VideoItem> videoItems = db.getVideos(
                    VideoItem.State.SAVING,
                    ProjectPreferencesManager.getUser(this));

            if (!videoItems.isEmpty()) {
                for (VideoItem item : videoItems) {
                    tryToProcessVideo(item, db);
                }
            }
        }

        // start auto upload service if need
        if (ProjectPreferencesManager.getAutoUploadMode(getApplicationContext())) {
            startService(new Intent(VideoProcessingService.this, UploadService.class));
        }
    }

    private void deleteVideo(Intent intent) {
        String id = intent.getStringExtra(DELETE_MOVIE);
        DatabasePatrol db = DatabasePatrol.get(this);
        VideoItem video = db.getVideo(id);
        FilesUtils.removeFile(video.getVideoURL());
        if (video.getThumb() != null) {
            FilesUtils.removeFile(video.getThumb());
        }
        DatabasePatrol.get(this).deleteVideo(id);
    }

    private void addVideo(Intent data) {
        DatabasePatrol db = DatabasePatrol.get(this);

        ViolationItem violationItem
                = data.getParcelableExtra(VideoCaptureActivity.MOVIES_TO_SAVE);

        checkIfFileExist(violationItem.videoUrl);

        String thumbUrl = VideoThumbUtils.makeThumb(ThumbnailUtils.createVideoThumbnail(violationItem.videoUrl,
                MediaStore.Images.Thumbnails.MINI_KIND));

        VideoItem video = new VideoItem();
        video.setId(String.valueOf(System.currentTimeMillis()));
        video.setDate(violationItem.getViolationTime());
        video.setVideoPrevURL(violationItem.videoUrlPrev);
        video.setVideoURL(violationItem.videoUrl);
        video.setLatitude(violationItem.getLat());
        video.setLongitude(violationItem.getLon());
        video.setState(VideoItem.State.SAVING);
        video.setOwnerEmail(ProjectPreferencesManager.getUser(this).getEmail());
        video.setThumb(thumbUrl);
        video.setSourceType(VideoItem.SOURCE_TYPE_REGISTRATOR);

        db.addVideo(video);

        addVideoToUI(video.getId());
    }

    private void checkIfFileExist(String fileUri) {
        for (int existRetry = 0; existRetry < 3; existRetry++) {
            boolean isFileExist = new File(fileUri).exists();
            if (!isFileExist) {
                existRetry++;
                try {
                    Thread.sleep(3000);   //wait file saving in media store
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
    }

    private void tryToProcessVideo(final VideoItem video, DatabasePatrol db) {
        String id = video.getId();
        Log.d(TAG, "item: " + id);
        Log.d(TAG, "itemUrl: " + video.getVideoURL());
        File src = new File(video.getVideoURL());

        String videoPrevURL = video.getVideoPrevURL();
        if (videoPrevURL != null) {
            File src2 = new File(videoPrevURL);
            File result = new File(FilesUtils.getOutputInternalMediaFile(FilesUtils.MEDIA_TYPE_VIDEO).getAbsolutePath());
            ProcessVideoUtils.concatTwoVideos(src2, src, result);
            FilesUtils.removeFile(src.getAbsolutePath());
            FilesUtils.removeFile(src2.getAbsolutePath());
            src = result;
        }

        try {
            String trimResultUrl = ProcessVideoUtils.trimToLast20sec(src);
            if (trimResultUrl != null) {
                video.setVideoURL(trimResultUrl);
            }

            db.updateVideo(id, video.getVideoURL());
            db.updateVideo(video.getId(), VideoItem.State.READY_TO_SEND);

        } catch (Exception e) {
            e.printStackTrace();
            db.updateVideo(video.getId(), VideoItem.State.BROKEN_FILE);
        }
        updateUI(id, video.getVideoURL());
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
