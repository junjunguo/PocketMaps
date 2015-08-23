package com.junjunguo.pocketmaps.model.map;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.junjunguo.pocketmaps.controller.AppSettings;
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
    private float avgSpeed, distance;
    private Location startLocation;
    private long timeStart;

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
     * stop Tracking: is on tracking false
     */
    public void stopTracking() {
        isOnTracking = false;
        intAnalytics();
        AppSettings.getAppSettings().updateAnalytics(0, 0);
    }

    /**
     * set avg speed & distance to 0 & start location = null;
     */
    private void intAnalytics() {
        avgSpeed = 0;
        distance = 0;
        timeStart = System.currentTimeMillis();
        startLocation = null;
    }

    /**
     * init and start tracking
     */
    public void startTracking() {
        init();
        intAnalytics();
        MapHandler.getMapHandler().startTrack();
        isOnTracking = true;
    }


    /**
     * @param f from
     * @param t to
     * @return distance of the two points in meters
     */
    public float getDistance(Location f, Location t) {
        return f.distanceTo(t);
    }

    public void init() {
        dBtrackingPoints.open();
        dBtrackingPoints.deleteAllRows();
        dBtrackingPoints.close();
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
        dBtrackingPoints.open();
        dBtrackingPoints.addLocation(location);
        dBtrackingPoints.close();
        updateDistance(location);
        startLocation = location;
    }

    /**
     * update distance and speed
     *
     * @param location
     */
    private void updateDistance(Location location) {
        if (startLocation != null) {
            distance += getDistance(startLocation, location);
            avgSpeed = (distance) / ((System.currentTimeMillis() - timeStart) / (60 * 60));
            if (AppSettings.getAppSettings().getAppSettingsVP().getVisibility() == View.VISIBLE) {
                AppSettings.getAppSettings().updateAnalytics(avgSpeed, distance);
            }
        }
    }

    public void saveAsGPX(final String name) {
        final File trackFolder = new File(Variable.getVariable().getTrackingFolder().getAbsolutePath());
        trackFolder.mkdirs();
        final File gpxFile = new File(trackFolder, name);
        if (!gpxFile.exists()) {
            try {
                gpxFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        new AsyncTask() {
            protected Object doInBackground(Object[] params) {
                try {
                    new GenerateGPX().writeGpxFile(name, dBtrackingPoints, gpxFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if (gpxFile.exists()) {
                    gpxFile.renameTo(new File(trackFolder, name + ".gpx"));
                }
            }
        }.execute();
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
