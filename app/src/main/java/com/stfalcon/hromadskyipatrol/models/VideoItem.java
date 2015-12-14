package com.stfalcon.hromadskyipatrol.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexandr on 19/08/15.
 */
public class VideoItem extends RealmObject {
    public static final int STATE_UPLOADED = 1;
    public static final int STATE_SENDING = 0;
    public static final int STATE_ERROR = 2;
    public static final int STATE_READY_TO_SEND = 3;
    public static final int STATE_SAVING = 4;

    @PrimaryKey
    private String id;
    private String videoURL;
    private String videoPrevURL;
    private int state;
    private double longitude;
    private double latitude;

    public void setState(int state) {
        this.state = state;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getState() {
        return state;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {

        return id;
    }

    public void setVideoURL(String photoURL) {
        this.videoURL = photoURL;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getVideoPrevURL() {
        return videoPrevURL;
    }

    public void setVideoPrevURL(String videoPrevURL) {
        this.videoPrevURL = videoPrevURL;
    }
}
