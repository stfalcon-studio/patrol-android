package com.stfalcon.hromadskyipatrol.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by alex on 16.11.15.
 */
public class ViolationItem implements Parcelable {
    public String videoUrl;
    public String videoUrlPrev;
    public long violationTime;
    public double lat;
    public double lon;

    public ViolationItem(long violationTime, String videoUrl) {
        this.videoUrl = videoUrl;
        this.violationTime = violationTime;
    }

    public ViolationItem(long violationTime, String videoUrl, String videoUrlPrev) {
        this.videoUrl = videoUrl;
        this.videoUrlPrev = videoUrlPrev;
        this.violationTime = violationTime;
    }

    public long getViolationTime() {
        return violationTime;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    protected ViolationItem(Parcel in) {
        videoUrl = in.readString();
        videoUrlPrev = in.readString();
        violationTime = in.readLong();
        lat = in.readDouble();
        lon = in.readDouble();
    }

    public static final Creator<ViolationItem> CREATOR = new Creator<ViolationItem>() {
        @Override
        public ViolationItem createFromParcel(Parcel in) {
            return new ViolationItem(in);
        }

        @Override
        public ViolationItem[] newArray(int size) {
            return new ViolationItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoUrl);
        dest.writeString(videoUrlPrev);
        dest.writeLong(violationTime);
        dest.writeDouble(lat);
        dest.writeDouble(lon);
    }
}
