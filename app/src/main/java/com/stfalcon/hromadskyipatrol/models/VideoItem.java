package com.stfalcon.hromadskyipatrol.models;


/**
 * Created by alexandr on 19/08/15.
 */
public class VideoItem {

    public VideoItem() {
    }

    public VideoItem(String id, String videoURL, int state, double longitude, double latitude, String videoPrevURL) {
        this(id, videoURL, State.from(state), longitude, latitude, videoPrevURL);
    }

    public VideoItem(String id, String videoURL, State state, double longitude, double latitude, String videoPrevURL) {
        this.id = id;
        this.videoURL = videoURL;
        this.state = state;
        this.longitude = longitude;
        this.latitude = latitude;
        this.videoPrevURL = videoPrevURL;
    }

    //    @PrimaryKey
    private String id;
    private String videoURL;
    private String videoPrevURL;
    private State state;
    private double longitude;
    private double latitude;

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

    public enum State {
        UNDEFINED(-1), SAVING(1), READY_TO_SEND(2), SENDING(3), UPLOADED(4), ERROR(5);

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
