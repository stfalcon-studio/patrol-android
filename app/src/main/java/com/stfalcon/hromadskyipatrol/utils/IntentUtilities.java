package com.stfalcon.hromadskyipatrol.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by troy379 on 06.11.15.
 */
public final class IntentUtilities {
    private IntentUtilities() { throw new AssertionError(); }

    public static String VIDEO_ID = "video_id";

    public static void openVideo(Context context, String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
        intent.setDataAndType(Uri.parse(path), "video/mp4");
        context.startActivity(intent);
    }
}
