package com.junjunguo.pocketmaps.model.map;

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
     * stop Tracking: is on tracking false
     */
    public void stopTracking() {
        isOnTracking = false;
    }

    /**
     * init and start tracking
     */
    public void startTracking() {
        init();
        MapHandler.getMapHandler().startTrack();
        isOnTracking = true;
    }


    public void init() {
        dBtrackingPoints.open();
        //        int rows =
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
