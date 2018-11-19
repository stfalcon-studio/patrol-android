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

package com.stfalcon.hromadskyipatrol.database;

/**
 * Created by troy379 on 11.12.15.
 */
final class Const {
    private Const() { throw new AssertionError(); }

    public static final String DATABASE_NAME = "patrol_videos_db";
    public static final int DATABASE_VERSION = 4;

    public static final String TABLE_VIDEOS= "videos";

    public static final String KEY_ID = "_id";
    public static final String KEY_DATE = "_date";
    public static final String KEY_THUMB = "thumb";
    public static final String KEY_URL = "url";
    public static final String KEY_PREV_URL = "prev_url";
    public static final String KEY_STATE = "state";
    public static final String KEY_LON = "lon";
    public static final String KEY_LAT = "lat";
    public static final String KEY_OWNER_EMAIL = "owner_email";
    public static final String KEY_SOURCE_TYPE = "source";

    public static final String CREATE_VIDEOS_TABLE = "CREATE TABLE " + TABLE_VIDEOS + "("
            + Const.KEY_ID + " TEXT PRIMARY KEY, "
            + Const.KEY_DATE + " REAL, "
            + Const.KEY_THUMB + " TEXT, "
            + Const.KEY_URL + " TEXT, "
            + Const.KEY_PREV_URL + " TEXT, "
            + Const.KEY_STATE + " INTEGER, "
            + Const.KEY_LON + " REAL, "
            + Const.KEY_LAT + " REAL, "
            + Const.KEY_OWNER_EMAIL + " TEXT, "
            + Const.KEY_SOURCE_TYPE + " TEXT"
            + ")";
}
