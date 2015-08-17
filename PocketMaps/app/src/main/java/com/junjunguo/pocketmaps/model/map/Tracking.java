package com.junjunguo.pocketmaps.model.map;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.junjunguo.pocketmaps.model.database.DBtrackingPoints;
import com.junjunguo.pocketmaps.model.util.GenerateGPX;
import com.junjunguo.pocketmaps.model.util.Variable;

import java.io.File;
import java.io.IOException;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on August 16, 2015.
 */
public class Tracking {
    private static Tracking tracking;

    private Tracking() {
        isOnTracking = false;
        dBtrackingPoints = new DBtrackingPoints(MapHandler.getMapHandler().getActivity().getApplicationContext());
    }

    public static Tracking getTracking() {
        if (tracking == null) {
            tracking = new Tracking();
        }
        return tracking;
    }

    private boolean isOnTracking;
    private DBtrackingPoints dBtrackingPoints;

    /**
     * stop Tracking: set everything to null
     */
    public void stopTracking() {
        init();
    }

    /**
     * init and start tracking
     *
     * @param context application context
     */
    public void startTracking(Context context) {
        init();
        MapHandler.getMapHandler().startTrack();
        isOnTracking = true;
    }


    public void init() {
        int rows = dBtrackingPoints.deleteAllRows();
        log("deleted location counts: " + rows);
        isOnTracking = false;
    }

    /**
     * @return true if is on tracking
     */
    public boolean isTracking() {
        return isOnTracking;
    }

    /**
     * add a location point to points list
     *
     * @param location
     */
    public void addPoint(Location location) {
        dBtrackingPoints.addLocation(location);
    }

    public void saveAsGPX(final String name) {
                    log("save as gps");
        new AsyncTask() {
            protected Object doInBackground(Object[] params) {
                try {
                    new GenerateGPX().writeGpxFile(name, dBtrackingPoints,
                            new File(Variable.getVariable().getTrackingFolder().getAbsolutePath(), name + ".gpx"));
                    Tracking.getTracking().stopTracking();
                    dBtrackingPoints.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    /**
     * send message to logcat
     *
     * @param str
     */
    private void log(String str) {
        Log.i(this.getClass().getSimpleName(), "-----------------" + str);
    }
}
