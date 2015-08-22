package com.stfalcon.hromadskyipatrol.network;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.stfalcon.hromadskyipatrol.models.PhotoAnswer;
import com.stfalcon.hromadskyipatrol.models.PhotoItem;
import com.stfalcon.hromadskyipatrol.models.UserItem;
import com.stfalcon.hromadskyipatrol.utils.MultipartUtility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import io.realm.Realm;

/**
 * Created by alexandr on 21/08/15.
 */
public class UploadService extends IntentService {

    public static final String UPDATE_PHOTO_UI = "photoExceeded";
    private static final String UPLOAD_URL = "/api/{userID}/violation/create";

    public UploadService() {
        super(UploadService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //init objects
        Realm realmDB = Realm.getInstance(this);
        UserItem user = realmDB.where(UserItem.class).findFirst();
        ArrayList<PhotoAnswer> serverAnswersList = new ArrayList<>();
        ArrayList<PhotoItem> photoList = new ArrayList<>();

        //get all photos for upload
        photoList.addAll(realmDB.where((PhotoItem.class))
                .equalTo("state", PhotoItem.STATE_IN_PROCESS).findAll());

        photoList.addAll(realmDB.where((PhotoItem.class))
                .equalTo("state", PhotoItem.STATE_ERROR).findAll());

        //upload process
        for (PhotoItem photo : photoList) {
            if (photo.getLatitude() != 0) {
                PhotoAnswer answer = uploadImage(photo.getPhotoURL(), String.valueOf(user.getId()),
                        photo.getId(), photo.getLatitude(), photo.getLongitude());
                serverAnswersList.add(answer);
                updateActivityUI(answer.getId(), answer.getState());
            } else {
                serverAnswersList.add(new PhotoAnswer(photo.getId(), PhotoItem.STATE_NO_GPS));
                updateActivityUI(photo.getId(), PhotoItem.STATE_NO_GPS);
            }

        }

        //update DB
        if (!serverAnswersList.isEmpty()) {
            for (PhotoAnswer answer : serverAnswersList) {
                realmDB.beginTransaction();
                PhotoItem photoInBase = realmDB.where(PhotoItem.class).contains("id", answer.getId()).findFirst();
                photoInBase.setState(answer.getState());
                realmDB.copyToRealmOrUpdate(photoInBase);
                realmDB.commitTransaction();
            }
        }
    }


    private void updateActivityUI(String id, int state) {
        Intent intent = new Intent(UPDATE_PHOTO_UI);
        intent.putExtra("id", id);
        intent.putExtra("state", state);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    public static PhotoAnswer uploadImage(String fileUrl, String userID, String photoID, double latitude, double longitude) {
        String charset = "UTF-8";
        File image = new File(fileUrl);
        String requestURL = PatrolSpiceService.BASE_URL + UPLOAD_URL.replace("{userID}", userID);
        PhotoAnswer serverAnswer = new PhotoAnswer(photoID, PhotoItem.STATE_IN_PROCESS);
        try {
            MultipartUtility multipart = new MultipartUtility(requestURL, charset);

            multipart.addHeaderField("Content-Type", "multipart/form-data");
            multipart.addHeaderField("Accept", "application/json");
            multipart.addHeaderField("Accept-Encoding", "gzip, deflate");
            multipart.addFilePart("photo", image);
            multipart.addFormField("latitude", String.valueOf(latitude));
            multipart.addFormField("longitude", String.valueOf(longitude));
            serverAnswer.setState(PhotoItem.STATE_UPLOADED);

        } catch (IOException ex) {
            System.err.println(ex);
            serverAnswer.setState(PhotoItem.STATE_ERROR);
        }
        return serverAnswer;
    }
}
