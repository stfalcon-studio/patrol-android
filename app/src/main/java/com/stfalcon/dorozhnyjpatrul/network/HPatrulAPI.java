package com.stfalcon.dorozhnyjpatrul.network;

import com.stfalcon.dorozhnyjpatrul.models.PhotoData;
import com.stfalcon.dorozhnyjpatrul.models.UserData;

import retrofit.http.EncodedPath;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Headers;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Created by alexandr on 20/08/15.
 */
public interface HPatrulAPI {

    @FormUrlEncoded
    @Headers({"Content-Type: application/x-www-form-urlencoded",
            "Accept: */*",
            "Accept-Encoding: gzip, deflate"})
    @POST("/api/register")
    UserData loginUser(@Field("email") String email);

    @Multipart
    @Headers({"Content-Type: multipart/form-data",
                "Accept: */*",
                "Accept-Encoding: gzip, deflate"})
    @POST("/api/{userID}/violation/create")
    PhotoData uploadImage(@Part("photo") TypedFile photo, @EncodedPath("userID") String userID);
}
