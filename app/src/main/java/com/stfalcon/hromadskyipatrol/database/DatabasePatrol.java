package com.stfalcon.hromadskyipatrol.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.stfalcon.hromadskyipatrol.models.VideoItem;

import java.util.ArrayList;

/**
 * Created by troy379 on 11.12.15.
 */
public class DatabasePatrol
        implements IDatabasePatrol {

    private static DatabasePatrol instance;
    private static DatabaseHelper helper;

    public static synchronized DatabasePatrol get(Context context) {
        if (instance == null) instance = new DatabasePatrol();
        if (helper == null) helper = new DatabaseHelper(context);
        return instance;
    }

    private DatabasePatrol() {
    }

    @Override
    public void addVideo(VideoItem item) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Const.KEY_ID, item.getId());
        values.put(Const.KEY_TUMB, item.getTumbURL());
        values.put(Const.KEY_URL, item.getVideoURL());
        values.put(Const.KEY_PREV_URL, item.getVideoPrevURL());
        values.put(Const.KEY_STATE, item.getState().value());
        values.put(Const.KEY_LON, item.getLongitude());
        values.put(Const.KEY_LAT, item.getLatitude());

        db.insert(Const.TABLE_VIDEOS, null, values);
        db.close();
    }

    @Override
    public ArrayList<VideoItem> getVideos() {
        SQLiteDatabase db = helper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + Const.TABLE_VIDEOS;

        return getVideos(db.rawQuery(selectQuery, null));
    }

    @Override
    public ArrayList<VideoItem> getVideos(VideoItem.State state) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + Const.TABLE_VIDEOS
                + " WHERE " + Const.KEY_STATE + " = '" + state.value() + "'";

        return getVideos(db.rawQuery(selectQuery, null));
    }

    @Override
    public VideoItem getVideo(VideoItem.State state) {
        return getVideoWhere(Const.KEY_STATE, state.value());
    }

    @Override
    public VideoItem getVideo(String id) {
        return getVideoWhere(Const.KEY_ID, id);
    }

    @Override
    public void updateVideo(String id, VideoItem.State state) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put(Const.KEY_STATE, state.value());
        db.update(Const.TABLE_VIDEOS, args, Const.KEY_ID + "=" + id, null);
        db.close();
    }

    @Override
    public void updateVideo(String id, String url) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put(Const.KEY_URL, url);
        db.update(Const.TABLE_VIDEOS, args, Const.KEY_ID + "=" + id, null);
        db.close();
    }

    @Override
    public void deleteVideo(String id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(Const.TABLE_VIDEOS, Const.KEY_ID + "=" + id, null);
        db.close();
    }


    /*      PRIVATE     */

    private VideoItem getVideoWhere(String state, int value) {
        return getVideoWhere(state, Integer.toString(value));
    }

    private VideoItem getVideoWhere(String state, String value) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + Const.TABLE_VIDEOS
                + " WHERE " + state + " = '" + value + "'";
        VideoItem item = null;

        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            item = new VideoItem(
                    cursor.getString(cursor.getColumnIndex(Const.KEY_ID)),
                    cursor.getString(cursor.getColumnIndex(Const.KEY_URL)),
                    cursor.getInt(cursor.getColumnIndex(Const.KEY_STATE)),
                    cursor.getDouble(cursor.getColumnIndex(Const.KEY_LON)),
                    cursor.getDouble(cursor.getColumnIndex(Const.KEY_LAT)),
                    cursor.getString(cursor.getColumnIndex(Const.KEY_PREV_URL)),
                    cursor.getString(cursor.getColumnIndex(Const.KEY_TUMB))
            );
        }

        cursor.close();
        return item;
    }

    private ArrayList<VideoItem> getVideos(Cursor cursor) {
        ArrayList<VideoItem> items = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do
                items.add(new VideoItem(
                        cursor.getString(cursor.getColumnIndex(Const.KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(Const.KEY_URL)),
                        cursor.getInt(cursor.getColumnIndex(Const.KEY_STATE)),
                        cursor.getDouble(cursor.getColumnIndex(Const.KEY_LON)),
                        cursor.getDouble(cursor.getColumnIndex(Const.KEY_LAT)),
                        cursor.getString(cursor.getColumnIndex(Const.KEY_PREV_URL)),
                        cursor.getString(cursor.getColumnIndex(Const.KEY_TUMB))

                ));
            while (cursor.moveToNext());
        }

        cursor.close();
        return items;
    }

    /*      HELPER      */

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private DatabaseHelper(Context context) {
            super(context, Const.DATABASE_NAME, null, Const.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(Const.CREATE_VIDEOS_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + Const.TABLE_VIDEOS);
            onCreate(db);
        }
    }
}
