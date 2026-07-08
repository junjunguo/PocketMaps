package com.junjunguo.pocketmaps.activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.junjunguo.pocketmaps.R;

public class LocationForegroundService extends Service
{
    private static final String CHANNEL_ID = "pocketmaps_location_channel";
    private static final int NOTIFICATION_ID = 1001;

    public static void start(Context context)
    {
        Intent intent = new Intent(context, LocationForegroundService.class);
        intent.setAction(ACTION_START);
        if (Build.VERSION.SDK_INT >= 26)
        {
            context.startForegroundService(intent);
        }
        else
        {
            context.startService(intent);
        }
    }

    public static void stop(Context context)
    {
        Intent intent = new Intent(context, LocationForegroundService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }

    private static final String ACTION_START = "com.junjunguo.pocketmaps.START_LOCATION";
    private static final String ACTION_STOP = "com.junjunguo.pocketmaps.STOP_LOCATION";

    @Override
    public void onCreate()
    {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent == null) return START_NOT_STICKY;

        if (ACTION_STOP.equals(intent.getAction()))
        {
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            return START_NOT_STICKY;
        }

        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private Notification buildNotification()
    {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.location_foreground_service_notification_text))
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= 26)
        {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.location_foreground_service_channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.location_foreground_service_channel_desc));
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null)
            {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
