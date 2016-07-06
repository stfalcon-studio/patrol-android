/*
 * Copyright (c) 2015 - 2016. Stepan Tanasiychuk
 *
 *     This file is part of Gromadskyi Patrul is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Found ation, version 3 of the License, or any later version.
 *
 *     If you would like to use any part of this project for commercial purposes, please contact us
 *     for negotiating licensing terms and getting permission for commercial use.
 *     Our email address: info@stfalcon.com
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stfalcon.hromadskyipatrol.network;

import com.stfalcon.hromadskyipatrol.models.LoginAnswer;
import com.stfalcon.hromadskyipatrol.models.VideoAnswer;

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
public interface PatrulatrulAPI {

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
    VideoAnswer uploadImage(@Part("photo") TypedFile photo,
                            @EncodedPath("userID") String userID,
                            @Part("latitude") double latitude,
                            @Part("longitude") double longitude);
}
