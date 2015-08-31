package com.stfalcon.hromadskyipatrol.models;

/**
 * Created by alexandr on 20/08/15.
 */
public class PhotoAnswer {
    private String id;
    private int state;
    private double latitude;
    private double longitude;

    public PhotoAnswer(String id, int state) {
        this.id = id;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
