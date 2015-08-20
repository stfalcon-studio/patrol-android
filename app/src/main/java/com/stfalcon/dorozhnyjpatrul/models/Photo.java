package com.stfalcon.dorozhnyjpatrul.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexandr on 19/08/15.
 */
public class Photo extends RealmObject {
    public static final int STATE_UPLOADED = 0;
    public static final int STATE_IN_PROCESS = 1;
    public static final int STATE_ERROR = 2;

    @PrimaryKey
    private int id;
    private String photoURL;
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

    public String getPhotoURL() {
        return photoURL;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {

        return id;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}