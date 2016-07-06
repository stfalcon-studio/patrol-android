/*
 * Copyright (c) 2015 - 2016. Stepan Tanasiychuk
 *
 *     This file is part of Gromadskyi Patrul is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Found ation, version 3 of the License, or any later version.
 *
 *     If you would like to use any part of this project for commercial purposes, please contact us
 *     for negotiating licensing terms and getting permission for commercial use.
 *     Our email address: info@stfalcon.com
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
