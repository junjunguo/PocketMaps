package com.junjunguo.pocketmaps.model.listeners;

import org.mapsforge.core.model.LatLong;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 26, 2015.
 */
public interface MapHandlerListener {
    /**
     * when use press on the screen to get a location form map
     *
     * @param latLong
     */
    void onPressLocation(LatLong latLong);

    /**
     * calculate path calculating (running) true NOT running or finished false
     *
     * @param shortestPathRunning
     */
    void pathCalculating(boolean shortestPathRunning);
}
