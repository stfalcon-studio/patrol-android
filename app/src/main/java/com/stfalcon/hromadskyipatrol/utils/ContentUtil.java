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
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by alex on 18.02.16.
 */
public class ContentUtil {
    private static final int CONTENT_PICKER = 15;
    private File targetFile;
    private Activity activity;
    private PickContentListener pickContentListener;
    private Handler handler;
    private int progressPersent = 0;

    public ContentUtil(Activity activity, PickContentListener pickContentListener) {
        this.activity = activity;
        this.pickContentListener = pickContentListener;
        handler = new Handler();
    }

    public void pickContent(Content content, File targetFile) {
        this.targetFile = targetFile;
        pick(content);
    }

    private void pick(Content content) {
        if (Build.VERSION.SDK_INT < 19) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType(content.toString());
            activity.startActivityForResult(photoPickerIntent, CONTENT_PICKER);
        } else {
            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            photoPickerIntent.setType(content.toString());
            photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
            if (photoPickerIntent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivityForResult(photoPickerIntent, CONTENT_PICKER);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONTENT_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                handleContentData(data);
            } else {
                cleanData();
                pickContentListener.onCanceled();
            }
        }
    }

    private void cleanData() {
        new File(targetFile.getAbsolutePath()).delete();
    }

    private void handleContentData(final Intent data) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Uri contentVideoUri = data.getData();

                    consumeProgress();

                    FileInputStream in = (FileInputStream) activity.getContentResolver().openInputStream(contentVideoUri);
                    FileOutputStream out = new FileOutputStream(targetFile);
                    FileChannel inChannel = in.getChannel();
                    FileChannel outChannel = out.getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);

                    in.close();
                    out.close();

                    ContentResolver contentResolver = activity.getContentResolver();
                    final String contentType = contentResolver.getType(contentVideoUri);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            pickContentListener.onContentLoaded(targetFile, contentType);
                        }
                    });
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            pickContentListener.onError(e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    private void consumeProgress() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressPersent++;
                pickContentListener.onLoadContentProgress(
                        progressPersent);
                if (progressPersent < 100) consumeProgress();
            }
        }, 500);
    }


    public interface PickContentListener {
        void onContentLoaded(File file, String contentType);

        void onLoadContentProgress(int loadPercent);

        void onError(String error);

        void onCanceled();
    }

    public enum Content {
        VIDEO("video/*"),
        IMAGE("image/*");

        private final String text;

        private Content(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
