package com.stfalcon.hromadskyipatrol.utils;

import android.content.Context;

import com.stfalcon.hromadskyipatrol.R;

import java.util.ArrayList;

/**
 * Created by troy379 on 06.11.15.
 */
public final class StringUtilities {
    private StringUtilities() { throw new AssertionError(); }

    public static ArrayList<String> getOptions(Context context, boolean isLoaded) {
        ArrayList<String> options = new ArrayList<>();

        options.add(context.getString(R.string.view_video));
        if (!isLoaded) options.add(context.getString(R.string.upload_video));
        options.add(context.getString(R.string.delete_video));

        return options;
    }
}
