package com.stfalcon.hromadskyipatrol.utils;

import android.graphics.Bitmap;

/**
 * Created by alex on 12.12.15.
 */
public class VideoThumbUtils {

    public static String makeThumb(Bitmap bitmap){
        return FilesUtils.storeThumb(bitmap);
    }

    public static void removeThumb(String url){
        FilesUtils.removeFile(url);
    }
}
