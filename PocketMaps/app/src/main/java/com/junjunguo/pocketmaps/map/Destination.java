package com.junjunguo.pocketmaps.map;

import org.oscim.core.GeoPoint;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 26, 2015.
 */
public class Destination {
    private GeoPoint startPoint, endPoint;
    private static Destination destination;

    private Destination(GeoPoint startPoint, GeoPoint endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }


    public static Destination getDestination() {
        if (destination == null) {
            destination = new Destination(null, null);
        }
        return destination;
    }

    public GeoPoint getStartPoint() {
        return startPoint;
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

    public void setStartPoint(GeoPoint startPoint) {
        this.startPoint = startPoint;
    }

    public GeoPoint getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(GeoPoint endPoint) {
        this.endPoint = endPoint;
    }
}
