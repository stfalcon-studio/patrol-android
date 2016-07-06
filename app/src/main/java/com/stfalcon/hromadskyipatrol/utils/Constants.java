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

/**
 * Created by troy379 on 10.12.15.
 */
public final class Constants {
    private Constants() { throw new AssertionError(); }

    public static final int REQUEST_GPS_SETTINGS = 10;
    public static final int REQUEST_CAMERA = 11;
    public static final int REQUEST_VIDEO_PERMISSIONS = 12;

    public static final String EXTRAS_OWNER_EMAIL = "extras_owner_email";
    public static final String SERVER_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String VIDEO_META_DATE_MASK = "yyyyMMdd'T'hhmmss.SSS'Z'";
    public static final String EDIT_TEXT_MASK = "dd.MM.yyyy";
}
