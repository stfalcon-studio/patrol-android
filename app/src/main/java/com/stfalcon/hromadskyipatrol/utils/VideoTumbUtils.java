package com.stfalcon.hromadskyipatrol.utils;

import android.graphics.Bitmap;

/**
 * Created by alex on 12.12.15.
 */
public class VideoTumbUtils {

    public static String makeTumb(Bitmap bitmap){
        return FilesUtils.storeTumb(bitmap);
    }

    public static void removeTumb(String url){
        FilesUtils.removeFile(url);
    }
}
