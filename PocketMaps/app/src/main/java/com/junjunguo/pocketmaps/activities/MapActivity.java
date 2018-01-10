package com.junjunguo.pocketmaps.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.map.MapHandler;
import com.junjunguo.pocketmaps.map.Tracking;
import com.junjunguo.pocketmaps.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.util.Variable;
import com.junjunguo.pocketmaps.navigator.NaviEngine;

import java.io.File;

import org.oscim.android.MapView;
import org.oscim.core.GeoPoint;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 04, 2015.
 */
public class MapActivity extends Activity implements LocationListener {
    enum PermissionStatus { Enabled, Disabled, Requesting, Unknown };
    private MapView mapView;
    private static Location mCurrentLocation;
    private Location mLastLocation;
    private MapActions mapActions;
    private LocationManager locationManager;
    private PermissionStatus locationListenerStatus = PermissionStatus.Unknown;
    private String lastProvider;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastProvider = null;
        setContentView(R.layout.activity_map);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Variable.getVariable().setContext(getApplicationContext());
        Variable.getVariable().setZoomLevels(22, 1);
//        AndroidGraphicFactory.createInstance(getApplication());
        mapView = new MapView(this);
        mapView.setClickable(true);
//        mapView.setBuiltInZoomControls(false);
        MapHandler.getMapHandler()
                .init(this, mapView, Variable.getVariable().getCountry(), Variable.getVariable().getMapsFolder());
        try
        {
          MapHandler.getMapHandler().loadMap(new File(Variable.getVariable().getMapsFolder().getAbsolutePath(),
                Variable.getVariable().getCountry() + "-gh"));
        }
        catch (Exception e)
        {
          logUser("Map file seems corrupt!\nPlease try to re-download.");
          log("Error while loading map!");
          e.printStackTrace();
          finish();
          Intent intent = new Intent(this, MainActivity.class);
          intent.putExtra("SELECTNEWMAP", true);
          startActivity(intent);
          return;
        }
        customMapView();
        checkGpsAvailability();
        ensureLastLocationInit();
        updateCurrentLocation(null);
    }
    
    public void ensureLocationListener(boolean showMsgEverytime)
    {
      if (locationListenerStatus == PermissionStatus.Disabled) { return; }
      if (locationListenerStatus != PermissionStatus.Enabled)
      {
        boolean f_loc = Permission.checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, this);
        if (!f_loc)
        {
          if (locationListenerStatus == PermissionStatus.Requesting)
          {
            locationListenerStatus = PermissionStatus.Disabled;
            return;
          }
          locationListenerStatus = PermissionStatus.Requesting;
          String[] permissions = new String[2];
          permissions[0] = android.Manifest.permission.ACCESS_FINE_LOCATION;
          permissions[1] = android.Manifest.permission.ACCESS_COARSE_LOCATION;
          Permission.startRequest(permissions, false, this);
          return;
        }
      }
      try
      {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider==null)
        {
          lastProvider = null;
          locationManager.removeUpdates(this);
          logUser("LocationProvider is off!");
          return;
        }
        else if (provider.equals(lastProvider))
        {
          if (showMsgEverytime)
          {
            logUser("LocationProvider: " + provider);
          }
          return;
        }
        locationManager.removeUpdates(this);
        lastProvider = provider;
        locationManager.requestLocationUpdates(provider, 3000, 5, this);
        logUser("LocationProvider: " + provider);
        locationListenerStatus = PermissionStatus.Enabled;
      }
      catch (SecurityException ex)
      {
        logUser("Location_Service not allowed by user!");
      }
    }

    /**
     * inject and inflate activity map content to map activity context and bring it to front
     */
    private void customMapView() {
        ViewGroup inclusionViewGroup = (ViewGroup) findViewById(R.id.custom_map_view_layout);
        View inflate = LayoutInflater.from(this).inflate(R.layout.activity_map_content, null);
        inclusionViewGroup.addView(inflate);

        inclusionViewGroup.getParent().bringChildToFront(inclusionViewGroup);
        new SetStatusBarColor().setSystemBarColor(findViewById(R.id.statusBarBackgroundMap),
                getResources().getColor(R.color.my_primary_dark_transparent), this);
        mapActions = new MapActions(this, mapView);
    }

    /**
     * check if GPS enabled and if not send user to the GSP settings
     */
    private void checkGpsAvailability() {
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    /**
     * Updates the users location based on the location
     *
     * @param location Location
     */
    private void updateCurrentLocation(Location location) {
        if (location != null) {
            mCurrentLocation = location;
        } else if (mLastLocation != null && mCurrentLocation == null) {
            mCurrentLocation = mLastLocation;
        }
        if (mCurrentLocation != null) {
            GeoPoint mcLatLong = new GeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            if (Tracking.getTracking().isTracking()) {
                MapHandler.getMapHandler().addTrackPoint(mcLatLong);
                Tracking.getTracking().addPoint(mCurrentLocation, mapActions.getAppSettings());
            }
            if (NaviEngine.getNaviEngine().isNavigating())
            {
              NaviEngine.getNaviEngine().updatePosition(this, mCurrentLocation);
            }
            MapHandler.getMapHandler().setCustomPoint(mcLatLong, R.drawable.ic_my_location_dark_24dp);
            mapActions.showPositionBtn.setImageResource(R.drawable.ic_my_location_white_24dp);
        } else {
            mapActions.showPositionBtn.setImageResource(R.drawable.ic_location_searching_white_24dp);
        }
    }

    @Override public void onBackPressed() {
        boolean back = mapActions.homeBackKeyPressed();
        if (back) {
            moveTaskToBack(true);
        }
        // if false do nothing
    }

    @Override protected void onStart() {
        super.onStart();
    }

    @Override public void onResume() {
        super.onResume();
        mapView.onResume();
        ensureLocationListener(true);
        ensureLastLocationInit();
    }

    @Override protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override protected void onStop() {
        super.onStop();
        if (mCurrentLocation != null) {
            GeoPoint geoPoint = mapView.map().getMapPosition().getGeoPoint();
            Variable.getVariable().setLastLocation(geoPoint);
            //                        log("last browsed location : "+mapView.getModel().mapViewPosition
            // .getMapPosition().latLong);
        }
        if (mapView != null) Variable.getVariable().setLastZoomLevel(mapView.map().getMapPosition().getZoomLevel());
        Variable.getVariable().saveVariables();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
        lastProvider = null;
        mapView.onDestroy();
        if (MapHandler.getMapHandler().getHopper() != null) MapHandler.getMapHandler().getHopper().close();
        MapHandler.getMapHandler().setHopper(null);
        System.gc();
    }

    /**
     * @return my currentLocation
     */
    public static Location getmCurrentLocation() {
        return mCurrentLocation;
    }

    private void ensureLastLocationInit()
    {
      if (mLastLocation != null) { return; }
      try
      {
        Location lonet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lonet != null) { mLastLocation = lonet; return; }
      }
      catch (SecurityException|IllegalArgumentException e)
      {
        log("NET-Location is not supported: " + e.getMessage());
      }
      try
      {
        Location logps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (logps != null) { mLastLocation = logps; return; }
      }
      catch (SecurityException|IllegalArgumentException e)
      {
        log("GPS-Location is not supported: " + e.getMessage());
      }
    }

    /**
     * Called when the location has changed.
     * <p/>
     * <p> There are no restrictions on the use of the supplied Location object.
     *
     * @param location The new location, as a Location object.
     */
    @Override public void onLocationChanged(Location location) {
        updateCurrentLocation(location);
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override public void onProviderEnabled(String provider) {
        logUser("LocationService is turned on!!");
    }

    @Override public void onProviderDisabled(String provider) {
        logUser("LocationService is turned off!!");
    }

    /**
     * send message to logcat
     *
     * @param str
     */
    private void log(String str) {
        Log.i(this.getClass().getName(), str);
    }
    
    private void logUser(String str) {
      Log.i(this.getClass().getName(), str);
      Toast.makeText(getBaseContext(), str, Toast.LENGTH_SHORT).show();
    }
}
