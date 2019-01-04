package com.junjunguo.pocketmaps.map;

import org.oscim.core.GeoPoint;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 26, 2015.
 */
public class Destination {
    private GeoPoint startPoint, endPoint;
    private String startPointName, endPointName;
    private static Destination destination = new Destination();

    private Destination() {}

    public static Destination getDestination() {
        return destination;
    }

    /**
     * @return a string of la: latitude, lo: longitude
     */
    public String getStartPointToString() {
        if (startPoint != null) {
            //            String la = "Lat:" + String.valueOf(startPoint.latitude);
            //            la = la.length() > 12 ? la.substring(0, 12) : la;
            //            String lo = "Lon:" + String.valueOf(startPoint.longitude);
            //            lo = lo.length() > 12 ? lo.substring(0, 12) : lo;
            String la = String.valueOf(startPoint.getLatitude());
            String lo = String.valueOf(startPoint.getLongitude());
            return la + "," + lo;
        }
        return null;
    }

    /**
     * @return a string of la: latitude, lo: longitude
     */
    public String getEndPointToString() {
        if (endPoint != null) {
            //            String la = "Lat:" + String.valueOf(endPoint.latitude);
            //            la = la.length() > 12 ? la.substring(0, 12) : la;
            //            String lo = "Lon:" + String.valueOf(endPoint.longitude);
            //            lo = lo.length() > 12 ? lo.substring(0, 12) : lo;
            String la = String.valueOf(endPoint.getLatitude());
            String lo = String.valueOf(endPoint.getLongitude());
            return la + "," + lo;
        }
        return null;
    }

    public GeoPoint getStartPoint() {
        return startPoint;
    }
    
    public String getStartPointName() {
      return startPointName;
    }

    public void setStartPoint(GeoPoint startPoint, String startPointName) {
        this.startPoint = startPoint;
        this.startPointName = startPointName;
    }

    public GeoPoint getEndPoint() {
        return endPoint;
    }
    
    public String getEndPointName() {
      return endPointName;
    }

    public void setEndPoint(GeoPoint endPoint, String endPointName) {
        this.endPoint = endPoint;
        this.endPointName = endPointName;
    }
}
