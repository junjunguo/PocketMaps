package com.junjunguo.offlinemap.controller;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.util.Constants;
import com.graphhopper.util.PointList;
import com.graphhopper.util.StopWatch;
import com.junjunguo.offlinemap.R;
import com.junjunguo.offlinemap.model.map.GHAsyncTask;
import com.junjunguo.offlinemap.model.util.SetStatusBarColor;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.util.List;

public class MapActivity extends Activity implements LocationListener {
    private MapView mapView;
    private GraphHopper hopper;
    private LatLong startPoint;
    private LatLong endPoint;
    private volatile boolean prepareInProgress = false;
    private volatile boolean shortestPathRunning = false;
    private TileCache tileCache;
    //    private String bestProvider;
    private LocationManager locationManager;
    private Location mCurrentLocation;
    private Marker mPositionMarker;
    private String currentArea;
    private File mapsFolder;
    private Location mLastLocation;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getExtraFromIntent();
        new SetStatusBarColor(findViewById(R.id.statusBarBackgroundMap),
                getResources().getColor(R.color.my_primary_dark), this);
        AndroidGraphicFactory.createInstance(getApplication());
        initCurrentLocation();
        mapView = new MapView(this);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        tileCache = AndroidUtil
                .createTileCache(this, getClass().getSimpleName(), mapView.getModel().displayModel.getTileSize(), 1f,
                        mapView.getModel().frameBufferModel.getOverdrawFactor());
        loadMap(new File(mapsFolder.getAbsolutePath(), currentArea + "-gh"));
    }

    /**
     * initial current location variables
     */
    private void initCurrentLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }


    /**
     * Updates the users location based on the best provider,
     */
    private void updateCurrentLocation() {
        if (mLastLocation != null) {
            mCurrentLocation = mLastLocation;
        } else {
            mCurrentLocation = new Location("default");
            //            mCurrentLocation.setLatitude(52.537205);
            //            mCurrentLocation.setLongitude(13.394924);
            //            logToast("Could not find any locations stored on device");
        }
        if (mPositionMarker == null) {
            mPositionMarker = createMarker(new LatLong(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                    R.drawable.my_position);
        }

        //        Reporting.reportMyLocation(mCurrentLocation);
        onLocationChanged(mCurrentLocation);

    }

    /**
     * Updates the current user's location marker to reflect a change in position
     *
     * @param location the new location
     */

    private void updateMyPositionMarker(Location location) {

    }

    protected boolean onMapTap(LatLong tapLatLong, Point layerXY, Point tapXY) {
        if (!isReady()) return false;

        if (shortestPathRunning) {
            logToast("Calculation still in progress");
            return false;
        }
        Layers layers = mapView.getLayerManager().getLayers();

        if (startPoint != null && endPoint == null) {
            endPoint = tapLatLong;
            shortestPathRunning = true;
            Marker marker = createMarker(tapLatLong, R.drawable.position_end);
            if (marker != null) {
                layers.add(marker);
            }

            calcPath(startPoint.latitude, startPoint.longitude, endPoint.latitude, endPoint.longitude);
        } else {
            startPoint = tapLatLong;
            endPoint = null;
            // remove all layers but the first one, which is the map
            while (layers.size() > 1) {
                layers.remove(1);
            }

            Marker marker = createMarker(startPoint, R.drawable.position_start);
            if (marker != null) {
                layers.add(marker);
            }
        }
        return true;
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
            }

        }
    }


    @Override protected void onDestroy() {
        super.onDestroy();
        if (hopper != null) hopper.close();

        hopper = null;
        // necessary?
        System.gc();
    }

    boolean isReady() {
        // only return true if already loaded
        if (hopper != null) return true;

        if (prepareInProgress) {
            logToast("Preparation still in progress");
            return false;
        }
        logToast("Prepare finished but hopper not ready. This happens when there was an error while loading the files");
        return false;
    }


    /**
     * load map to mapView
     *
     * @param areaFolder
     */
    public void loadMap(File areaFolder) {
        logToast("loading map");
        File mapFile = new File(areaFolder, currentArea + ".map");

        mapView.getLayerManager().getLayers().clear();

        TileRendererLayer tileRendererLayer =
                new TileRendererLayer(tileCache, mapView.getModel().mapViewPosition, false, true,
                        AndroidGraphicFactory.INSTANCE) {
                    @Override public boolean onLongPress(LatLong tapLatLong, Point layerXY, Point tapXY) {
                        return onMapTap(tapLatLong, layerXY, tapXY);
                    }
                };
        tileRendererLayer.setMapFile(mapFile);
        tileRendererLayer.setTextScale(1.5f);
        tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
        mapView.getModel().mapViewPosition.setMapPosition(
                new MapPosition(tileRendererLayer.getMapDatabase().getMapFileInfo().boundingBox.getCenterPoint(),
                        (byte) 7));
        mapView.getLayerManager().getLayers().add(tileRendererLayer);

        setContentView(mapView);
        loadGraphStorage();
    }

    void loadGraphStorage() {
        logToast("loading graph (" + Constants.VERSION + ") ... ");
        new GHAsyncTask<Void, Void, Path>() {
            protected Path saveDoInBackground(Void... v) throws Exception {
                GraphHopper tmpHopp = new GraphHopper().forMobile();
                tmpHopp.load(new File(mapsFolder, currentArea).getAbsolutePath());
                log("found graph " + tmpHopp.getGraph().toString() + ", nodes:" + tmpHopp.getGraph().getNodes());
                hopper = tmpHopp;
                return null;
            }

            protected void onPostExecute(Path o) {
                if (hasError()) {
                    logToast("An error happend while creating graph:" + getErrorMessage());
                } else {
                    logToast(
                            "Finished loading graph. Press long to define where to startPoint and endPoint the route.");
                }

                finishPrepare();
            }
        }.execute();
    }

    private void finishPrepare() {
        prepareInProgress = false;
    }

    private Polyline createPolyline(GHResponse response) {
        Paint paintStroke = AndroidGraphicFactory.INSTANCE.createPaint();
        paintStroke.setStyle(Style.STROKE);
        paintStroke.setColor(Color.argb(200, 0, 0xCC, 0x33));
        paintStroke.setDashPathEffect(new float[]{25, 15});
        paintStroke.setStrokeWidth(8);

        // TODO: new mapsforge version wants an mapsforge-paint, not an android paint.
        // This doesn't seem to support transparceny
        //paintStroke.setAlpha(128);
        Polyline line = new Polyline((Paint) paintStroke, AndroidGraphicFactory.INSTANCE);
        List<LatLong> geoPoints = line.getLatLongs();
        PointList tmp = response.getPoints();
        for (int i = 0; i < response.getPoints().getSize(); i++) {
            geoPoints.add(new LatLong(tmp.getLatitude(i), tmp.getLongitude(i)));
        }

        return line;
    }

    private Marker createMarker(LatLong p, int resource) {
        Drawable drawable = getResources().getDrawable(resource);
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
        return new Marker(p, bitmap, 0, -bitmap.getHeight() / 2);
    }

    public void calcPath(final double fromLat, final double fromLon, final double toLat, final double toLon) {

        log("calculating path ...");
        new AsyncTask<Void, Void, GHResponse>() {
            float time;

            protected GHResponse doInBackground(Void... v) {
                StopWatch sw = new StopWatch().start();
                GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon).
                        setAlgorithm(AlgorithmOptions.DIJKSTRA_BI);
                req.getHints().
                        put("instructions", "false");
                GHResponse resp = hopper.route(req);
                time = sw.stop().getSeconds();
                return resp;
            }

            protected void onPostExecute(GHResponse resp) {
                if (!resp.hasErrors()) {
                    log("from:" + fromLat + "," + fromLon + " to:" + toLat + "," + toLon +
                            " found path with distance:" + resp.getDistance() / 1000f + ", nodes:" +
                            resp.getPoints().getSize() + ", time:" + time + " " + resp.getDebugInfo());
                    logToast("the route is " + (int) (resp.getDistance() / 100) / 10f + "km long, time:" +
                            resp.getMillis() / 60000f + "min, debug:" + time);

                    mapView.getLayerManager().getLayers().add(createPolyline(resp));
                    //mapView.redraw();
                } else {
                    logToast("Error:" + resp.getErrors());
                }
                shortestPathRunning = false;
            }
        }.execute();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                //                got to seeting;
                return true;
            case R.id.menu_map_google:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                // get rid of the dialog
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                intent.setData(Uri.parse(
                        "http://maps.google.com/maps?saddr=" + startPoint.latitude + "," + startPoint.longitude +
                                "&daddr=" +
                                endPoint.latitude + "," + endPoint.longitude));
                startActivity(intent);
                return true;
            //            case R.id.menu_map_quit:
            //                quitApp();
            //                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem itemGoogle = menu.findItem(R.id.menu_map_google);
        if (startPoint == null || endPoint == null) {
            itemGoogle.setVisible(false);
            logToast("start point endpoint" + startPoint + " " + endPoint);
        } else {
            itemGoogle.setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }


    /**
     * Called when the location has changed.
     * <p>
     * <p> There are no restrictions on the use of the supplied Location object.
     *
     * @param location The new location, as a Location object.
     */
    @Override public void onLocationChanged(Location location) {
        updateMyPositionMarker(location);
    }

    /**
     * Called when the provider status changes. This method is called when a provider is unable to fetch a location or
     * if the provider has recently become available after a period of unavailability.
     *
     * @param provider the name of the location provider associated with this update.
     * @param status   {@link LocationProvider#OUT_OF_SERVICE} if the provider is out of service, and this is not
     *                 expected to change in the near future; {@link LocationProvider#TEMPORARILY_UNAVAILABLE} if the
     *                 provider is temporarily unavailable but is expected to be available shortly; and {@link
     *                 LocationProvider#AVAILABLE} if the provider is currently available.
     * @param extras   an optional Bundle which will contain provider specific status variables.
     *                 <p>
     *                 <p> A number of common key/value pairs for the extras Bundle are listed below. Providers that use
     *                 any of the keys on this list must provide the corresponding value as described below.
     *                 <p>
     *                 <ul> <li> satellites - the number of satellites used to derive the fix
     */
    @Override public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this update.
     */
    @Override public void onProviderEnabled(String provider) {

    }

    /**
     * Called when the provider is disabled by the user. If requestLocationUpdates is called on an already disabled
     * provider, this method is called immediately.
     *
     * @param provider the name of the location provider associated with this update.
     */
    @Override public void onProviderDisabled(String provider) {

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
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

}
