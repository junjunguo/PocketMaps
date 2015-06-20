package com.junjunguo.pocketmaps.model.map;

import android.app.Activity;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.util.PointList;
import com.graphhopper.util.StopWatch;
import com.junjunguo.pocketmaps.R;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.util.List;

/**
 * MapHandler:
 * <p/>
 * This file is part of Pockets Maps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 15, 2015.
 */
public class MapHandler {
    private Activity activity;
    private MapView mapView;
    private String currentArea;
    private TileCache tileCache;
    private GraphHopper hopper;
    private File mapsFolder;
    private volatile boolean prepareInProgress = false;
    private volatile boolean shortestPathRunning = false;
    private LatLong startPoint;
    private LatLong endPoint;
    private Marker markerStart = null, markerEnd = null;
    private Polyline polylinePath = null;
    private String vehicle, weighting;

    public MapHandler(Activity activity, MapView mapView, String currentArea, File mapsFolder,
            boolean prepareInProgress) {
        this.activity = activity;
        this.mapView = mapView;
        this.currentArea = currentArea;
        tileCache = AndroidUtil
                .createTileCache(activity, getClass().getSimpleName(), mapView.getModel().displayModel.getTileSize(),
                        1f, mapView.getModel().frameBufferModel.getOverdrawFactor());
        this.mapsFolder = mapsFolder;
        this.prepareInProgress = prepareInProgress;
    }


    /**
     * load map to mapView
     *
     * @param areaFolder
     */
    public void loadMap(File areaFolder) {
        //        logToast("loading map");
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
        tileRendererLayer.setTextScale(0.6f);
        tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
        mapView.getModel().mapViewPosition.setMapPosition(
                new MapPosition(tileRendererLayer.getMapDatabase().getMapFileInfo().boundingBox.getCenterPoint(),
                        (byte) 7));
        mapView.getLayerManager().getLayers().add(tileRendererLayer);
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        activity.addContentView(mapView, params);
        loadGraphStorage();
    }


    /**
     * load graph from storage: Use and ready to search the map
     */
    private void loadGraphStorage() {
        //        logToast("loading graph (" + Constants.VERSION + ") ... ");
        new GHAsyncTask<Void, Void, Path>() {
            protected Path saveDoInBackground(Void... v) throws Exception {

                GraphHopper tmpHopp = new GraphHopper().forMobile();
                //                tmpHopp.setCHEnable(false);
                tmpHopp.load(new File(mapsFolder, currentArea).getAbsolutePath());

                log("======encoding manager:graph " + tmpHopp.getGraph().getEncodingManager().toDetailsString());
                log("======encoding manager: hop  " + tmpHopp.getEncodingManager().toDetailsString());
                log("found graph " + tmpHopp.getGraph().toString() + ", nodes:" +
                        tmpHopp.getGraph().getNodes());
                hopper = tmpHopp;
                return null;
            }

            protected void onPostExecute(Path o) {
                if (hasError()) {
                    logToast("An error happend while creating graph:" + getErrorMessage());
                } else {
                    logToast("Finished loading graph. Press long to define where to startPoint and endPoint the route" +
                            ".");
                }
                prepareInProgress = false;
            }
        }.execute();
    }

    /**
     * calculate a path: start to end
     *
     * @param fromLat
     * @param fromLon
     * @param toLat
     * @param toLon
     */
    public void calcPath(final double fromLat, final double fromLon, final double toLat, final double toLon) {
        //        log("calculating path ...");
        new AsyncTask<Void, Void, GHResponse>() {
            float time;

            protected GHResponse doInBackground(Void... v) {
                StopWatch sw = new StopWatch().start();
                GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon);
                req.setAlgorithm(AlgorithmOptions.DIJKSTRA);
                req.getHints().put("instructions", "true");
                req.setVehicle(getVehicle());
                req.setWeighting(getWeighting());
                try {
                    logToast("----encoder: " + req.getVehicle().toString() +
                            "\n weighting: " + req.getWeighting().toString());
                } catch (Exception e) {
                    e.getStackTrace();
                }
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
                    Navigator.getNavigator().setGhResponse(resp);

                    polylinePath = createPolyline(resp);
                    mapView.getLayerManager().getLayers().add(polylinePath);
                    log("navigator: " + Navigator.getNavigator().toString());
                } else {
                    logToast("Error:" + resp.getErrors());
                }
                shortestPathRunning = false;
            }
        }.execute();
    }

    /**
     * get start point and end point
     *
     * @param tapLatLong
     * @param layerXY
     * @param tapXY
     * @return
     */
    protected boolean onMapTap(LatLong tapLatLong, Point layerXY, Point tapXY) {
        if (!isReady()) return false;

        if (shortestPathRunning) {
            return false;
        }
        Layers layers = mapView.getLayerManager().getLayers();
        if (startPoint != null && endPoint == null) {
            endPoint = tapLatLong;
            shortestPathRunning = true;
            markerEnd = createMarker(tapLatLong, R.drawable.position_end);
            layers.add(markerEnd);
            calcPath(startPoint.latitude, startPoint.longitude, endPoint.latitude, endPoint.longitude);
        } else {
            startPoint = tapLatLong;
            endPoint = null;

            removeLayer(layers, markerEnd);
            markerEnd = null;
            removeLayer(layers, markerStart);
            markerStart = null;
            removeLayer(layers, polylinePath);
            polylinePath = null;

            markerStart = createMarker(startPoint, R.drawable.position_start);
            if (markerStart != null) {
                layers.add(markerStart);
            }
        }
        return true;
    }

    /**
     * remove a layer from map layers
     *
     * @param layers
     * @param layer
     */
    public void removeLayer(Layers layers, Layer layer) {
        if (layers != null && layer != null && layers.contains(layer)) {
            layers.remove(layer);
        }
    }

    /**
     * create a marker for map
     *
     * @param p
     * @param resource
     * @return
     */
    public Marker createMarker(LatLong p, int resource) {
        Drawable drawable = activity.getResources().getDrawable(resource);
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
        return new Marker(p, bitmap, 0, -bitmap.getHeight() / 2);
    }

    /**
     * @return true if already loaded
     */
    boolean isReady() {
        if (hopper != null) return true;

        if (prepareInProgress) {
            //            logToast("Preparation still in progress");
            return false;
        }
        //        logToast("Prepare finished but hopper not ready. This happens when there was an error while loading
        // the files");
        return false;
    }

    /**
     * draws a connected series of line segments specified by a list of LatLongs.
     *
     * @param response
     * @return Polyline
     */
    public Polyline createPolyline(GHResponse response) {
        Paint paintStroke = AndroidGraphicFactory.INSTANCE.createPaint();
        paintStroke.setStyle(Style.STROKE);
        paintStroke.setColor(activity.getResources().getColor(R.color.my_polyline_calculate));
        paintStroke.setDashPathEffect(new float[]{25, 25});
        paintStroke.setStrokeWidth(16);

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


    /**
     * center my location in the screen with zoom lever 16
     *
     * @param mCurrentLocation
     */
    public void showMyLocation(Location mCurrentLocation) {
        mapView.getModel().mapViewPosition.setMapPosition(
                new MapPosition(new LatLong(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                        (byte) 16));
    }

    /**
     * @return LatLong start Point
     */
    public LatLong getStartPoint() {
        return startPoint;
    }

    /**
     * @return LatLong end Point
     */
    public LatLong getEndPoint() {
        return endPoint;
    }

    /**
     * @return GraphHopper object
     */
    public GraphHopper getHopper() {
        return hopper;
    }

    /**
     * assign a new GraphHopper
     *
     * @param hopper
     */
    public void setHopper(GraphHopper hopper) {
        this.hopper = hopper;
    }

    /**
     * default bike (if not set )
     *
     * @return = bike,car or foot
     */
    public String getVehicle() {
        if (vehicle == null) {
            vehicle = "bike";
        }
        return vehicle;
    }

    /**
     * @param vehicle (bike,car or foot)
     */
    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    /**
     * @return weighting (fastest or shortest)
     */
    public String getWeighting() {
        if (weighting == null) {
            weighting = "fastest";
        }
        return weighting;
    }

    /**
     * @param weighting ("fastest or shortest")
     */
    public void setWeighting(String weighting) {
        this.weighting = weighting;
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
        Toast.makeText(activity, str, Toast.LENGTH_LONG).show();
    }
}
