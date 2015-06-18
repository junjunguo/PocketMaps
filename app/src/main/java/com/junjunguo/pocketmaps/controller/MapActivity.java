package com.junjunguo.pocketmaps.controller;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.graphhopper.GraphHopper;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.map.MapHandler;
import com.junjunguo.pocketmaps.model.util.SetStatusBarColor;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.overlay.Marker;

import java.io.File;

public class MapActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private MapView mapView;
    private GraphHopper hopper;
    private volatile boolean prepareInProgress = false;
    private Location mCurrentLocation;
    private Marker mPositionMarker;
    private String currentArea;
    private File mapsFolder;
    private Location mLastLocation;
    private MapHandler mapHandler;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getExtraFromIntent();
        buildGoogleApiClient();
        AndroidGraphicFactory.createInstance(getApplication());
        mapView = new MapView(this);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapHandler = new MapHandler(this, mapView, currentArea, hopper, mapsFolder, prepareInProgress);
        mapHandler.loadMap(new File(mapsFolder.getAbsolutePath(), currentArea + "-gh"));
        customMapView();
        checkGpsAvailability();
        updateCurrentLocation(null);

    }

    private void customMapView() {
        ViewGroup inclusionViewGroup = (ViewGroup) findViewById(R.id.inclusion_layout);

        View child = LayoutInflater.from(this).inflate(R.layout.activity_map_content, null);
        child.bringToFront();
        inclusionViewGroup.addView(child);

        View customSidebar = LayoutInflater.from(this).inflate(R.layout.custom_sidebar, null);
        inclusionViewGroup.addView(customSidebar);
        log("-----------p-" + customSidebar.getParent().toString());
        log("-----------pp-" + customSidebar.getParent().getParent().getClass().getName().toString());
        log("-----------ppp-" + customSidebar.getParent().getParent().getParent().toString());
        log("-----------pppp-" + customSidebar.getParent().getParent().getParent().getParent().toString());
        ImageButton zoomIn = (ImageButton) findViewById(R.id.zoom_in);
        log("-----zoom in btn---"+zoomIn.toString());
        log("-----zoom in btn-p--"+zoomIn.getParent().toString());
        log("-----zoom in btn-pp--"+zoomIn.getParent().getParent().toString());

        log("==mapView=children count="+mapView.getChildCount());
        log("==mapView=get child at 0="+mapView.getChildAt(0).toString());
        log("==mapView=get child at 0="+mapView.getChildAt(0).getContext().toString());
        log("==mapView=="+mapView.getContext().toString());
        log("==mapView=p="+mapView.getParent().toString());
        log("==mapView=p="+mapView.getParent().getParent().toString());
        zoomIn.bringToFront();// parent != null ?
        mapView.getParent().bringChildToFront(inclusionViewGroup);
//        requestLayout();
//        mapView.invalidate();
        //        mapView.addView(inclusionViewGroup);
        //        RelativeLayout item = (RelativeLayout)findViewById(R.id.my_side_bar);
        //        View child = getLayoutInflater().inflate(R.layout.custom_side_bar, null);
        //        item.addView(child);

        //        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //        View view = layoutInflater.inflate(R.layout.activity_map_content, mapView);

        new SetStatusBarColor(findViewById(R.id.statusBarBackgroundMap),
                getResources().getColor(R.color.my_primary_dark_transparent), this);
        logToast("zoom in ;;;;;;;;; " + (findViewById(R.id.zoom_in).toString()));
    }

    /**
     * move map to my current location as the center of the screen
     */
    protected void showMyLocation() {
        mapView.getModel().mapViewPosition.setMapPosition(
                new MapPosition(new LatLong(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                        (byte) 16));
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
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
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
     * @param location
     */
    private void updateCurrentLocation(Location location) {
        if (location != null) {
            mCurrentLocation = location;
        } else if (mLastLocation != null && mCurrentLocation == null) {
            mCurrentLocation = mLastLocation;

        }
        if (mCurrentLocation != null) {
            Layers layers = mapView.getLayerManager().getLayers();
            mapHandler.removeLayer(layers, mPositionMarker);
            mPositionMarker = mapHandler
                    .createMarker(new LatLong(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                            R.drawable.my_position);
            layers.add(mPositionMarker);

            //            invalidateOptionsMenu();
            //            imgShowPosition.setVisibility(View.VISIBLE);
        } else {
            //            imgShowPosition.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * get extra data from Main Activity
     */
    private void getExtraFromIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            prepareInProgress = extras.getBoolean("prepareInProgressExtra");
            currentArea = extras.getString("currentAreaExtra");
            mapsFolder = new File(extras.getString("mapsFolderAbsolutePathExtra"));
            double latitude = extras.getDouble("mLastLocationLatitudeExtra");
            double longitude = extras.getDouble("mLastLocationLongitudeExtra");
            if (latitude != 0 || longitude != 0) {
                mLastLocation = new Location("default");
                mLastLocation.setLatitude(latitude);
                mLastLocation.setLongitude(longitude);
                logToast("get extra last location: " + mCurrentLocation);
            }
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


    @Override protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (hopper != null) hopper.close();
        hopper = null;
        System.gc();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                //                got to setting;
                return true;
            case R.id.menu_map_google:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                // get rid of the dialog
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                intent.setData(
                        Uri.parse("http://maps.google.com/maps?saddr=" + mapHandler.getStartPoint().latitude + "," +
                                mapHandler.getStartPoint().longitude +
                                "&daddr=" +
                                mapHandler.getEndPoint().latitude + "," + mapHandler.getEndPoint().longitude));
                startActivity(intent);
                return true;
            //                        case R.id.action_bar_show_position:
            case R.id.menu_show_position:
                logToast("show my location ...");
                showMyLocation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemGoogle = menu.findItem(R.id.menu_map_google);
        MenuItem imgShowPosition = menu.findItem(R.id.menu_show_position);
        if (mapHandler.getStartPoint() == null || mapHandler.getEndPoint() == null) {
            itemGoogle.setVisible(false);
        } else {
            itemGoogle.setVisible(true);
        }
        if (mCurrentLocation != null) {
            imgShowPosition.setVisible(true);
        } else {
            imgShowPosition.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override public void onConnectionFailed(ConnectionResult connectionResult) {
        logToast("on connection failed: " + connectionResult.getErrorCode());
    }

    @Override public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        startLocationUpdates();
        logToast("on connected: " + mCurrentLocation);
    }

    @Override public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        logToast("Connection suspended");
        mGoogleApiClient.connect();
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
        Log.i(this.getClass().getSimpleName(), str);
    }

    private void log(String str, Throwable t) {
        Log.i(this.getClass().getSimpleName(), str, t);
    }

    /**
     * send message to logcat and Toast it on screen
     *
     * @param str: message
     */
    private void logToast(String str) {
        log(str);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    /**
     * show my position is clicked
     *
     * @param view
     */
    public void showMyPosition(View view) {

    }
}
