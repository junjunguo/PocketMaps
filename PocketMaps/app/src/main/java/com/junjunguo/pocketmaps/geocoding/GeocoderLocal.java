package com.junjunguo.pocketmaps.geocoding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.graphhopper.routing.util.AllEdgesIterator;
import com.junjunguo.pocketmaps.map.MapHandler;
import com.junjunguo.pocketmaps.model.listeners.OnProgressListener;
import com.junjunguo.pocketmaps.util.GeoMath;
import com.junjunguo.pocketmaps.util.Variable;

import android.content.Context;
import android.location.Address;
import android.util.Log;

public class GeocoderLocal
{
  public final static int ADDR_TYPE_FOUND = 0;
  public final static int ADDR_TYPE_CITY = 1;
  public final static int ADDR_TYPE_CITY_EN = 2;
  public final static int ADDR_TYPE_POSTCODE = 3;
  public final static int ADDR_TYPE_STREET = 4;
  public final static int ADDR_TYPE_COUNTRY = 5;
  public final static int BIT_MULT = 1;
  public final static int BIT_EXPL = 2;
  public final static int BIT_CITY = 4;
  public final static int BIT_STREET = 8;

  private Locale locale;
  private boolean bMultiMatchOnly;
  private boolean bExplicitSearch;
  private boolean bStreetNodes;
  private boolean bCityNodes;
  
  public GeocoderLocal(Context context, Locale locale)
  {
    this.locale = locale;
  }
  
  public static boolean isPresent() { return true; }
  
  
  public List<Address> getFromLocationName(String searchS, int maxCount, OnProgressListener progressListener) throws IOException
  {
    getSettings();
    progressListener.onProgress(2);
    ArrayList<Address> addrList = new ArrayList<Address>();
    if (bCityNodes && !bMultiMatchOnly)
    {
      ArrayList<Address> nodes = findCity(searchS, maxCount);
      if (nodes == null) { return null; }
      addrList.addAll(nodes);
    }
    progressListener.onProgress(5);
    if (addrList.size() < maxCount && bStreetNodes)
    {
      ArrayList<Address> nodes = searchNodes(searchS, maxCount - addrList.size(), progressListener);
      if (nodes == null) { return null; }
      addrList.addAll(nodes);
    }
    return addrList;
  }

  private void getSettings()
  {
    int bits = Variable.getVariable().getOfflineSearchBits();
    bMultiMatchOnly = (bits & BIT_MULT) > 0;
    bExplicitSearch = (bits & BIT_EXPL) > 0;
    bCityNodes = (bits & BIT_CITY) > 0;
    bStreetNodes = (bits & BIT_STREET) > 0;
  }
  
  /** For more information of street-matches. **/
  private String findNearestCity(double lat, double lon)
  {
    String mapsPath = Variable.getVariable().getMapsFolder().getAbsolutePath();
    mapsPath = new File(mapsPath, Variable.getVariable().getCountry() + "-gh").getPath();
    mapsPath = new File(mapsPath, "city_nodes.txt").getPath();
    String nearestName = null;
    double nearestDist = 0;
    String curName = "";
    double curLat = 0;
    double curLon = 0;
    double curDist = 0;
    try(FileReader r = new FileReader(mapsPath);
        BufferedReader br = new BufferedReader(r))
    {
      String line;
      while ((line = br.readLine())!=null)
      {
        if (GeocoderGlobal.isStopRunningActions()) { return null; }
        if (line.startsWith("name="))
        {
          curName = readString("name", line);
        }
        else if (line.startsWith("name:en="))
        {
          if (curName.isEmpty())
          {
            curName = readString("name:en", line);
          }
        }
        else if (line.startsWith("lat="))
        {
          curLat = readDouble("lat", line);
        }
        else if (line.startsWith("lon="))
        {
          if (curName.isEmpty()) { continue; }
          curLon = readDouble("lon", line);
          curDist = GeoMath.fastDistance(curLat, curLon, lat, lon);
          if (nearestName == null || curDist < nearestDist)
          {
            nearestDist = curDist;
            nearestName = curName;
          }
        }
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return nearestName;
  }
  
  private ArrayList<Address> findCity(String searchS, int maxCount) throws IOException
  {
    ArrayList<Address> result = new ArrayList<Address>();
    CityMatcher cityMatcher = new CityMatcher(searchS, bExplicitSearch);

    String mapsPath = Variable.getVariable().getMapsFolder().getAbsolutePath();
    mapsPath = new File(mapsPath, Variable.getVariable().getCountry() + "-gh").getPath();
    mapsPath = new File(mapsPath, "city_nodes.txt").getPath();
    Address curAddress = new Address(locale);
    try(FileReader r = new FileReader(mapsPath);
        BufferedReader br = new BufferedReader(r))
    {
      String line;
      while ((line = br.readLine())!=null)
      {
        if (GeocoderGlobal.isStopRunningActions()) { return null; }
        if (result.size() >= maxCount) { break; }
        if (line.startsWith("name="))
        {
          curAddress = clearAddress(curAddress);
          String name = readString("name", line);
          curAddress.setAddressLine(ADDR_TYPE_CITY, name);
          boolean isMatching = cityMatcher.isMatching(name, false);
          if (isMatching)
          {
            result.add(curAddress);
            curAddress.setAddressLine(ADDR_TYPE_COUNTRY, Variable.getVariable().getCountry());
            curAddress.setAddressLine(ADDR_TYPE_FOUND, name);
            log("Added address: " + name);
          }
        }
        else if (line.startsWith("name:en="))
        {
          String name = readString("name:en", line);
          curAddress.setAddressLine(ADDR_TYPE_CITY_EN, name);
          if (curAddress.getAddressLine(ADDR_TYPE_COUNTRY) == null)
          { // Is still not attached!
            boolean isMatching = cityMatcher.isMatching(name, false);
            if (isMatching)
            {
              result.add(curAddress);
              curAddress.setAddressLine(ADDR_TYPE_COUNTRY, Variable.getVariable().getCountry());
              curAddress.setAddressLine(ADDR_TYPE_FOUND, name);
              log("Added address: " + name);
            }
          }
        }
        else if (line.startsWith("post="))
        {
          String name = readString("post", line);
          curAddress.setAddressLine(ADDR_TYPE_POSTCODE, name);
          if (curAddress.getAddressLine(ADDR_TYPE_COUNTRY) == null)
          { // Is still not attached!
            boolean isMatching = cityMatcher.isMatching(name, true);
            if (isMatching)
            {
              result.add(curAddress);
              curAddress.setAddressLine(ADDR_TYPE_COUNTRY, Variable.getVariable().getCountry());
              curAddress.setAddressLine(ADDR_TYPE_FOUND, name);
              log("Added address: " + name);
            }
          }
        }
        else if (line.startsWith("lat="))
        {
          curAddress.setLatitude(readDouble("lat", line));
        }
        else if (line.startsWith("lon="))
        {
          curAddress.setLongitude(readDouble("lon", line));
        }
      }
    }
    catch (IOException e)
    {
      throw e;
    }
    return result;
  }
  
  private Address clearAddress(Address curAddress)
  {
    if (curAddress.getAddressLine(ADDR_TYPE_COUNTRY) == null)
    { // Clear this curAddress for reuse!
      curAddress.clearLatitude();
      curAddress.clearLongitude();
      for (int i=10; i>0; i--)
      {
        curAddress.setAddressLine(i, null);
      }
      curAddress.setAddressLine(ADDR_TYPE_COUNTRY, null);
    }
    else { curAddress = new Address(locale); }
    return curAddress;
  }

  private double readDouble(String key, String txt)
  {
    String s = readString(key, txt);
    if (s==null)
    {
      Log.e(GeocoderLocal.class.getName(), "Double for key not found: " + key);
      return 0;
    }
    return Double.parseDouble(s);
  }
  
  private String readString(String key, String txt)
  {
    return txt.substring(key.length()+1);
  }

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
  
  /** Search all edges for matching text. **/
  ArrayList<Address> searchNodes(String txt, int maxMatches, OnProgressListener progressListener)
  {
    txt = txt.toLowerCase();
    ArrayList<Address> addressList = new ArrayList<Address>();
    StreetMatcher streetMatcher = new StreetMatcher(txt, bExplicitSearch);
    CityMatcher cityMatcher = streetMatcher;
    
    AllEdgesIterator edgeList = MapHandler.getMapHandler().getAllEdges();
    if (edgeList == null) { return null; }
    log("SEARCH_EDGE Start ...");
    int counter = 0;
    int lastProgress = 5;
    while (edgeList.next())
    {
      counter++;
      if (GeocoderGlobal.isStopRunningActions()) { return null; }
      if (edgeList.getName().isEmpty()) { continue; }
      if (edgeList.fetchWayGeometry(0).isEmpty()) { continue; }
      int newProgress = (counter*100) / edgeList.length();
      if (newProgress > lastProgress)
      {
        progressListener.onProgress((counter*100) / edgeList.length());
        lastProgress = newProgress;
      }
      if (streetMatcher.isMatching(edgeList.getName(), false))
      {
        log("SEARCH_EDGE Status: " + counter + "/" + edgeList.length());
        boolean b = StreetMatcher.addToList(addressList,
                                            edgeList.getName(),
                                            edgeList.fetchWayGeometry(0).get(0).lat,
                                            edgeList.fetchWayGeometry(0).get(0).lon,
                                            locale);
        if (b)
        {
          String c = findNearestCity(edgeList.fetchWayGeometry(0).get(0).lat, edgeList.fetchWayGeometry(0).get(0).lon);
          if (bMultiMatchOnly && !cityMatcher.isMatching(c, false))
          {
            addressList.remove(addressList.size()-1);
          }
          else
          {
            log("SEARCH_EDGE found=" + edgeList.getName() + " on " + c);
            addressList.get(addressList.size()-1).setAddressLine(ADDR_TYPE_CITY, c);
          }
        }
      }
      if (addressList.size() >= maxMatches) { break; }
    }
    log("SEARCH_EDGE Stop on length=" + addressList.size());
    return addressList;
  }
  
  private void log(String str)
  {
    Log.i(GeocoderLocal.class.getName(), str);
  }

}
