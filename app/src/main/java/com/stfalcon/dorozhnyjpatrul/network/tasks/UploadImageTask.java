package com.stfalcon.dorozhnyjpatrul.network.tasks;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.stfalcon.dorozhnyjpatrul.models.Photo;
import com.stfalcon.dorozhnyjpatrul.models.PhotoAnswer;
import com.stfalcon.dorozhnyjpatrul.network.HPatrulAPI;
import com.stfalcon.dorozhnyjpatrul.network.UploadService;

/**
 * Created by alexandr on 18/08/15.
 */
public class UploadImageTask extends RetrofitSpiceRequest<PhotoAnswer, HPatrulAPI> {

    private final String photoID;
    private final double latitude;
    private final double longitude;
    private String fileUrl;
    private String userID;

    public UploadImageTask(int userID, String photoID, Photo photo) {
        super(PhotoAnswer.class, HPatrulAPI.class);
        this.fileUrl = photo.getPhotoURL();
        this.latitude = photo.getLatitude();
        this.longitude = photo.getLongitude();
        this.userID = String.valueOf(userID);
        this.photoID = photoID;
    }

    @Override
    public PhotoAnswer loadDataFromNetwork() {
        /*TypedFile file = new TypedFile("image/jpeg", new File(fileUrl));
        PhotoAnswer photoData = getService().uploadImage(file, userID, 27.0400, 49.0300);
        photoData.setId(photoID);*/
        return UploadService.uploadImage(fileUrl, userID, photoID, latitude, longitude);
    }
}
