package com.junjunguo.pocketmaps.model.listeners;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on September 01, 2015.
 */
public interface TrackingListener {
    /**
     * @param distance new distance passed
     */
    void updateDistance(Float distance);

    /**
     * @param avgSpeed new avg speed
     */
    void updateAvgSpeed(Float avgSpeed);

    /**
     * @param maxSpeed new max speed
     */
    void updateMaxSpeed(Float maxSpeed);
}
