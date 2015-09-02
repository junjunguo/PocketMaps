package com.junjunguo.pocketmaps.model.listeners;

import com.jjoe64.graphview.series.DataPoint;
import com.junjunguo.pocketmaps.model.map.Tracking;

/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on September 01, 2015.
 */
public interface TrackingListener {
    /**
     * @param distance new distance passed
     */
    void updateDistance(Double distance);

    /**
     * @param avgSpeed new avg speed
     */
    void updateAvgSpeed(Double avgSpeed);

    /**
     * @param maxSpeed new max speed
     */
    void updateMaxSpeed(Double maxSpeed);

    /**
     * return data when {@link Tracking#requestDistanceGraphSeries()}  called
     *
     * @param dataPoints
     */
    void updateDistanceGraphSeries(DataPoint[][] dataPoints);

    /**
     * used to add new speed and distance DataPoint to DistanceGraphSeries
     *
     * @param speed
     * @param distance
     */
    void addDistanceGraphSeriesPoint(DataPoint speed, DataPoint distance);
}
