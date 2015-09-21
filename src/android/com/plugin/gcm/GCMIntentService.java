package com.plugin.gcm;

import com.google.android.gcm.GCMBaseIntentService;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import android.content.pm.PackageManager;

@SuppressLint("NewApi")
public class GCMIntentService extends GCMBaseIntentService {

    public static final int NOTIFICATION_ID = 237;

    private static String TAG = "PushPlugin-GCMIntentService";

    public static final String MESSAGE = "message";

    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    public void onRegistered(Context context, String regId) {
        Log.d(TAG, "onRegistered: " + regId);
        NotificationService.getInstance(context).onRegistered(regId);
    }

    @Override
    public void onUnregistered(Context context, String regId) {
        Log.d(TAG, "onUnregistered - regId: " + regId);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.d("PushNotification", "Received message: ");
        boolean isAppInForeground = NotificationService.getInstance(context).isForeground();

        Bundle extras = intent.getExtras();
        if (extras != null) {
            Log.d("PushNotification", "Got extras ");
            // If in background, create notification to display in notification center
            if (!isAppInForeground) {
                Log.d("PushNotification", "App is not foreground ");
                //forceMainActivityReload(context);
                if (extras.getString(MESSAGE) != null && extras.getString(MESSAGE).length() != 0) {
                    createNotification(context, extras);
                }
            }

            NotificationService.getInstance(context).onMessage(extras);
        }
    }

    private void forceMainActivityReload(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            //String packageName = getApplicationContext().getPackageName();
            String packageName = context.getPackageName();

            Log.d(TAG, "forceMainActivityReload() - packageName: " + packageName);

            Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
            //startActivity(launchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
            startActivity(launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); 
            //startActivity(launchIntent.setFlags(Intent.FLAG_FROM_BACKGROUND));                
        } catch (Exception e) {
            Log.e(TAG, "error : " + e);
        }

    }

    public void createNotification(Context context, Bundle extras) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        String appName = getAppName(this);

        Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra("pushBundle", extras);
        Log.d(TAG, "createNotification() - extras: ");
        Log.d(TAG, "extras "+extras);

        //[{event: "post", title: "post $1 by $2", msg: "", icon: "", sound: ""}]

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        int defaults = Notification.DEFAULT_ALL;

        if (extras.getString("defaults") != null) {
            try {
                defaults = Integer.parseInt(extras.getString("defaults"));
            } catch (NumberFormatException e) {
            }
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setDefaults(defaults)
                        .setSmallIcon(context.getApplicationInfo().icon)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(extras.getString("title"))
                        .setTicker(extras.getString("title"))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);

        String message = extras.getString("message");
        if (message != null) {
            mBuilder.setContentText(message);
        } else {
            mBuilder.setContentText("<missing message content>");
        }

        String msgcnt = extras.getString("msgcnt");
        if (msgcnt != null) {
            mBuilder.setNumber(Integer.parseInt(msgcnt));
        }

        int notId = NOTIFICATION_ID;

        try {
            notId = Integer.parseInt(extras.getString("notId"));
        } catch (NumberFormatException e) {
            Log.e(TAG,
                    "Number format exception - Error parsing Notification ID: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Number format exception - Error parsing Notification ID" + e.getMessage());
        }

        mNotificationManager.notify((String) appName, notId, mBuilder.build());

    }

    public static void cancelNotification(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel((String) getAppName(context), NOTIFICATION_ID);
    }

    private static String getAppName(Context context) {
        CharSequence appName =
                context
                        .getPackageManager()
                        .getApplicationLabel(context.getApplicationInfo());

        return (String) appName;
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.e(TAG, "onError - errorId: " + errorId);
    }

}
