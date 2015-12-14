package com.stfalcon.hromadskyipatrol.database;

/**
 * Created by troy379 on 11.12.15.
 */
public final class Const {
    private Const() { throw new AssertionError(); }

    public static final String DATABASE_NAME = "patrol_videos_db";
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_VIDEOS= "videos";

    public static final String KEY_ID = "_id";
    public static final String KEY_URL = "url";
    public static final String KEY_STATE = "state";
    public static final String KEY_LON = "lon";
    public static final String KEY_LAT = "lat";
    public static final String KEY_OWNER_EMAIL = "owner_email";

    public static final String CREATE_VIDEOS_TABLE = "CREATE TABLE " + TABLE_VIDEOS + "("
            + Const.KEY_ID + " TEXT PRIMARY KEY, "
            + Const.KEY_URL + " TEXT, "
            + Const.KEY_STATE + " INTEGER, "
            + Const.KEY_LON + " REAL, "
            + Const.KEY_LAT + " REAL, "
            + Const.KEY_OWNER_EMAIL + " TEXT"
            + ")";
}
