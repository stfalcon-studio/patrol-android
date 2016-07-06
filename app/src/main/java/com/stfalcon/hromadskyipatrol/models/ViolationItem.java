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
