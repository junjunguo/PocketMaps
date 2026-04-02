package com.junjunguo.pocketmaps.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class LocationManagerHelper {
    private static final String TAG = LocationManagerHelper.class.getName();

    public static void requestLocationUpdates(Context context, LocationListener listener) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted");
            return;
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Log.e(TAG, "LocationManager is null");
            return;
        }

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager.getBestProvider(criteria, true);
        
        if (provider != null) {
            locationManager.requestLocationUpdates(provider, 3000, 5, listener, Looper.getMainLooper());
            Log.i(TAG, "Using provider: " + provider);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5, listener, Looper.getMainLooper());
            Log.i(TAG, "Using GPS provider as fallback");
        }
    }

    public static void removeUpdates(Context context, LocationListener listener) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.removeUpdates(listener);
        }
    }
}
