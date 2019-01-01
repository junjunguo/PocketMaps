package com.junjunguo.pocketmaps.model.listeners;

import com.jjoe64.graphview.series.DataPoint;
import com.junjunguo.pocketmaps.map.Tracking;

/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on September 01, 2015.
 */
public interface TrackingListener {
    /**
     * @param distance new distance passed in m
     */
    void updateDistance(Double distance);

    /**
     * @param avgSpeed new avg speed in km/h
     */
    void updateAvgSpeed(Double avgSpeed);

    /**
     * @param maxSpeed new max speed in km/h
     */
    void updateMaxSpeed(Double maxSpeed);

    /**
     * return data when {@link Tracking#requestDistanceGraphSeries()}  called
     *
     * @param dataPoints
     */
    void updateDistanceGraphSeries(DataPoint[][] dataPoints);

    /**
     * used to set the flag for updating
     */
    void setUpdateNewPoint();
}
