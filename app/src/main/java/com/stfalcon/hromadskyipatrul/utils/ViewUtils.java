package com.stfalcon.hromadskyipatrul.utils;

import android.view.View;

/**
 * Created by TROY!379 on 21.08.15.
 */
public final class ViewUtils {
    private ViewUtils() { throw new AssertionError(); }

    public static int booleanToVisibility(Boolean state) {
        if (state) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }
}
