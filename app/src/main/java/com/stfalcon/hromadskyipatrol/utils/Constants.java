package com.stfalcon.hromadskyipatrol.utils;

/**
 * Created by troy379 on 10.12.15.
 */
public final class Constants {
    private Constants() { throw new AssertionError(); }

    public static final int REQUEST_GPS_SETTINGS = 10;
    public static final int REQUEST_CAMERA = 11;
    public static final int REQUEST_VIDEO_PERMISSIONS = 12;

    public static final String EXTRAS_OWNER_EMAIL = "extras_owner_email";
    public static final String SERVER_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String VIDEO_DATE_MASK = "yyyyMMdd'T'hhmmss.SSS'Z'";
    public static final String EDIT_TEXT_MASK = "dd-MM-yyyy";
}
