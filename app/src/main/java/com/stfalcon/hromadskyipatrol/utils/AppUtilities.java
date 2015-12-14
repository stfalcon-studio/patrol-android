package com.stfalcon.hromadskyipatrol.utils;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.widget.Toast;

/**
 * Created by troy379 on 10.12.15.
 */
public final class AppUtilities {
    private AppUtilities() { throw new AssertionError(); }

    public static void showToast(Activity context, @StringRes int resId, boolean isLong) {
        showToast(context, context.getString(resId), isLong);
    }

    public static void showToast(Activity context, String message, boolean isLong) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(
                    context,
                    message,
                    isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT
            ).show();
        }
    }
}
