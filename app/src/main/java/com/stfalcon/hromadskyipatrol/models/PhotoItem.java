package com.stfalcon.hromadskyipatrol.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexandr on 19/08/15.
 */
public class PhotoItem extends RealmObject {
    public static final int STATE_UPLOADED = 1;
    public static final int STATE_IN_PROCESS = 0;
    public static final int STATE_ERROR = 2;
    public static final int STATE_NO_GPS = 3;

    @PrimaryKey
    private String id;
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

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {

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
