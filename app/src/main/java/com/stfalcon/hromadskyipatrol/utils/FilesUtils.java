package com.stfalcon.hromadskyipatrol.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
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
        File pictureFile = getOutputInternalThumbFile();
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
        }
        return null;
    }


    public static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "camera");
        createMediaStorageDir(mediaStorageDir);
        return createFile(type, mediaStorageDir);
    }


    public static File getOutputInternalMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), APP_CONTENT_PATH);
        createMediaStorageDir(mediaStorageDir);
        return createFile(type, mediaStorageDir);
    }

    public static File getOutputInternalThumbFile() {
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
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".png");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        }
        return mediaFile;
    }


    public static String saveToInternalStorage(int type, Uri tempUri) {
        InputStream in = null;
        OutputStream out = null;

        File sourceExternalImageFile = new File(tempUri.getPath());
        File destinationInternalImageFile = new File(getOutputInternalMediaFile(type).getPath());

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
        new File(url).delete();
    }
}
