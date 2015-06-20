package com.junjunguo.pocketmaps.controller;

import android.app.Activity;
import android.content.Context;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ZoomButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.map.MapHandler;
import com.junjunguo.pocketmaps.model.util.SetStatusBarColor;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.model.MapViewPosition;

import java.io.File;

public class MapActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private MapView mapView;
    private volatile boolean prepareInProgress = false;
    private Location mCurrentLocation;
    private Marker mPositionMarker;
    private String currentArea;
    private File mapsFolder;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private ImageButton showPositionImgBtn;
    private ZoomButton zoomInBtn, zoomOutBtn;
    private Context context;
    private int ZOOM_LEVEL_MAX;
    private int ZOOM_LEVEL_MIN;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        context = this;
        setZoomLevel(22, 1);
        getExtraFromIntent();
        buildGoogleApiClient();
        AndroidGraphicFactory.createInstance(getApplication());
        mapView = new MapView(this);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(false);
        MapHandler.getMapHandler().init(this, mapView, currentArea, mapsFolder, prepareInProgress);
        MapHandler.getMapHandler().loadMap(new File(mapsFolder.getAbsolutePath(), currentArea + "-gh"));
        customMapView();
        checkGpsAvailability();
        updateCurrentLocation(null);
    }

    /**
     * set map zoom level
     *
     * @param zoom_level_max
     * @param zoom_level_min
     */
    public void setZoomLevel(int zoom_level_max, int zoom_level_min) {
        this.ZOOM_LEVEL_MAX = zoom_level_max;
        this.ZOOM_LEVEL_MIN = zoom_level_min;
    }


    /**
     * inject and inflate activity map content to map activity context and bring it to front
     */
    private void customMapView() {
        ViewGroup inclusionViewGroup = (ViewGroup) findViewById(R.id.custom_map_view_layout);
        View inflate = LayoutInflater.from(this).inflate(R.layout.activity_map_content, null);
        inclusionViewGroup.addView(inflate);
        inclusionViewGroup.getParent().bringChildToFront(inclusionViewGroup);
//        new SetStatusBarColor(findViewById(R.id.statusBarBackgroundMap),
//                getResources().getColor(R.color.my_primary_dark_transparent), this);
        new SetStatusBarColor().setSystemBarColor(findViewById(R.id.statusBarBackgroundMap),
                getResources().getColor(R.color.my_primary_dark_transparent), this);
        sidebarBtnHandler();
    }

    /**
     * init and implement btn functions
     */
    private void sidebarBtnHandler() {
        this.showPositionImgBtn = (ImageButton) findViewById(R.id.show_my_position_btn);
        this.zoomInBtn = (ZoomButton) findViewById(R.id.zoom_in_btn);
        this.zoomOutBtn = (ZoomButton) findViewById(R.id.zoom_out_btn);
        showMyLocation();
        zoomControlHandler();
    }

    /**
     * implement zoom btn
     */
    private void zoomControlHandler() {
        zoomInBtn.setImageResource(R.drawable.zoom_in);
        zoomOutBtn.setImageResource(R.drawable.zoom_out);

        zoomInBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                MapViewPosition mvp = mapView.getModel().mapViewPosition;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        zoomInBtn.setImageResource(R.drawable.zoom_in_f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        zoomInBtn.setImageResource(R.drawable.zoom_in);
                        //                        logToast(
                        //                                "-----zoom level: " + mvp.getZoomLevel() + " max: " + mvp
                        // .getZoomLevelMax() + "hopper" +
                        //                                        MapHandler.getMapHandler().getHopper().toString());
                        if (mvp.getZoomLevel() < ZOOM_LEVEL_MAX) mvp.zoomIn();
                        return true;
                }
                return false;
            }
        });
        zoomOutBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                MapViewPosition mvp = mapView.getModel().mapViewPosition;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        zoomOutBtn.setImageResource(R.drawable.zoom_out_f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        zoomOutBtn.setImageResource(R.drawable.zoom_out);
                        if (mvp.getZoomLevel() > ZOOM_LEVEL_MIN) mvp.zoomOut();
                        return true;
                }

                return false;
            }
        });
    }


    /**
     * move map to my current location as the center of the screen
     */
    protected void showMyLocation() {
        showPositionImgBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        showPositionImgBtn.setImageResource(R.drawable.show_position_f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (mCurrentLocation != null) {
                            showPositionImgBtn.setImageResource(R.drawable.show_position);
                            mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(
                                    new LatLong(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                                    (byte) 16));
                        } else {
                            showPositionImgBtn.setImageResource(R.drawable.show_position_invisible);
                            Toast.makeText(context, "No Location Available", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                }

                return false;
            }
        });
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
            MapHandler.getMapHandler().removeLayer(layers, mPositionMarker);
            mPositionMarker = MapHandler.getMapHandler()
                    .createMarker(new LatLong(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                            R.drawable.my_position);
            layers.add(mPositionMarker);

            showPositionImgBtn.setImageResource(R.drawable.show_position);
        } else {
            showPositionImgBtn.setImageResource(R.drawable.show_position_invisible);
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
        if (MapHandler.getMapHandler().getHopper() != null) MapHandler.getMapHandler().getHopper().close();
        MapHandler.getMapHandler().setHopper(null);
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
                intent.setData(Uri.parse("http://maps.google.com/maps?saddr=" +
                                MapHandler.getMapHandler().getStartPoint().latitude + "," +
                                MapHandler.getMapHandler().getStartPoint().longitude +
                                "&daddr=" +
                                MapHandler.getMapHandler().getEndPoint().latitude + "," +
                                MapHandler.getMapHandler().getEndPoint().longitude));
                startActivity(intent);
                return true;
            //            case R.id.menu_show_position:
            //                logToast("show my location ...");
            //                showMyLocation();
            //                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemGoogle = menu.findItem(R.id.menu_map_google);
        if (MapHandler.getMapHandler().getStartPoint() == null || MapHandler.getMapHandler().getEndPoint() == null) {
            itemGoogle.setVisible(false);
        } else {
            itemGoogle.setVisible(true);
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
