package com.junjunguo.pocketmaps.geocoding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oscim.backend.CanvasAdapter;
import org.oscim.core.GeoPoint;
import org.oscim.core.GeometryBuffer;
import org.oscim.core.MercatorProjection;
import org.oscim.core.Point;
import org.oscim.core.Tag;
import org.oscim.core.Tile;
import org.oscim.tiling.source.mapfile.MapDatabase;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import org.oscim.tiling.source.mapfile.MapReadResult;
import org.oscim.tiling.source.mapfile.PointOfInterest;
import org.oscim.tiling.source.mapfile.Way;
import org.oscim.utils.GeoPointUtils;
import org.osmdroid.location.GeocoderNominatim;

import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.storage.NodeAccess;
import com.junjunguo.pocketmaps.map.MapHandler;
import com.junjunguo.pocketmaps.util.Variable;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;

public class GeocoderLocal
{
  Context context;
  Locale locale;
  
  public GeocoderLocal(Context context, Locale locale)
  {
    this.context = context;
    this.locale = locale;
  }
  
  public static boolean isPresent() { return true; }
  
  
  public List<Address> getFromLocationName(String searchS, int maxCount) throws IOException
  {
    ArrayList<AddressLoc> cities = findCity(searchS, maxCount);
    ArrayList<Address> aCities = new ArrayList<Address>();
    for (AddressLoc curA : cities) { aCities.add(curA.toAndroidAddress(locale)); }
    //TODO: Find streets, adminAreas, states, and fuzzy search, use maxCount.
    return aCities;
  }
  
  private ArrayList<AddressLoc> findCity(String searchS, int maxCount) throws IOException
  {
    ArrayList<AddressLoc> result = new ArrayList<AddressLoc>();
    CityMatcher cityMatcher = new CityMatcher(searchS);

    String mapsPath = Variable.getVariable().getMapsFolder().getAbsolutePath();
    mapsPath = new File(mapsPath, Variable.getVariable().getCountry() + "-gh").getPath();
    mapsPath = new File(mapsPath, "cityNodes.txt").getPath();
    AddressLoc curAddress = new AddressLoc();
    try(FileReader r = new FileReader(mapsPath);
        BufferedReader br = new BufferedReader(r))
    {
      String line;
      while ((line = br.readLine())!=null)
      {
        if (GeocoderGlobal.isStopRunningActions()) { return null; }
        if (result.size() >= maxCount) { break; }
        if (line.startsWith("<node "))
        {
          curAddress = curAddress.ensureCopy();
          double lat = findDouble("lat", line);
          double lon = findDouble("lon", line);
          curAddress.location = new GeoPoint(lat,lon);
        }
        else if (line.startsWith("<tag "))
        {
          String key = findString("k", line);
          if (key==null) { continue; }
          if (key.equals("name"))
          {
            String val = findString("v", line);
            curAddress.city = val;
            if (!curAddress.isAdded)
            {
              boolean isMatching = cityMatcher.isMatching(val, false);
              if (isMatching)
              {
                result.add(curAddress);
                curAddress.isAdded = true;
              }
            }
          }
          if (key.contains("postal_code"))
          {
            String val = findString("v", line);
            curAddress.postalCode = val;
            if (!curAddress.isAdded)
            {
              boolean isMatching = cityMatcher.isMatching(val, true);
              if (isMatching)
              {
                result.add(curAddress);
                curAddress.isAdded = true;
              }
            }
          }
        }
      }
    }
    catch (IOException e)
    {
      throw e;
    }
    return result;
  }
  
  private double findDouble(String key, String txt)
  {
    String s = findString(key, txt);
    if (s==null)
    {
      Log.e(GeocoderLocal.class.getName(), "Double for key not found: " + key);
      return 0;
    }
    return Double.parseDouble(s);
  }

  private String findString(String key, String txt)
  {
    String regex = " " + key + "=\"[^\"]*\"";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(txt);
    if (!matcher.find()) { return null; }
    String attr = matcher.group();
    int index = key.length() + 3;
    return attr.substring(index, attr.length()-1);
  }
//  
//  private ArrayList<AddressLoc> findLocation(String name, int maxCount)
//  { // Test to find Street for CityPos
//    GeoPoint cityPos = new GeoPoint(48.1203262,14.8752424);
//    String nameLC = name.toLowerCase();
//    ArrayList<AddressLoc> addresses = new ArrayList<AddressLoc>();
//    AllEdgesIterator edges = MapHandler.getMapHandler().getHopper().getGraphHopperStorage().getAllEdges();
//    NodeAccess nodeAccess = MapHandler.getMapHandler().getHopper().getGraphHopperStorage().getNodeAccess();
//
//    Log.i("junjunguo", "Searching for: " + edges.getMaxId() + " Edges ...");
//    int counter = 0;
//    long allCounter = 0;
//    while (edges.next())
//    {
//      if (edges.getName().toLowerCase().contains(nameLC))
//      {
//        double lat = nodeAccess.getLat(edges.getBaseNode());
//        double lon = nodeAccess.getLon(edges.getBaseNode());
//        double distance = NaviEngine.fastDistance(lat,lon,cityPos.getLatitude(), cityPos.getLongitude());
//        if (distance<0.005)
//          counter++;
//      }
//      if (allCounter%100000==0) {Log.i("junjunguo", "Searching: " + allCounter * 100.0 / edges.getMaxId() + "%");}
//      allCounter++;
//    }
//    Log.i("junjunguo", "Finish searching found=" + counter + " all=" + allCounter + " allArr=" + edges.getMaxId());
//    return addresses;    
//  }
//  
//  private ArrayList<AddressLoc> findLocationOri(String name, int maxCount)
//  {
//    org.osmdroid.location.GeocoderNominatim.isPresent();
//    String nameLC = name.toLowerCase();
//    ArrayList<AddressLoc> addresses = new ArrayList<AddressLoc>();
//    AllEdgesIterator edges = MapHandler.getMapHandler().getHopper().getGraphHopperStorage().getAllEdges();
//    NodeAccess nodeAccess = MapHandler.getMapHandler().getHopper().getGraphHopperStorage().getNodeAccess();
//    
//    while (edges.next())
//    {
//      if (edges.getName().toLowerCase().contains(nameLC))
//      {
//        AddressLoc a = new AddressLoc();
//        a.street = edges.getName();
//        double lat = nodeAccess.getLat(edges.getBaseNode());
//        double lon = nodeAccess.getLon(edges.getBaseNode());
//        a.location = new GeoPoint(lat, lon);
//        if (addToAddressList(addresses, a))
//        {
//          a.city = findNextCity(a.location);
//          Log.i("junjunguo", "Found Address: city=\"" + a.city + "\" street=" + a.street);
//        }
//      }
//      if (addresses.size() >= maxCount) { break; }
//    }
////    org.osmdroid.location.GeocoderNominatim geocoder = new org.osmdroid.location.GeocoderNominatim(null);
//    return addresses;
//  }
//  
//  /** Dont add all street-segments, but new streets.
//   * @return true if added, false if street-segment is next to existing one. **/
//  private boolean addToAddressList(ArrayList<AddressLoc> addresses, AddressLoc a)
//  {
//    for (AddressLoc curAddress : addresses)
//    {
//      if (curAddress.street.equals(a.street))
//      {
//        double distance = a.location.distance(curAddress.location);
//        if (distance<0.005) { return false; }
//      }
//    }
//    addresses.add(a);
//    return true;
//  }
//
//  private String findNextCity(GeoPoint p)
//  {
//    int touchRadiusDist = 1024;
//    int zoomLevel = 12;
//    float touchRadius = touchRadiusDist * CanvasAdapter.getScale();
//    long mapSize = MercatorProjection.getMapSize((byte) zoomLevel);
//    double pixelX = MercatorProjection.longitudeToPixelX(p.getLongitude(), mapSize);
//    double pixelY = MercatorProjection.latitudeToPixelY(p.getLatitude(), mapSize);
//    int tileXMin = MercatorProjection.pixelXToTileX(pixelX - touchRadius, (byte) zoomLevel);
//    int tileXMax = MercatorProjection.pixelXToTileX(pixelX + touchRadius, (byte) zoomLevel);
//    int tileYMin = MercatorProjection.pixelYToTileY(pixelY - touchRadius, (byte) zoomLevel);
//    int tileYMax = MercatorProjection.pixelYToTileY(pixelY + touchRadius, (byte) zoomLevel);
//    Tile upperLeft = new Tile(tileXMin, tileYMin, (byte) zoomLevel);
//    Tile lowerRight = new Tile(tileXMax, tileYMax, (byte) zoomLevel);
//    MapDatabase dataSource = (MapDatabase) MapHandler.getMapHandler().getTileSource().getDataSource();
//    MapReadResult mapReadResult = dataSource.readLabels(upperLeft, lowerRight);
//    
//    double nearestDist = Double.MAX_VALUE;
//    String nearestCity = "";
//    for (PointOfInterest pointOfInterest : mapReadResult.pointOfInterests)
//    {
//      List<Tag> tags = pointOfInterest.tags;
//      boolean isCity = false;
//      String tagName = null;
//      for (Tag tag : tags)
//      {
//        if (tag.key.equals("place") && tag.value.equals("suburb")) { isCity = true; }
//        else if (tag.key.equals("name")) { tagName = tag.value; }
//      }
//      if (isCity && tagName!=null)
//      {
//        double thisDist = pointOfInterest.position.distance(p);
//        if (thisDist < nearestDist)
//        {
//          nearestDist = thisDist;
//          nearestCity = tagName;
//        }
//      }
//    }
//    return nearestCity;
//  }
//  
//  /** Find POIs next to screen touch.
//   * Example from org/oscim/android/test/ReverseGeocodeActivity.java
//   * @param p From mapviewport().fromScreenPoint(px, py); **/
//  private String findPointsOfInterest(GeoPoint p, int touchRadiusDist, int zoomLevel)
//  {
// // Read all labeled POI and ways for the area covered by the tiles under touch
////    float touchRadius = 16 * CanvasAdapter.getScale();
//    float touchRadius = touchRadiusDist * CanvasAdapter.getScale();
//    long mapSize = MercatorProjection.getMapSize((byte) zoomLevel);
//    double pixelX = MercatorProjection.longitudeToPixelX(p.getLongitude(), mapSize);
//    double pixelY = MercatorProjection.latitudeToPixelY(p.getLatitude(), mapSize);
//    int tileXMin = MercatorProjection.pixelXToTileX(pixelX - touchRadius, (byte) zoomLevel);
//    int tileXMax = MercatorProjection.pixelXToTileX(pixelX + touchRadius, (byte) zoomLevel);
//    int tileYMin = MercatorProjection.pixelYToTileY(pixelY - touchRadius, (byte) zoomLevel);
//    int tileYMax = MercatorProjection.pixelYToTileY(pixelY + touchRadius, (byte) zoomLevel);
//
//    
////    int tileXMin = MercatorProjection.longitudeToTileX(p.getLongitude() - touchRadius,(byte) zoomLevel);
////    int tileXMax = MercatorProjection.longitudeToTileX(p.getLongitude() + touchRadius,(byte) zoomLevel);
////    int tileYMin = MercatorProjection.latitudeToTileY(p.getLatitude() - touchRadius,(byte) zoomLevel);
////    int tileYMax = MercatorProjection.latitudeToTileY(p.getLatitude() + touchRadius,(byte) zoomLevel);
//    Tile upperLeft = new Tile(tileXMin, tileYMin, (byte) zoomLevel);
//    Tile lowerRight = new Tile(tileXMax, tileYMax, (byte) zoomLevel);
//    MapDatabase dataSource = (MapDatabase) MapHandler.getMapHandler().getTileSource().getDataSource();
//    MapReadResult mapReadResult = dataSource.readLabels(upperLeft, lowerRight);
//    String result = fromMap(mapReadResult);
//    mapReadResult = dataSource.readMapData(upperLeft, lowerRight);
//    return result + "\n\n----Now All ----\n\n" + fromMap(mapReadResult);
//  }
//  
//  String fromMap(MapReadResult mapReadResult)
//  {
//    StringBuilder sb = new StringBuilder();
//    
//    // Filter POI
//    sb.append("*** POI ***");
//    for (PointOfInterest pointOfInterest : mapReadResult.pointOfInterests) {
////        Point layerXY = new Point();
////        mMap.viewport().toScreenPoint(pointOfInterest.position, false, layerXY);
////        Point tapXY = new Point(e.getX(), e.getY());
////        if (layerXY.distance(tapXY) > touchRadius) {
////            continue;
////        }
//        sb.append("\n");
//        List<Tag> tags = pointOfInterest.tags;
//        for (Tag tag : tags) {
//            sb.append("\n").append(tag.key).append("=").append(tag.value);
//        }
//    }
//
//    // Filter ways
//    sb.append("\n\n").append("*** WAYS ***");
//    for (Way way : mapReadResult.ways) {
////        if (way.geometryType != GeometryBuffer.GeometryType.POLY
////                || !GeoPointUtils.contains(way.geoPoints[0], p)) {
////            continue;
////        }
//        sb.append("\n");
//        List<Tag> tags = way.tags;
//        for (Tag tag : tags) {
//            sb.append("\n").append(tag.key).append("=").append(tag.value);
//        }
//    }
//    return sb.toString();
//  }
  private void log(String str)
  {
    Log.i(GeocoderLocal.class.getName(), str);
  }
    
  private void logUser(String str)
  {
    Log.i(GeocoderLocal.class.getName(), str);
    Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
  }
}
