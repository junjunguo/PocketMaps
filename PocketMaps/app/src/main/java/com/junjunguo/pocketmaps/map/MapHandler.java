package com.junjunguo.pocketmaps.map;

import android.app.Activity;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.oscim.android.MapView;
import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.Layer;
import org.oscim.layers.vector.PathLayer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Layers;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapFileTileSource;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.android.GHAsyncTask;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.util.Constants;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.Parameters.Algorithms;
import com.graphhopper.util.Parameters.Routing;
import com.graphhopper.util.PointList;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.activities.MapActivity;
import com.junjunguo.pocketmaps.activities.ShowLocationActivity;
import com.junjunguo.pocketmaps.model.listeners.MapHandlerListener;
import com.junjunguo.pocketmaps.navigator.NaviEngine;
import com.junjunguo.pocketmaps.util.TargetDirComputer;
import com.junjunguo.pocketmaps.util.Variable;


public class MapHandler
{
  private static MapHandler mapHandler;
  private volatile boolean prepareInProgress = false;
  private volatile boolean calcPathActive = false;
  MapPosition tmpPos = new MapPosition();
  private GeoPoint startMarker;
  private GeoPoint endMarker;
  private boolean needLocation = false;
  private MapView mapView;
  private ItemizedLayer<MarkerItem> itemizedLayer;
  private ItemizedLayer<MarkerItem> customLayer;
  private PathLayer pathLayer;
  private PathLayer polylineTrack;
  private GraphHopper hopper;
  private MapHandlerListener mapHandlerListener;
  private String currentArea;
  File mapsFolder;
  PointList trackingPointList = new PointList();
  private int customIcon = R.drawable.ic_my_location_dark_24dp;
  private MapFileTileSource tileSource;
  /**
   * need to know if path calculating status change; this will trigger MapActions function
   */
  private boolean needPathCal;
  
  public static MapHandler getMapHandler()
  {
    if (mapHandler == null)
    {
      reset();
    }
    return mapHandler;
  }

   /**
    * reset class, build a new instance
    */
  public static void reset()
  {
    mapHandler = new MapHandler();
  }

  private MapHandler()
  {
    setCalculatePath(false,false);
    startMarker = null;
    endMarker = null;
    needLocation = false;
    needPathCal = false;
  }

  public void init(MapView mapView, String currentArea, File mapsFolder)
  {
    this.mapView = mapView;
    this.currentArea = currentArea;
    this.mapsFolder = mapsFolder; // path/to/map/area-gh/
  }
  
  public MapFileTileSource getTileSource() { return tileSource; }

  /**
   * load map to mapView
   *
   * @param areaFolder
   */
  public void loadMap(File areaFolder, MapActivity activity)
  {
    logUser(activity, "loading map");

    // Map events receiver
    mapView.map().layers().add(new MapEventsReceiver(mapView.map()));

    // Map file source
    tileSource = new MapFileTileSource();
    tileSource.setMapFile(new File(areaFolder, currentArea + ".map").getAbsolutePath());
    VectorTileLayer l = mapView.map().setBaseMap(tileSource);
    mapView.map().setTheme(VtmThemes.DEFAULT);
    mapView.map().layers().add(new BuildingLayer(mapView.map(), l));
    mapView.map().layers().add(new LabelLayer(mapView.map(), l));
    
    // Markers layer
    itemizedLayer = new ItemizedLayer<>(mapView.map(), (MarkerSymbol) null);
    mapView.map().layers().add(itemizedLayer);
    customLayer = new ItemizedLayer<>(mapView.map(), (MarkerSymbol) null);
    mapView.map().layers().add(customLayer);

    // Map position
    GeoPoint mapCenter = tileSource.getMapInfo().boundingBox.getCenterPoint();
    mapView.map().setMapPosition(mapCenter.getLatitude(), mapCenter.getLongitude(), 1 << 12);

    ViewGroup.LayoutParams params =
        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    activity.addContentView(mapView, params);
    
    loadGraphStorage(activity);
  }
  
  void loadGraphStorage(final MapActivity activity) {
      logUser(activity, "loading graph (" + Constants.VERSION + ") ... ");
      new GHAsyncTask<Void, Void, Path>() {
          protected Path saveDoInBackground(Void... v) throws Exception {
              GraphHopper tmpHopp = new GraphHopper().forMobile();
              // Why is "shortest" missing in default config? Add!
              tmpHopp.getCHFactoryDecorator().addCHProfileAsString("shortest");
              tmpHopp.load(new File(mapsFolder, currentArea).getAbsolutePath() + "-gh");
              log("found graph " + tmpHopp.getGraphHopperStorage().toString() + ", nodes:" + tmpHopp.getGraphHopperStorage().getNodes());
              hopper = tmpHopp;
              return null;
          }

          protected void onPostExecute(Path o) {
              if (hasError()) {
                  logUser(activity, "An error happened while creating graph:"
                          + getErrorMessage());
              } else {
                  logUser(activity, "Finished loading graph.");
              }

              GeoPoint g = ShowLocationActivity.locationGeoPoint;
              String lss = ShowLocationActivity.locationSearchString;
              if (g != null)
              {
                activity.getMapActions().onPressLocationEndPoint(g);
                ShowLocationActivity.locationGeoPoint = null;
              }
              else if (lss != null)
              {
                activity.getMapActions().startGeocodeActivity(null, null, false, false);
              }
              prepareInProgress = false;
          }
      }.execute();
  }
  
  public AllEdgesIterator getAllEdges()
  {
    if (hopper==null) { return null; }
    if (hopper.getGraphHopperStorage()==null) { return null; }
    return hopper.getGraphHopperStorage().getAllEdges();
  }

  /**
   * center the LatLong point in the map and zoom map to zoomLevel
   *
   * @param latLong
   * @param zoomLevel (if 0 use current zoomlevel)
   */
  public void centerPointOnMap(GeoPoint latLong, int zoomLevel, float bearing, float tilt)
  {
    if (zoomLevel == 0)
    {
      zoomLevel = mapView.map().getMapPosition().zoomLevel;
    }
    log("Using cur zoom: " + zoomLevel);
    double scale = 1 << zoomLevel;
    tmpPos.setPosition(latLong);
    tmpPos.setScale(scale);
    tmpPos.setBearing(bearing);
    tmpPos.setTilt(tilt);
    mapView.map().animator().animateTo(300, tmpPos);
  }

  /**
   * @return
   */
  public boolean isNeedLocation()
  {
    return needLocation;
  }

  /**
   * set in need a location from screen point (touch)
   *
   * @param needLocation
   */
  public void setNeedLocation(boolean needLocation)
  {
    this.needLocation = needLocation;
  }

  /** Set start or end Point-Marker.
   *  @param p The Point to set, or null.
   *  @param isStart True for startpoint false for endpoint.
   *  @param recalculate True to calculate path, when booth points are set.
   *  @return Whether the path will be recalculated. **/
  public boolean setStartEndPoint(Activity activity, GeoPoint p, boolean isStart, boolean recalculate)
  {
    boolean result = false;
    boolean refreshBoth = false;
    if (startMarker!=null && endMarker!=null && p!=null) { refreshBoth = true; }
      
    if (isStart)
    {
      startMarker = p;
    }
    else { endMarker = p; }

    // remove routing layers
    if ((startMarker==null || endMarker==null) || refreshBoth)
    {
      if (pathLayer!=null) { pathLayer.clearPath(); }
      itemizedLayer.removeAllItems();
    }
    if (startMarker!=null)
    {
      itemizedLayer.addItem(createMarkerItem(activity, startMarker, R.drawable.ic_location_start_24dp, 0.5f, 1.0f));
    }
    if (endMarker!=null)
    {
      itemizedLayer.addItem(createMarkerItem(activity, endMarker, R.drawable.ic_location_end_24dp, 0.5f, 1.0f));
    }
    if (startMarker!=null && endMarker!=null && recalculate)
    {
      recalcPath(activity);
      result = true;
    }
    mapView.map().updateMap(true);
    return result;
  }
  
  public void recalcPath(Activity activity)
  {
    setCalculatePath(true, true);
    calcPath(startMarker.getLatitude(), startMarker.getLongitude(), endMarker.getLatitude(), endMarker.getLongitude(), activity);
  }

  /** Set the custom Point for current location, or null to delete.
   *  Sets the offset to center. **/
  public void setCustomPoint(Activity activity, GeoPoint p)
  {
    if (customLayer==null) { return; } // Not loaded yet.
    customLayer.removeAllItems();
    if (p!=null)
    {
      customLayer.addItem(createMarkerItem(activity, p,customIcon, 0.5f, 0.5f));
      mapView.map().updateMap(true);
    }
  }
  
  public void setCustomPointIcon(Activity activity, int customIcon)
  {
    this.customIcon = customIcon;
    if (customLayer.getItemList().size() > 0)
    { // RefreshIcon
      MarkerItem curSymbol = customLayer.getItemList().get(0);
      MarkerSymbol marker = createMarkerItem(activity, new GeoPoint(0,0), customIcon, 0.5f, 0.5f).getMarker();
      curSymbol.setMarker(marker);
    }
  }

  private MarkerItem createMarkerItem(Activity activity, GeoPoint p, int resource, float offsetX, float offsetY) {
//      Drawable drawable = activity.getDrawable(resource); // Since API21
      Drawable drawable = ContextCompat.getDrawable(activity, resource);
      Bitmap bitmap = AndroidGraphics.drawableToBitmap(drawable);
      MarkerSymbol markerSymbol = new MarkerSymbol(bitmap, offsetX, offsetY);
      MarkerItem markerItem = new MarkerItem("", "", p);
      markerItem.setMarker(markerSymbol);
      return markerItem;
  }

    /**
     * remove all markers and polyline from layers
     */
    public void removeMarkers(Activity activity) {
      // setCustomPoint(null, 0);
      setStartEndPoint(activity, null, true, false);
      setStartEndPoint(activity, null, false, false);
    }

    /**
     * @return true if already loaded
     */
    boolean isReady() {
      return !prepareInProgress;
    }

    /**
     * start tracking : reset polylineTrack & trackingPointList & remove polylineTrack if exist
     */
    public void startTrack(Activity activity) {
        if (polylineTrack != null) {
            removeLayer(mapView.map().layers(), polylineTrack);
        }
        polylineTrack = null;
        trackingPointList.clear();
        if (polylineTrack != null) { polylineTrack.clearPath(); }
        polylineTrack = updatePathLayer(activity, polylineTrack, trackingPointList, 0x99003399, 4);
        NaviEngine.getNaviEngine().startDebugSimulator(activity, true);
    }

    /**
     * add a tracking point
     *
     * @param point
     */
    public void addTrackPoint(Activity activity, GeoPoint point) {
      trackingPointList.add(point.getLatitude(), point.getLongitude());
      updatePathLayer(activity, polylineTrack, trackingPointList, 0x9900cc33, 4);
      mapView.map().updateMap(true);
    }
    
  /**
   * remove a layer from map layers
   *
   * @param layers
   * @param layer
   */
  public static void removeLayer(Layers layers, Layer layer)
  {
    if (layers != null && layer != null && layers.contains(layer))
    {
      layers.remove(layer);
    }
  }

    public boolean isCalculatingPath() {
        return calcPathActive;
    }

    private void setCalculatePath(boolean calcPathActive, boolean callListener) {
        this.calcPathActive = calcPathActive;
        if (mapHandlerListener != null && needPathCal && callListener) mapHandlerListener.pathCalculating(calcPathActive);
    }

    public void setNeedPathCal(boolean needPathCal) {
        this.needPathCal = needPathCal;
    }

    /**
     * Get the hopper object, that may be null while loading map.
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
     * only tell on object
     *
     * @param mapHandlerListener
     */
    public void setMapHandlerListener(MapHandlerListener mapHandlerListener) {
        this.mapHandlerListener = mapHandlerListener;
    }

    public void calcPath(final double fromLat, final double fromLon,
                         final double toLat, final double toLon, final Activity activity) {
        setCalculatePath(true, false);
        log("calculating path ...");
        new AsyncTask<Void, Void, GHResponse>() {
            float time;

            @Override
            protected GHResponse doInBackground(Void... v) {
              StopWatch sw = new StopWatch().start();
              GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon).
                      setAlgorithm(Algorithms.DIJKSTRA_BI);
              req.getHints().put(Routing.INSTRUCTIONS, Variable.getVariable().getDirectionsON());
              req.setVehicle(Variable.getVariable().getTravelMode().toString().toLowerCase());
              req.setWeighting(Variable.getVariable().getWeighting());
              GHResponse resp = null;
              if (hopper != null)
              {
                resp = hopper.route(req);
              }
              if (resp==null || resp.hasErrors())
              {
                NaviEngine.getNaviEngine().setDirectTargetDir(true);
                Throwable error;
                if (resp != null) { error = resp.getErrors().get(0); }
                else { error = new NullPointerException("Hopper is null!!!"); }
                log("Multible errors, first: " + error);
                resp = TargetDirComputer.getInstance().createTargetdirResponse(fromLat, fromLon, toLat, toLon);
              }
              else
              {
                NaviEngine.getNaviEngine().setDirectTargetDir(false);
              }
              time = sw.stop().getSeconds();
              return resp;
            }


            @Override
            protected void onPostExecute(GHResponse ghResp) {
                if (!ghResp.hasErrors()) {
                    PathWrapper resp = ghResp.getBest();
                    log("from:" + fromLat + "," + fromLon + " to:" + toLat + ","
                            + toLon + " found path with distance:" + resp.getDistance()
                            / 1000f + ", nodes:" + resp.getPoints().getSize() + ", time:"
                            + time + " " + resp.getDebugInfo());
                    logUser(activity, "the route is " + (int) (resp.getDistance() / 100) / 10f
                            + "km long, time:" + resp.getTime() / 60000f + "min.");

                    int sWidth = 4;
                    pathLayer = updatePathLayer(activity, pathLayer, resp.getPoints(), 0x9900cc33, sWidth);
                    mapView.map().updateMap(true);
                    if (Variable.getVariable().isDirectionsON()) {
                      Navigator.getNavigator().setGhResponse(resp);
                    }
                } else {
                    logUser(activity, "Multible errors: " + ghResp.getErrors().size());
                    log("Multible errors, first: " + ghResp.getErrors().get(0));
                }
                if (NaviEngine.getNaviEngine().isNavigating())
                {
                  setCalculatePath(false, false);
                }
                else
                {
                  setCalculatePath(false, true);
                  try
                  {
                    activity.findViewById(R.id.map_nav_settings_path_finding).setVisibility(View.GONE);
                    activity.findViewById(R.id.nav_settings_layout).setVisibility(View.VISIBLE);
                  }
                  catch (Exception e) { e.printStackTrace(); }
                }
            }
        }.execute();
    }
    
  private PathLayer updatePathLayer(Activity activity, PathLayer ref, PointList pointList, int color, int strokeWidth) {
      if (ref==null) {
          ref = createPathLayer(activity, color, strokeWidth);
          mapView.map().layers().add(ref);
      }
      List<GeoPoint> geoPoints = new ArrayList<>();
      //TODO: Search for a more efficient way
      for (int i = 0; i < pointList.getSize(); i++)
          geoPoints.add(new GeoPoint(pointList.getLatitude(i), pointList.getLongitude(i)));
      ref.setPoints(geoPoints);
      return ref;
  }
  
  public void joinPathLayerToPos(double lat, double lon)
  {
    try
    {
      List<GeoPoint> geoPoints = new ArrayList<>();
      geoPoints.add(new GeoPoint(lat,lon));
      geoPoints.add(pathLayer.getPoints().get(1));
      pathLayer.setPoints(geoPoints);
    }
    catch (Exception e) { log("Error: " + e); }
  }
    
  private PathLayer createPathLayer(Activity activity, int color, int strokeWidth)
  {
      Style style = Style.builder()
        .fixed(true)
        .generalization(Style.GENERALIZATION_SMALL)
        .strokeColor(color)
        .strokeWidth(strokeWidth * activity.getResources().getDisplayMetrics().density)
        .build();
      PathLayer newPathLayer = new PathLayer(mapView.map(), style);
      return newPathLayer;
  }
    
    class MapEventsReceiver extends Layer implements GestureListener {

      MapEventsReceiver(org.oscim.map.Map map) {
          super(map);
      }

      @Override
      public boolean onGesture(Gesture g, MotionEvent e) {
          if (g instanceof Gesture.Tap) {
              GeoPoint p = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
              if (mapHandlerListener!=null && needLocation)
              {
                mapHandlerListener.onPressLocation(p);
              }
          }
          return false;
      }
  }
  
  private void logUser(Activity activity, String str)
  {
    log(str);
    try
    {
      Toast.makeText(activity, str, Toast.LENGTH_LONG).show();
    }
    catch (Exception e) { e.printStackTrace(); }
  }
  
  private void log(String str)
  {
    Log.i(MapHandler.class.getName(), str);
  }
}

