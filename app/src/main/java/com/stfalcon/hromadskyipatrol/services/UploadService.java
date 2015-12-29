package com.stfalcon.hromadskyipatrol.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.stfalcon.hromadskyipatrol.BuildConfig;
import com.stfalcon.hromadskyipatrol.database.DatabasePatrol;
import com.stfalcon.hromadskyipatrol.models.UserItem;
import com.stfalcon.hromadskyipatrol.models.VideoAnswer;
import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.utils.Extras;
import com.stfalcon.hromadskyipatrol.utils.IntentUtilities;
import com.stfalcon.hromadskyipatrol.utils.MultipartUtility;
import com.stfalcon.hromadskyipatrol.utils.NetworkUtils;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

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
            if (intent.hasExtra(IntentUtilities.VIDEO_ID)) {
                DatabasePatrol db = DatabasePatrol.get(this);
                String videoId = intent.getStringExtra(IntentUtilities.VIDEO_ID);
                VideoItem videoItem = db.getVideo(videoId);

                if (videoItem != null) {
                    if (videoItem.getState() != VideoItem.State.UPLOADED) {
                        UserItem user = ProjectPreferencesManager.getUser(this);
                        updateItem(this, videoItem.getId(), VideoItem.State.SENDING, db);
                        VideoAnswer answer = uploadVideo(videoItem, user);
                        updateItem(this, answer.getId(), VideoItem.State.from(answer.getState()), db);
                    }
                }

            } else {
                tryToSendAllVideo();
            }
        }
    }

    public void tryToSendAllVideo() {
        DatabasePatrol db = DatabasePatrol.get(this);
        UserItem user = ProjectPreferencesManager.getUser(this);

        List<VideoItem> videoToSend = db.getVideos(VideoItem.State.READY_TO_SEND, user);

        for (VideoItem item : videoToSend) {
            updateItem(this, item.getId(), VideoItem.State.SENDING, db);

            VideoAnswer answer = uploadVideo(item, user);
            updateItem(this, answer.getId(), VideoItem.State.from(answer.getState()), db);
        }
    }


    public static void updateActivityUI(Context context, String id, VideoItem.State state) {
        Intent intent = new Intent(UPDATE_VIDEO_UI);
        intent.putExtra(Extras.ID, id);
        intent.putExtra(Extras.STATE, state.value());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void updateItem(Context context, String id, VideoItem.State state, DatabasePatrol db) {
        db.updateVideo(id, state);
        updateActivityUI(this, id, state);
    }

    public static VideoAnswer uploadVideo(VideoItem video, UserItem user) {
        return uploadVideo(video.getVideoURL(), String.valueOf(user.getId()),
                video.getId(), video.getDate(), video.getLatitude(), video.getLongitude());
    }

    public static VideoAnswer uploadVideo(String fileUrl, String userID, String videoID,
                                          long date, double latitude, double longitude) {
        String charset = "UTF-8";
        File file = new File(fileUrl);
        String requestURL = BuildConfig.BASE_URL + UPLOAD_URL.replace("{userID}", userID);
        VideoAnswer serverAnswer = new VideoAnswer(videoID, VideoItem.State.SENDING.value());
        try {
            MultipartUtility multipart = new MultipartUtility(requestURL, charset);

            multipart.addHeaderField("Content-Type", "multipart/form-data");
            multipart.addHeaderField("Accept", "application/json");
            multipart.addHeaderField("Accept-Encoding", "gzip, deflate");
            multipart.addFilePart("video", file);
            multipart.addFormField("latitude", String.valueOf(latitude));
            multipart.addFormField("longitude", String.valueOf(longitude));
            multipart.addFormField("date", String.valueOf(date));

            //logs
            List<String> response = multipart.finish();
            System.out.println("SERVER REPLIED:");
            for (String line : response) {
                System.out.println(line);
            }
            serverAnswer.setState(VideoItem.State.UPLOADED.value());

        } catch (FileNotFoundException ex) {
            System.err.println(ex);
            serverAnswer.setState(VideoItem.State.BROKEN_FILE.value());
        } catch (Exception ex) {
            System.err.println(ex);
            serverAnswer.setState(VideoItem.State.ERROR.value());
        }
        return serverAnswer;
    }
}
