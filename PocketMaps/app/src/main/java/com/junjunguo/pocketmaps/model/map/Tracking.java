package com.junjunguo.pocketmaps.model.map;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.jjoe64.graphview.series.DataPoint;
import com.junjunguo.pocketmaps.controller.AppSettings;
import com.junjunguo.pocketmaps.model.database.DBtrackingPoints;
import com.junjunguo.pocketmaps.model.listeners.TrackingListener;
import com.junjunguo.pocketmaps.model.util.GenerateGPX;
import com.junjunguo.pocketmaps.model.util.Variable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on August 16, 2015.
 */
public class Tracking {
    private static Tracking tracking;
    private double avgSpeed, maxSpeed, distance;
    private Location startLocation;
    private long timeStart;

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
        avgSpeed = 0; // km/h
        maxSpeed = 0; // km/h
        distance = 0; // meter
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

    public void init() {
        dBtrackingPoints.open();
        dBtrackingPoints.deleteAllRows();
        dBtrackingPoints.close();
        isOnTracking = false;
    }

    /**
     * @return average speed
     */
    public double getAvgSpeed() {
        return avgSpeed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    /**
     * @return total distance through in meters
     */
    public double getDistance() {
        return distance;
    }

    /**
     * @return total distance through in km
     */
    public double getDistanceKm() {
        return distance / 1000.0;
    }

    /**
     * @return tracking start time in milliseconds
     */
    public long getTimeStart() {
        return timeStart;
    }

    /**
     * @return total points recorded --> database row count
     */
    public int getTotalPoints() {
        dBtrackingPoints.open();
        int p = dBtrackingPoints.getRowCount();
        dBtrackingPoints.close();
        return p;
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
        updateDisSpeed(location);// update first
        updateMaxSpeed(location);// update after updateDisSpeed
        startLocation = location;
    }

    /**
     * distance DataPoint series  DataPoint (x, y) x = increased time, y = increased distance
     * <p/>
     * Listener will handler the return data
     */
    public void requestDistanceGraphSeries() {
        new AsyncTask<URL, Integer, DataPoint[][]>() {
            protected DataPoint[][] doInBackground(URL... params) {
                try {
                    dBtrackingPoints.open();
                    DataPoint[][] dp = dBtrackingPoints.getGraphSeries();
                    dBtrackingPoints.close();
                    return dp;
                } catch (Exception e) {e.printStackTrace();}
                return null;
            }

            protected void onPostExecute(DataPoint[][] dataPoints) {
                super.onPostExecute(dataPoints);
                broadcast(null, null, null, dataPoints);
            }
        }.execute();
    }

    /**
     * update distance and speed
     *
     * @param location
     */
    private void updateDisSpeed(Location location) {
        if (startLocation != null) {
            float disPoints = startLocation.distanceTo(location);
            distance += disPoints;
            avgSpeed = (distance) / (getDurationInMilliS() / (60 * 60));
            if (AppSettings.getAppSettings().getAppSettingsVP().getVisibility() == View.VISIBLE) {
                AppSettings.getAppSettings().updateAnalytics(avgSpeed, distance);
            }
            broadcast(avgSpeed, null, distance, null);
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
    public double getDurationInHours() {
        return (getDurationInMilliS() / (60 * 60 * 1000.0));
    }

    /**
     * update max speed and broadcast DataPoint for speeds and distances
     *
     * @param location
     */
    private void updateMaxSpeed(Location location) {
        if (startLocation != null) {
            // velocity: m/s
            double velocity =
                    (startLocation.distanceTo(location)) / ((location.getTime() - startLocation.getTime()) / (1000.0));
            double timePoint = (double) (location.getTime() - getTimeStart()) / (1000.0 * 60 * 60); // timePoint hours
            DataPoint speed = new DataPoint(timePoint, velocity);
            DataPoint distance = new DataPoint(timePoint, this.distance);

            broadcast(speed, distance);
            //            TODO: improve noise reduce (Kalman filter)
            // TODO: http://dsp.stackexchange.com/questions/8860/more-on-kalman-filter-for-position-and-velocity
            velocity = velocity * (6 * 6 / 10);// velocity: km/h
            //            if (maxSpeed < velocity && velocity < (maxSpeed + 32) * 10) {
            if (maxSpeed < velocity) {
                maxSpeed = (float) velocity;
                broadcast(null, maxSpeed, null, null);
            }
        }
    }


    private void broadcast(DataPoint speed, DataPoint distance) {
        for (TrackingListener tl : listeners) {
            tl.addDistanceGraphSeriesPoint(speed, distance);
        }
    }

    /**
     * set null if do not need to update
     *
     * @param avgSpeed
     * @param maxSpeed
     * @param distance
     */

    private void broadcast(Double avgSpeed, Double maxSpeed, Double distance, DataPoint[][] dataPoints) {
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
            if (dataPoints != null) {
                tl.updateDistanceGraphSeries(dataPoints);
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
