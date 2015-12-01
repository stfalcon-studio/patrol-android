package com.stfalcon.hromadskyipatrol.network;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.stfalcon.hromadskyipatrol.BuildConfig;
import com.stfalcon.hromadskyipatrol.models.VideoAnswer;
import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.models.UserItem;
import com.stfalcon.hromadskyipatrol.utils.IntentUtilities;
import com.stfalcon.hromadskyipatrol.utils.MultipartUtility;
import com.stfalcon.hromadskyipatrol.utils.NetworkUtils;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by alexandr on 21/08/15.
 */
public class UploadService extends IntentService {

    public static final String UPDATE_VIDEO_UI = "videoExceeded";
    private static final String UPLOAD_URL = "/api/{userID}/violation-video/create";

    public UploadService() {
        super(UploadService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (isConnectionAvailable()) {
            Bundle extras = intent.getExtras();
            String videoId = null;

            if (extras != null) {
                videoId = extras.getString(IntentUtilities.VIDEO_ID);
            }

            //init objects
            Realm realmDB = Realm.getInstance(this);
            UserItem user = realmDB.where(UserItem.class).findFirst();
            ArrayList<VideoAnswer> serverAnswersList = new ArrayList<>();
            RealmResults<VideoItem> videoList;

            if (videoId != null) {
                videoList = realmDB.where((VideoItem.class))
                        .contains("id", videoId)
                        .findAll();
            } else {
                //get all videos for upload
                videoList = realmDB.where((VideoItem.class))
                        .equalTo("state", VideoItem.STATE_IN_PROCESS)
                        .or()
                        .equalTo("state", VideoItem.STATE_ERROR)
                        .findAll();
            }

            //upload process
            for (VideoItem video : videoList) {
                if (video.getLatitude() != 0) {
                    VideoAnswer answer = uploadImage(video, user);
                    serverAnswersList.add(answer);
                    updateActivityUI(this, answer.getId(), answer.getState());
                } else {
                    serverAnswersList.add(new VideoAnswer(video.getId(), VideoItem.STATE_NO_GPS));
                    updateActivityUI(this, video.getId(), VideoItem.STATE_NO_GPS);
                }

            }

            //update DB
            if (!serverAnswersList.isEmpty()) {
                for (VideoAnswer answer : serverAnswersList) {
                    realmDB.beginTransaction();
                    VideoItem videoInBase = realmDB.where(VideoItem.class).contains("id", answer.getId()).findFirst();
                    videoInBase.setState(answer.getState());
                    realmDB.copyToRealmOrUpdate(videoInBase);
                    realmDB.commitTransaction();
                }
            }
        }
    }


    public static void updateActivityUI(Context context, String id, int state) {
        Intent intent = new Intent(UPDATE_VIDEO_UI);
        intent.putExtra("id", id);
        intent.putExtra("state", state);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static VideoAnswer uploadImage(VideoItem video, UserItem user) {
        return uploadImage(video.getVideoURL(), String.valueOf(user.getId()),
                video.getId(), video.getLatitude(), video.getLongitude());
    }

    public static VideoAnswer uploadImage(String fileUrl, String userID, String videoID, double latitude, double longitude) {
        String charset = "UTF-8";
        File file = new File(fileUrl);
        String requestURL = BuildConfig.BASE_URL + UPLOAD_URL.replace("{userID}", userID);
        VideoAnswer serverAnswer = new VideoAnswer(videoID, VideoItem.STATE_IN_PROCESS);
        try {
            MultipartUtility multipart = new MultipartUtility(requestURL, charset);

            multipart.addHeaderField("Content-Type", "multipart/form-data");
            multipart.addHeaderField("Accept", "application/json");
            multipart.addHeaderField("Accept-Encoding", "gzip, deflate");
            multipart.addFilePart("video", file);
            multipart.addFormField("latitude", String.valueOf(latitude));
            multipart.addFormField("longitude", String.valueOf(longitude));

            //logs
            List<String> response = multipart.finish();
            System.out.println("SERVER REPLIED:");
            for (String line : response) {
                System.out.println(line);
            }
            serverAnswer.setState(VideoItem.STATE_UPLOADED);

        } catch (IOException ex) {
            System.err.println(ex);
            serverAnswer.setState(VideoItem.STATE_ERROR);
        }
        return serverAnswer;
    }

    private boolean isConnectionAvailable() {
        int connectivityStatus = NetworkUtils.getConnectivityStatus(UploadService.this);
        boolean isCanUpload = true;

        if (ProjectPreferencesManager.getUploadWifiOnlyMode(UploadService.this))
            if (connectivityStatus != NetworkUtils.CONNECTION_WIFI) {
                isCanUpload = false;
            }

        return (isCanUpload && connectivityStatus != NetworkUtils.NOT_CONNECTED);
    }
}
