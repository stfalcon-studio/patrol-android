package com.stfalcon.dorozhnyjpatrul.network.tasks;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.stfalcon.dorozhnyjpatrul.models.PhotoData;
import com.stfalcon.dorozhnyjpatrul.network.HPatrulAPI;

import java.io.File;

import retrofit.mime.TypedFile;

/**
 * Created by alexandr on 18/08/15.
 */
public class UploadImageTask extends RetrofitSpiceRequest<PhotoData, HPatrulAPI> {

    private final int photoID;
    private String fileUrl;
    private String id;

    public UploadImageTask(int userID, int photoID, String fileUrl) {
        super(PhotoData.class, HPatrulAPI.class);
        this.fileUrl = fileUrl;
        this.id = String.valueOf(userID);
        this.photoID = photoID;
    }

    @Override
    public PhotoData loadDataFromNetwork() {
        TypedFile file = new TypedFile("file", new File(fileUrl));
        PhotoData photoData = getService().uploadImage(id, file);
        photoData.setId(photoID);
        return photoData;
    }
}
