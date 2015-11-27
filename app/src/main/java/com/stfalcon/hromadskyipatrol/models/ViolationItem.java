package com.stfalcon.hromadskyipatrol.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by alex on 16.11.15.
 */
public class ViolationItem implements Parcelable {
    public String videoUrl;
    public long violationTime;

    public ViolationItem(long violationTime, String videoUrl) {
        this.videoUrl = videoUrl;
        this.violationTime = violationTime;
    }

    protected ViolationItem(Parcel in) {
        videoUrl = in.readString();
        violationTime = in.readLong();
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
        dest.writeLong(violationTime);
    }
}
