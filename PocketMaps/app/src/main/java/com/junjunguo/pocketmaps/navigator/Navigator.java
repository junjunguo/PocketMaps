package com.junjunguo.pocketmaps.navigator;

import org.oscim.core.GeoPoint;

import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.QueryResult;
import com.junjunguo.pocketmaps.map.MapHandler;

import android.location.Location;

public class Navigator
{
  private static final int BEST_NAVI_ZOOM = 17;
  private static Navigator instance;
  private Location pos;
  private boolean active = false;
  private GeoPoint startP;
  private GeoPoint endP;

  public static Navigator getNavigator()
  {
    if (instance == null) { instance = new Navigator(); }
    return instance;
  }
  
  public boolean isNavigating()
  {
    return active;
  }
  
  public void setNavigating(boolean active, GeoPoint startP, GeoPoint endP)
  {
    this.active = active;
    this.startP = startP;
    this.endP = endP;
  }
  
  private GeoPoint findClosest(GeoPoint fromPos)
  {
    QueryResult pos = MapHandler.getMapHandler().getHopper().getLocationIndex().findClosest(fromPos.getLatitude(), fromPos.getLongitude(), null);
    int n = pos.getClosestEdge().getBaseNode();
    NodeAccess nodeAccess = MapHandler.getMapHandler().getHopper().getGraphHopperStorage().getNodeAccess();
    GeoPoint gp = new GeoPoint(nodeAccess.getLat(n), nodeAccess.getLon(n));
    return gp;
  }
  
  public void updatePosition(Location pos)
  {
    this.pos = pos;
    pos.getBearing(); // 0-360
  }
}
