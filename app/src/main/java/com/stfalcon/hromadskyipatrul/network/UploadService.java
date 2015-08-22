package com.stfalcon.hromadskyipatrul.network;

import android.app.IntentService;
import android.content.Intent;

import com.stfalcon.hromadskyipatrul.models.Photo;
import com.stfalcon.hromadskyipatrul.models.PhotoAnswer;
import com.stfalcon.hromadskyipatrul.models.UserData;
import com.stfalcon.hromadskyipatrul.utils.MultipartUtility;

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

    public UploadService() {
        super(UploadService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Realm realm = Realm.getInstance(this);
        UserData userData = realm.where(UserData.class).findFirst();
        ArrayList<PhotoAnswer> answers = new ArrayList<>();
        ArrayList<Photo> realmResults = new ArrayList<>();
        realmResults.addAll(realm.where((Photo.class))
                .equalTo("state", Photo.STATE_IN_PROCESS).findAll());
        realmResults.addAll(realm.where((Photo.class))
                .equalTo("state", Photo.STATE_ERROR).findAll());
        for (Photo photo : realmResults) {
            if (photo.getLatitude() != 0) {
                answers.add(uploadImage(photo.getPhotoURL(), String.valueOf(userData.getId()),
                        photo.getId(), photo.getLatitude(), photo.getLongitude()));
            } else {
                answers.add(new PhotoAnswer(photo.getId(), Photo.STATE_NO_GPS));
            }
        }
        if (!answers.isEmpty()) {
            for (PhotoAnswer answer : answers) {
                realm.beginTransaction();
                Photo photoInBase = realm.where(Photo.class).contains("id", String.valueOf(answer.getId())).findFirst();
                photoInBase.setState(answer.getState());
                realm.copyToRealmOrUpdate(photoInBase);
                realm.commitTransaction();
            }
        }
    }


    public static PhotoAnswer uploadImage(String fileUrl, String userID, String photoID, double latitude, double longitude) {
        String charset = "UTF-8";
        File image = new File(fileUrl);
        String requestURL = "http://192.168.0.29/app_dev.php/api/{userID}/violation/create".replace("{userID}", userID);
        PhotoAnswer photoData = new PhotoAnswer(photoID, Photo.STATE_IN_PROCESS);
        try {
            MultipartUtility multipart = new MultipartUtility(requestURL, charset);

            multipart.addHeaderField("Content-Type", "multipart/form-data");
            multipart.addHeaderField("Accept", "application/json");
            multipart.addHeaderField("Accept-Encoding", "gzip, deflate");
            multipart.addFilePart("photo", image);
            multipart.addFormField("latitude", String.valueOf(latitude));
            multipart.addFormField("longitude", String.valueOf(longitude));

            List<String> response = multipart.finish();

            System.out.println("SERVER REPLIED:");

            photoData.setState(Photo.STATE_UPLOADED);

            for (String line : response) {
                System.out.println(line);
            }
        } catch (IOException ex) {
            System.err.println(ex);
            photoData.setState(Photo.STATE_ERROR);
        }
        return photoData;
    }
}
