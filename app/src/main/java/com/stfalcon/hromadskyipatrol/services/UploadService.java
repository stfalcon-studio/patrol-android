package com.stfalcon.hromadskyipatrol.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;

import com.stfalcon.hromadskyipatrol.BuildConfig;
import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.database.DatabasePatrol;
import com.stfalcon.hromadskyipatrol.models.UserItem;
import com.stfalcon.hromadskyipatrol.models.VideoAnswer;
import com.stfalcon.hromadskyipatrol.models.VideoItem;
import com.stfalcon.hromadskyipatrol.ui.activity.MainActivity;
import com.stfalcon.hromadskyipatrol.utils.Extras;
import com.stfalcon.hromadskyipatrol.utils.IntentUtilities;
import com.stfalcon.hromadskyipatrol.utils.MultipartUtility;
import com.stfalcon.hromadskyipatrol.utils.NetworkUtils;
import com.stfalcon.hromadskyipatrol.utils.ProjectPreferencesManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by alexandr on 21/08/15.
 */
public class UploadService extends IntentService {

    public static final String UPDATE_VIDEO_UI = "videoExceeded";
    private static final String UPLOAD_URL = "/api/{userID}/violation-video/create";
    private static final int NOTIFY_ID = 101;

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

            if (intent.hasExtra(Extras.ID) && intent.hasExtra(Extras.URL_VIDEO) && intent.hasExtra(Extras.DATE)){
                String date = intent.getStringExtra(Extras.DATE);
                String urlVideo = intent.getStringExtra(Extras.URL_VIDEO);
                String id = intent.getStringExtra(Extras.ID);
                uploadVideo(urlVideo, id, date);
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
        String violationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(date));

        try {
            MultipartUtility multipart = new MultipartUtility(requestURL, charset);
            multipart.addHeaderField("Content-Type", "multipart/form-data");
            multipart.addHeaderField("Accept", "application/json");
            multipart.addHeaderField("Accept-Encoding", "gzip, deflate");
            multipart.addFilePart("video", file);
            multipart.addFormField("latitude", String.valueOf(latitude));
            multipart.addFormField("longitude", String.valueOf(longitude));
            multipart.addFormField("date", violationDate);

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

    public boolean uploadVideo(String fileUrl, String userID, String date) {
        String charset = "UTF-8";
        File file = new File(fileUrl);
        String requestURL = BuildConfig.BASE_URL + UPLOAD_URL.replace("{userID}", userID);
        String violationDate = date;
        boolean isUploaded;

        try {
            MultipartUtility multipart = new MultipartUtility(requestURL, charset);
            multipart.addHeaderField("Content-Type", "multipart/form-data");
            multipart.addHeaderField("Accept", "application/json");
            multipart.addHeaderField("Accept-Encoding", "gzip, deflate");
            multipart.addFilePart("video", file);
            multipart.addFormField("latitude", String.valueOf(0)); //TODO Hardcode geodata
            multipart.addFormField("longitude", String.valueOf(0));
            multipart.addFormField("date", violationDate);

            //logs
            List<String> response = multipart.finish();
            System.out.println("SERVER REPLIED:");
            for (String line : response) {
                System.out.println(line);
            }

            isUploaded = true;

        } catch (FileNotFoundException ex) {
            System.err.println(ex);
            isUploaded = false;
            notification();
        } catch (Exception ex) {
            System.err.println(ex);
            isUploaded = false;
            notification();
        }
        return isUploaded;
    }

    private void notification(){
        Context context = getApplicationContext();

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.icon_broken)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_settings))
                .setTicker("Відправка відео")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle("Патруль")
                .setContentText("Відео не відправилось");

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, notification);
    }
}
