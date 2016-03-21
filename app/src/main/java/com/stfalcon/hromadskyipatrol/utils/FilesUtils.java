package com.stfalcon.hromadskyipatrol.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alexandr on 18/08/15.
 */
public class FilesUtils {
    private static String TAG = FilesUtils.class.getSimpleName();

    private static String APP_CONTENT_PATH = "DPatrul";
    private static String VIDEO_THUMB_PATH = APP_CONTENT_PATH + "/thumbs";

    public static final int MEDIA_TYPE_IMAGE = 0;
    public static final int MEDIA_TYPE_VIDEO = 1;


    public static String storeThumb(Bitmap image) {
        File pictureFile = getOutputExternalThumbFile();
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
            return pictureFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        } catch (Exception ignore) {
        }
        return null;
    }


    public static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "camera");
        createMediaStorageDir(mediaStorageDir);
        return createFile(type, mediaStorageDir);
    }


    public static File getOutputExternalMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), APP_CONTENT_PATH);
        createMediaStorageDir(mediaStorageDir);
        return createFile(type, mediaStorageDir);
    }

    public static File getOutputExternalThumbFile() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), VIDEO_THUMB_PATH);
        createMediaStorageDir(mediaStorageDir);
        return createFile(MEDIA_TYPE_IMAGE, mediaStorageDir);
    }


    private static void createMediaStorageDir(File mediaStorageDir) {
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs(); // Used to be 'mediaStorage.mkdirs();'
        }
    } // Was flipped the other way


    private static File createFile(int type, File mediaStorageDir) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = null;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getAbsolutePath() + File.separator +
                    "IMG_" + timeStamp + ".png");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getAbsolutePath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        }
        return mediaFile;
    }


    public static String saveToInternalStorage(int type, Uri tempUri) {
        InputStream in = null;
        OutputStream out = null;

        File sourceExternalImageFile = new File(tempUri.getPath());
        File destinationInternalImageFile = new File(getOutputExternalMediaFile(type).getPath());

        try {
            destinationInternalImageFile.createNewFile();

            in = new FileInputStream(sourceExternalImageFile);
            out = new FileOutputStream(destinationInternalImageFile);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
            //Handle error
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    in.close();
                }
            } catch (IOException e) {
                // Eh
            }
        }
        return destinationInternalImageFile.getAbsolutePath();
    }

    public static void removeFile(String url) {
        try {
            new File(url).delete();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }


    public static String getRealPathFromURI(Context context, Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static boolean deleteFile(String filePath) {
        return new File(filePath).delete();
    }


    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }
}
