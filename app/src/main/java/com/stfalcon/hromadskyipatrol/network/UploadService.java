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
        if (NetworkUtils.isConnectionAvailable(this)) {
            Bundle extras = intent.getExtras();
            String videoId = null;
            Realm realmDB = Realm.getInstance(this);
            if (extras != null) {
                videoId = extras.getString(IntentUtilities.VIDEO_ID);
            }


            if (videoId != null) {
                VideoItem videoItem = realmDB.where((VideoItem.class))
                        .contains("id", videoId)
                        .findFirst();
                UserItem user = realmDB.where(UserItem.class).findFirst();
                realmDB.beginTransaction();
                videoItem.setState(VideoItem.STATE_SENDING);
                realmDB.commitTransaction();
                updateActivityUI(this);
                VideoAnswer answer = uploadImage(videoItem, user);
                realmDB.beginTransaction();
                VideoItem videoInBase = realmDB.where(VideoItem.class).contains("id", answer.getId()).findFirst();
                videoInBase.setState(answer.getState());
                realmDB.commitTransaction();

                updateActivityUI(this);

            } else {
                tryToSendVideo();
            }
        }
    }

    public void tryToSendVideo() {
        //init objects
        Realm realmDB = Realm.getInstance(this);
        UserItem user = realmDB.where(UserItem.class).findFirst();

        VideoItem videoItem = realmDB.where((VideoItem.class))
                .equalTo("state", VideoItem.STATE_READY_TO_SEND)
                .or()
                .equalTo("state", VideoItem.STATE_ERROR)
                .findFirst();

        if (videoItem == null) {
            return;
        }

        realmDB.beginTransaction();
        videoItem.setState(VideoItem.STATE_SENDING);
        realmDB.commitTransaction();
        updateActivityUI(this);

        VideoAnswer answer = uploadImage(videoItem, user);
        realmDB.beginTransaction();
        VideoItem videoInBase = realmDB.where(VideoItem.class).contains("id", answer.getId()).findFirst();
        videoInBase.setState(answer.getState());
        realmDB.commitTransaction();
        updateActivityUI(this);

        tryToSendVideo();
    }


    public static void updateActivityUI(Context context) {
        Intent intent = new Intent(UPDATE_VIDEO_UI);
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
        VideoAnswer serverAnswer = new VideoAnswer(videoID, VideoItem.STATE_SENDING);
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
}
