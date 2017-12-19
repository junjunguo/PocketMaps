package com.junjunguo.pocketmaps.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.junjunguo.pocketmaps.navigator.Navigator;

import java.io.File;

import org.oscim.android.MapView;
import org.oscim.core.GeoPoint;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 04, 2015.
 */
public class MapActivity extends Activity implements LocationListener {
    enum ActionStatus { Enabled, Disabled, Requesting, Unknown };
    private MapView mapView;
    private static Location mCurrentLocation;
    private Location mLastLocation;
    private MapActions mapActions;
    private LocationManager locationManager;
    private ActionStatus locationListenerStatus = ActionStatus.Unknown;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        MapHandler.getMapHandler().loadMap(new File(Variable.getVariable().getMapsFolder().getAbsolutePath(),
                Variable.getVariable().getCountry() + "-gh"));
        customMapView();
        checkGpsAvailability();

        getMyLastLocation();
        updateCurrentLocation(null);
    }
    
    private void ensureLocationListener()
    {
      if (locationListenerStatus == ActionStatus.Disabled
          || locationListenerStatus == ActionStatus.Enabled) { return; }
      boolean f_loc = Permission.checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, this);
      if (!f_loc)
      {
        if (locationListenerStatus == ActionStatus.Requesting)
        {
          locationListenerStatus = ActionStatus.Disabled;
          return;
        }
        locationListenerStatus = ActionStatus.Requesting;
        Permission.startRequest(android.Manifest.permission.ACCESS_FINE_LOCATION, false, this);
        return;
      }
      
      try
      {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5, this);
        locationListenerStatus = ActionStatus.Enabled;
      }
      catch (SecurityException ex)
      {
        logUser("Location_Service not allowed by user");
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
                Tracking.getTracking().addPoint(mCurrentLocation);
            }
            if (Navigator.getNavigator().isNavigating())
            {
              Navigator.getNavigator().updatePosition(mCurrentLocation);
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
        ensureLocationListener();
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

    private void getMyLastLocation()
    {
      if (locationListenerStatus != ActionStatus.Enabled) { return; }
      try
      {
        mLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      }
      catch (SecurityException ex)
      {
        logUser("Location_Service not allowed by user");
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

    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public void onProviderEnabled(String provider) {
        logUser("Gps is turned on!!");
    }

    public void onProviderDisabled(String provider) {
        logUser("Gps is turned off!!");
    }

    /**
     * send message to logcat
     *
     * @param str
     */
    private void log(String str) {
        Log.i(this.getClass().getSimpleName(), "-------" + str);
    }
    
    private void logUser(String str) {
      Log.i(this.getClass().getSimpleName(), "-------" + str);
      Toast.makeText(getBaseContext(), str, Toast.LENGTH_SHORT).show();
    }
}
