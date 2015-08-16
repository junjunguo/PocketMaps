package com.junjunguo.pocketmaps.model.map;

/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on August 16, 2015.
 */
public class Tracking {
    private static Tracking tracking;

    private Tracking() {
        isOnTracking = false;
    }

    public static Tracking getTracking() {
        if (tracking == null) {
            tracking = new Tracking();
        }
        return tracking;
    }

    private boolean isOnTracking;

    /**
     * stop Tracking: set everything to null
     */
    public void stopTracking() {
        isOnTracking = false;
    }

    /**
     * init and start tracking
     */
    public void startTracking() {
        MapHandler.getMapHandler().startTrack();
        isOnTracking = true;
    }

    public boolean isTracking() {
        return isOnTracking;
    }
}
