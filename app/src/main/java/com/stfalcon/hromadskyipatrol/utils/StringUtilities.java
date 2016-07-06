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
