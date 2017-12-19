package com.junjunguo.pocketmaps.geocoding;

import java.util.ArrayList;

import org.oscim.core.GeoPoint;

import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.storage.NodeAccess;
import com.junjunguo.pocketmaps.map.MapHandler;

public class GeocoderLocal
{

  public GeocoderLocal()
  {
  }
  
  public ArrayList<Address> findLocation(String name, int maxCount)
  {
    ArrayList<Address> addresses = new ArrayList<Address>();
    AllEdgesIterator edges = MapHandler.getMapHandler().getHopper().getGraphHopperStorage().getAllEdges();
    NodeAccess nodeAccess = MapHandler.getMapHandler().getHopper().getGraphHopperStorage().getNodeAccess();
    
    while (edges.next())
    {
      if (name.equals(edges.getName()))
      {
        Address a = new Address();
        a.street = edges.getName();
        double lat = nodeAccess.getLat(edges.getBaseNode());
        double lon = nodeAccess.getLon(edges.getBaseNode());
        a.location = new GeoPoint(lat, lon);
        addresses.add(a);
      }
      if (addresses.size() >= maxCount) { break; }
    }
    return addresses;
  }
}
