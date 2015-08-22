package com.stfalcon.hromadskyipatrul.network;

import com.stfalcon.hromadskyipatrul.models.LoginAnswer;
import com.stfalcon.hromadskyipatrul.models.PhotoAnswer;

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
            "Accept: application/json",
            "Accept-Encoding: gzip, deflate"})
    @POST("/api/register")
    LoginAnswer loginUser(@Field("email") String email);


    @Multipart
    @Headers({"Content-Type: multipart/form-data",
            "Accept: application/json",
            "Accept-Encoding: gzip, deflate"})
    @POST("/api/{userID}/violation/create")
    PhotoAnswer uploadImage(@Part("photo") TypedFile photo,
                            @EncodedPath("userID") String userID,
                            @Part("latitude") double latitude,
                            @Part("longitude") double longitude);
}
