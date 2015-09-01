package com.junjunguo.pocketmaps.model.map;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.junjunguo.pocketmaps.controller.AppSettings;
import com.junjunguo.pocketmaps.model.database.DBtrackingPoints;
import com.junjunguo.pocketmaps.model.listeners.TrackingListener;
import com.junjunguo.pocketmaps.model.util.GenerateGPX;
import com.junjunguo.pocketmaps.model.util.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on August 16, 2015.
 */
public class Tracking {
    private static Tracking tracking;
    private float avgSpeed, maxSpeed, distance;
    private Location startLocation;
    private long timeStart, newPointTime;

    private boolean isOnTracking;
    private DBtrackingPoints dBtrackingPoints;
    private List<TrackingListener> listeners;

    private Tracking() {
        isOnTracking = false;
        dBtrackingPoints = new DBtrackingPoints(MapHandler.getMapHandler().getActivity().getApplicationContext());
        listeners = new ArrayList<>();
    }

    public static Tracking getTracking() {
        if (tracking == null) {
            tracking = new Tracking();
        }
        return tracking;
    }

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
        maxSpeed = 0;
        distance = 0;
        timeStart = System.currentTimeMillis();
        newPointTime = 0L;
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

    public void init() {
        dBtrackingPoints.open();
        dBtrackingPoints.deleteAllRows();
        dBtrackingPoints.close();
        isOnTracking = false;
    }

    /**
     * @return average speed
     */
    public float getAvgSpeed() {
        return avgSpeed;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * @return total distance through
     */
    public float getDistance() {
        return distance;
    }

    /**
     * @return tracking start time in milliseconds
     */
    public long getTimeStart() {
        return timeStart;
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
        updateMaxSpeed(location);
        startLocation = location;
        newPointTime = System.currentTimeMillis();
    }


    /**
     * update distance and speed
     *
     * @param location
     */
    private void updateDistance(Location location) {
        if (startLocation != null) {
            distance += startLocation.distanceTo(location);
            avgSpeed = (distance) / (getDurationInMilliS() / (60 * 60));
            if (AppSettings.getAppSettings().getAppSettingsVP().getVisibility() == View.VISIBLE) {
                AppSettings.getAppSettings().updateAnalytics(avgSpeed, distance);
            }
            broadcast(avgSpeed, null, distance);
        }
    }

    /**
     * @return duration in milli second
     */
    public long getDurationInMilliS() {
        return (System.currentTimeMillis() - timeStart);
    }

    /**
     * @return duration in hours
     */
    public long getDurationInHours() {
        return getDurationInMilliS() / (60 * 60 * 1000);
    }

    private void updateMaxSpeed(Location location) {
        if (newPointTime != 0L) {
            float speed =
                    (startLocation.distanceTo(location)) / ((System.currentTimeMillis() - newPointTime) / (60 * 60));
            if (maxSpeed < speed && speed < (maxSpeed + 9) * 10) {
                maxSpeed = speed;
                broadcast(null, maxSpeed, null);
            }
        }
    }

    /**
     * set null if do not need to update
     *
     * @param avgSpeed
     * @param maxSpeed
     * @param distance
     */
    private void broadcast(Float avgSpeed, Float maxSpeed, Float distance) {
        for (TrackingListener tl : listeners) {
            if (avgSpeed != null) {
                tl.updateAvgSpeed(avgSpeed);
            }
            if (maxSpeed != null) {
                tl.updateMaxSpeed(maxSpeed);
            }
            if (distance != null) {
                tl.updateDistance(distance);
            }
        }
    }

    /**
     * remove from listeners list
     *
     * @param listener
     */
    public void removeListener(TrackingListener listener) {
        listeners.remove(listener);
    }

    /**
     * add to listeners list
     *
     * @param listener
     */
    public void addListener(TrackingListener listener) {
        listeners.add(listener);
    }

    /**
     * export location data from database to GPX file
     *
     * @param name folder name
     */
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
