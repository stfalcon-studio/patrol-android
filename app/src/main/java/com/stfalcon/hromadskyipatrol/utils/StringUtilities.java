package com.stfalcon.hromadskyipatrol.utils;

import android.content.Context;

import com.stfalcon.hromadskyipatrol.R;

import java.util.ArrayList;

/**
 * Created by troy379 on 06.11.15.
 */
public final class StringUtilities {
    private StringUtilities() {
        throw new AssertionError();
    }

    public static ArrayList<String> getOptions(Context context, boolean isCanLoad,
                                               boolean isCanDelete) {
        ArrayList<String> options = new ArrayList<>();

        options.add(context.getString(R.string.view_video));
        if (isCanLoad) options.add(context.getString(R.string.upload_video));
        if (isCanDelete) options.add(context.getString(R.string.delete_video));

        return options;
    }
}
