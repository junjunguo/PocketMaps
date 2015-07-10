package com.junjunguo.pocketmaps.model.dataType;

import org.mapsforge.core.model.LatLong;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 26, 2015.
 */
public class Destination {
    private LatLong startPoint, endPoint;
    private static Destination destination;

    private Destination(LatLong startPoint, LatLong endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }


    public static Destination getDestination() {
        if (destination == null) {
            destination = new Destination(null, null);
        }
        return destination;
    }

    public LatLong getStartPoint() {
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
            String la = String.valueOf(startPoint.latitude);
            String lo = String.valueOf(startPoint.longitude);
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
            String la = String.valueOf(endPoint.latitude);
            String lo = String.valueOf(endPoint.longitude);
            return la + "," + lo;
        }
        return null;
    }

    public void setStartPoint(LatLong startPoint) {
        this.startPoint = startPoint;
    }

    public LatLong getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(LatLong endPoint) {
        this.endPoint = endPoint;
    }
}
