package com.stfalcon.hromadskyipatrul.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

/**
 * Created by TROY!379 on 21.08.15.
 */
public final class SpannableTextHelper {
    private SpannableTextHelper() { throw new AssertionError(); }

    public static Spannable getTextWithImage(String text, Resources resources, int resourceIcon, int position) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        Bitmap icon = BitmapFactory.decodeResource(resources, resourceIcon);
        ssb.setSpan(new ImageSpan(icon), position, position + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return ssb;
    }
}
