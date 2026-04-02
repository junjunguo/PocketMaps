package com.junjunguo.pocketmaps.location;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.activities.MapActivity;
import com.junjunguo.pocketmaps.map.Tracking;
import com.villoren.android.kalmanlocationmanager.lib.KalmanLocationManager;

public class LocationService extends Service {
    private static final String TAG = LocationService.class.getName();
    private static final String CHANNEL_ID = "PocketMapsLocationChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final long GPS_TIME = 1000;
    private static final long NET_TIME = 5000;
    private static final long FILTER_TIME = 40;

    public interface LocationCallback {
        void onLocationChanged(Location location);
    }

    private final IBinder mBinder = new LocalBinder();
    private KalmanLocationManager kalmanLocationManager;
    private LocationCallback locationCallback;
    private boolean isTracking = false;
    private boolean isSmoothEnabled = true;

    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        kalmanLocationManager = new KalmanLocationManager(this);
        kalmanLocationManager.setMaxPredictTime(10000);
        Log.i(TAG, "LocationService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "LocationService started");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void startLocationUpdates(boolean smoothEnabled) {
        this.isSmoothEnabled = smoothEnabled;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted");
            return;
        }

        Notification notification = createNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        if (isSmoothEnabled) {
            kalmanLocationManager.requestLocationUpdates(
                KalmanLocationManager.UseProvider.GPS,
                FILTER_TIME,
                GPS_TIME,
                NET_TIME,
                locationListener,
                false
            );
        } else {
            LocationManagerHelper.requestLocationUpdates(this, locationListener);
        }
        Log.i(TAG, "Location updates started");
    }

    public void stopLocationUpdates() {
        kalmanLocationManager.removeUpdates(locationListener);
        LocationManagerHelper.removeUpdates(this, locationListener);
        stopForeground(true);
        Log.i(TAG, "Location updates stopped");
    }

    public void setLocationCallback(LocationCallback callback) {
        this.locationCallback = callback;
    }

    public void setTrackingMode(boolean tracking) {
        this.isTracking = tracking;
    }

    private final android.location.LocationListener locationListener = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (isTracking) {
                Tracking.getTracking(getApplicationContext()).addPoint(location, null);
            }
            if (locationCallback != null) {
                locationCallback.onLocationChanged(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, android.os.Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(TAG, "Provider enabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(TAG, "Provider disabled: " + provider);
        }
    };

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows when PocketMaps is tracking your location");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MapActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PocketMaps")
            .setContentText("Tracking your location")
            .setSmallIcon(R.drawable.ic_my_location_white_24dp)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        Log.i(TAG, "LocationService destroyed");
        super.onDestroy();
    }
}
