package com.junjunguo.pocketmaps.controller;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.map.MapHandler;
import com.junjunguo.pocketmaps.model.map.Tracking;
import com.junjunguo.pocketmaps.model.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.model.util.Variable;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.overlay.Marker;

import java.io.File;

public class MapActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private MapView mapView;
    private static Location mCurrentLocation;
    private Marker mPositionMarker;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private MapActions mapActions;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Variable.getVariable().setContext(getApplicationContext());
        Variable.getVariable().setZoomLevels(22, 1);
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        AndroidGraphicFactory.createInstance(getApplication());
        mapView = new MapView(this);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(false);
        MapHandler.getMapHandler()
                .init(this, mapView, Variable.getVariable().getCountry(), Variable.getVariable().getMapsFolder());
        MapHandler.getMapHandler().loadMap(new File(Variable.getVariable().getMapsFolder().getAbsolutePath(),
                Variable.getVariable().getCountry() + "-gh"));
        customMapView();
        checkGpsAvailability();
        updateCurrentLocation(null);
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
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
     * accessing google play services
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient =
                new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API).build();
        createLocationRequest();

    }

    /**
     * initial LocationRequest: sets the update interval, fastest update interval, and priority ...
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5s
        //        mLocationRequest.setInterval(50); //5s
        mLocationRequest.setFastestInterval(1000); //2s
        //        mLocationRequest.setFastestInterval(10); //2s
        mLocationRequest.setSmallestDisplacement(5);
        //        mLocationRequest.setSmallestDisplacement(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * check if GPS enabled and if not send user to the GSP settings
     */
    private void checkGpsAvailability() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
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
            LatLong mcLatLong = new LatLong(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            if (Tracking.getTracking().isTracking()) {
                MapHandler.getMapHandler().addTrackPoint(mcLatLong);
                Tracking.getTracking().addPoint(mCurrentLocation);
            }
            Layers layers = mapView.getLayerManager().getLayers();
            MapHandler.getMapHandler().removeLayer(layers, mPositionMarker);
            mPositionMarker = MapHandler.getMapHandler().createMarker(mcLatLong, R.drawable.ic_my_location_dark_24dp);
            layers.add(mPositionMarker);
            mapActions.showPositionBtn.setImageResource(R.drawable.ic_my_location_white_24dp);
        } else {
            mapActions.showPositionBtn.setImageResource(R.drawable.ic_location_searching_white_24dp);
        }
    }

    /**
     * Requests location updates from the FusedLocationApi.
     * <p/>
     * The final argument to {@code requestLocationUpdates()} is a LocationListener
     * <p/>
     * (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
     */
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
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
    }

    @Override protected void onPause() {
        super.onPause();
    }

    @Override protected void onStop() {
        super.onStop();
        if (mCurrentLocation != null) {
            Variable.getVariable().setLastLocation(mapView.getModel().mapViewPosition.getMapPosition().latLong);
            //                        log("last browsed location : "+mapView.getModel().mapViewPosition
            // .getMapPosition().latLong);
        }
        if (mapView != null) Variable.getVariable().setLastZoomLevel(mapView.getModel().mapViewPosition.getZoomLevel());
        Variable.getVariable().saveVariables();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
        if (MapHandler.getMapHandler().getHopper() != null) MapHandler.getMapHandler().getHopper().close();
        MapHandler.getMapHandler().setHopper(null);
        System.gc();
    }

    @Override public void onConnectionFailed(ConnectionResult connectionResult) {
        //        log("on connection failed: " + connectionResult.getErrorCode());
    }

    @Override public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        startLocationUpdates();
        //        log("on connected: " + mCurrentLocation);
    }

    @Override public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        //        log("Connection suspended");
        mGoogleApiClient.connect();
    }

    /**
     * @return my currentLocation
     */
    public static Location getmCurrentLocation() {
        return mCurrentLocation;
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

    /**
     * send message to logcat
     *
     * @param str
     */
    private void log(String str) {
        Log.i(this.getClass().getSimpleName(), "-------" + str);
    }
}
