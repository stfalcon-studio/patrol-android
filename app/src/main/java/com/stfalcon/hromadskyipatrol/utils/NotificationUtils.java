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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;

import com.stfalcon.hromadskyipatrol.R;
import com.stfalcon.hromadskyipatrol.ui.activity.MainActivity;

/**
 * Created by artem on 10.02.16.
 */
public class NotificationUtils {

    private static final int NOTIFY_ID = 101;


    public static void notificationFailLoad (Context context){
        notification(context, context.getString(R.string.notification_fail_upload_title),
                context.getString(R.string.notification_fail_upload_body),
                context.getString(R.string.notification_fail_upload_textTicker),
                R.drawable.ic_launcher, R.drawable.icon_broken);
    }

    public static void notificationStartLoad (Context context){
        notification(context, context.getString(R.string.notification_start_upload_title),
                context.getString(R.string.notification_start_upload_body),
                context.getString(R.string.notification_start_upload_textTicker),
                R.drawable.ic_launcher, android.R.drawable.stat_sys_upload);
    }

    public static void notificationSuccesLoad (Context context){
        notification(context, context.getString(R.string.notification_end_upload_title),
                context.getString(R.string.notification_end_upload_body),
                context.getString(R.string.notification_end_upload_textTicker),
                R.drawable.ic_launcher, R.drawable.icon_done);
    }

    private static void notification(Context context, String title, String body, String textTicker, int largeIcon, int smallIcon){

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(smallIcon)
                .setLargeIcon(BitmapFactory.decodeResource(res, largeIcon))
                .setTicker(textTicker)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(body);

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, notification);
    }


}
