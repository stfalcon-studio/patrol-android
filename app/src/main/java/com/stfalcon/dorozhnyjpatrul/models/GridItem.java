package com.stfalcon.dorozhnyjpatrul.models;

/**
 * Created by alexandr on 17/08/15.
 */
public class GridItem {
    public static final int STATE_UPLOADED = 0;
    public static final int STATE_IN_PROCESS = 1;
    public static final int STATE_ERROR = 2;

    private int mThumbnail;
    private int state;


    public void setState(int state) {
        this.state = state;
    }

    public int getState() {

        return state;
    }

    public int getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.mThumbnail = thumbnail;
    }
}
