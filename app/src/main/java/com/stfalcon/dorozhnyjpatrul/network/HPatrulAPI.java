package com.stfalcon.dorozhnyjpatrul.network;

import com.stfalcon.dorozhnyjpatrul.models.PhotoData;
import com.stfalcon.dorozhnyjpatrul.models.UserData;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedFile;

/**
 * Created by alexandr on 20/08/15.
 */
public interface HPatrulAPI {

    @FormUrlEncoded
    @POST("/api/register")
    UserData loginUser(@Field("email") String email);

    @Multipart
    @POST("/api/{userID}/violation/create")
    PhotoData uploadImage(@Path("user") String userID, @Part("photo") TypedFile photo);
}
