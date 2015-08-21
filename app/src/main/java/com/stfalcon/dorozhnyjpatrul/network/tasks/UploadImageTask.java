package com.stfalcon.dorozhnyjpatrul.network.tasks;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.stfalcon.dorozhnyjpatrul.models.Photo;
import com.stfalcon.dorozhnyjpatrul.models.PhotoAnswer;
import com.stfalcon.dorozhnyjpatrul.network.HPatrulAPI;
import com.stfalcon.dorozhnyjpatrul.utils.MultipartUtility;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by alexandr on 18/08/15.
 */
public class UploadImageTask extends RetrofitSpiceRequest<PhotoAnswer, HPatrulAPI> {

    private final String photoID;
    private final double latitude;
    private final double longitude;
    private String fileUrl;
    private String id;

    public UploadImageTask(int userID, String photoID, Photo photo) {
        super(PhotoAnswer.class, HPatrulAPI.class);
        this.fileUrl = photo.getPhotoURL();
        this.latitude = photo.getLatitude();
        this.longitude = photo.getLongitude();
        this.id = String.valueOf(userID);
        this.photoID = photoID;
    }

    @Override
    public PhotoAnswer loadDataFromNetwork() {
        /*TypedFile file = new TypedFile("image/jpeg", new File(fileUrl));
        PhotoAnswer photoData = getService().uploadImage(file, id, 27.0400, 49.0300);
        photoData.setId(photoID);*/

        String charset = "UTF-8";
        File image = new File(fileUrl);
        String requestURL = "http://192.168.0.29/app_dev.php/api/{userID}/violation/create".replace("{userID}", id);
        PhotoAnswer photoData = new PhotoAnswer();
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
        photoData.setId(photoID);
        return photoData;
    }
}
