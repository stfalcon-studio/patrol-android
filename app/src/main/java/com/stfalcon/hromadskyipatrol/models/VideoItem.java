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


/**
 * Created by alexandr on 19/08/15.
 */
public class VideoItem {

    public static final String SOURCE_TYPE_CAMERA = "camera";
    public static final String SOURCE_TYPE_REGISTRATOR = "recorder";
    public static final String SOURCE_TYPE_UPLOAD = "upload";

    public VideoItem() {
    }

    public VideoItem(String id, long date, String videoURL, int state, double longitude,
                     double latitude, String ownerEmail, String videoPrevURL, String thumb, String sourceType) {
        this(id, date, videoURL, State.from(state), longitude, latitude, ownerEmail, videoPrevURL, thumb, sourceType);
    }

    public VideoItem(String id, long date, String videoURL, State state, double longitude,
                     double latitude, String ownerEmail, String videoPrevURL, String thumb, String sourceType) {
        this.id = id;
        this.date = date ;
        this.videoURL = videoURL;
        this.thumb = thumb;
        this.state = state;
        this.longitude = longitude;
        this.latitude = latitude;
        this.ownerEmail = ownerEmail;
        this.videoPrevURL = videoPrevURL;
        this.sourceType = sourceType;
    }

    private String id;
    private long date;
    private String videoURL;
    private String thumb;
    private String videoPrevURL;
    private State state;
    private String sourceType;
    private double longitude;
    private double latitude;
    private String ownerEmail;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setState(State state) {
        this.state = state;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public State getState() {
        return state;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
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

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getVideoPrevURL() {
        return videoPrevURL;
    }

    public void setVideoPrevURL(String videoPrevURL) {
        this.videoPrevURL = videoPrevURL;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public enum State {
        UNDEFINED(-1), SAVING(1), READY_TO_SEND(2), SENDING(3), UPLOADED(4), ERROR(5), BROKEN_FILE(6);

        private final int value;

        private State(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static State from(int value) {
            for (State my : State.values())
                if (my.value == value) return my;
            return null;
        }
    }
}
