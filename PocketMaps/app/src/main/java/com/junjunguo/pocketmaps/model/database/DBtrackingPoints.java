package com.junjunguo.pocketmaps.model.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.junjunguo.pocketmaps.model.map.Tracking;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on August 17, 2015.
 */
public class DBtrackingPoints {
    private SQLiteDatabase database;
    private DBhelper dbHelper;

    public DBtrackingPoints(Context context) {
        dbHelper = new DBhelper(context);
        open();
    }

    /**
     * open database for read and write
     */
    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * close any database object
     */
    public void close() {
        dbHelper.close();
        database.close();
    }

    /**
     * delete all rows from database
     *
     * @return deleted row count
     */
    public int deleteAllRows() {
        return database.delete(dbHelper.TABLE_NAME, "1", null);
    }

    /**
     * insert a text report item to the location database table
     *
     * @param location
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long addLocation(Location location) {
        ContentValues cv = new ContentValues();
        cv.put(dbHelper.COLUMN_DATETIME, location.getTime());
        cv.put(dbHelper.COLUMN_LONGITUDE, location.getLongitude());
        cv.put(dbHelper.COLUMN_LATITUDE, location.getLatitude());
        cv.put(dbHelper.COLUMN_ALTITUDE, location.getAltitude());

        long result = database.insert(dbHelper.TABLE_NAME, null, cv);
        if (result >= 0) {
            // correct
        }
        return result;
    }


    /**
     * @return total row count of the table
     */
    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + dbHelper.TABLE_NAME;
        Cursor cursor = database.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public DBhelper getDbHelper() {
        return dbHelper;
    }

    /**
     * @return Cursor
     */
    public Cursor getCursor() {
        Cursor cursor =
                database.query(dbHelper.TABLE_NAME, null, null, null, null, null, dbHelper.COLUMN_DATETIME + " ASC");
        cursor.moveToFirst();
        return cursor;
    }

    /**
     * DataPoint (double x, double y) x = time in hours, y = increased distance in km
     * <p/>
     * DataPoint (double x, double y) x = time, y = speed during time interval
     *
     * @return DataPoint with time and increased distance {speedPoints, distancePoints}
     */
    public DataPoint[][] getGraphSeries() {
        int rowCount = getRowCount();
        if (rowCount > 2) {
            // start record time
            long startTime = Tracking.getTracking().getTimeStart();
            // start point time -- end point time (time between to locations)
            long startPointTime = 0;
            double disIncreased = 0;
            double timeDuration = 0;
            Location startLocation = null;
            DataPoint[] distancePoints = new DataPoint[rowCount];
            DataPoint[] velocityPoints = new DataPoint[rowCount];
            distancePoints[0] = new DataPoint(0, 0);
            velocityPoints[0] = new DataPoint(0, 0);
            Cursor cursor = database.query(dbHelper.TABLE_NAME, null, null, null, null, null,
                    dbHelper.COLUMN_DATETIME + " ASC");

            cursor.moveToFirst();
            int i = 1;
            while (!cursor.isAfterLast()) {
                double longitude = cursor.getDouble(cursor.getColumnIndex(dbHelper.COLUMN_LONGITUDE));
                double latitude = cursor.getDouble(cursor.getColumnIndex(dbHelper.COLUMN_LATITUDE));
                long time = cursor.getLong(cursor.getColumnIndex(dbHelper.COLUMN_DATETIME));
                //                log("db location point time: " + time + " start time: " + startTime);
                timeDuration = (double) (time - startTime) / (1000.0 * 60 * 60);    //hours
                //                log("increased time: " + timeDuration);
                if (startLocation == null) {
                    startLocation = new Location("");
                    startLocation.setLatitude(latitude);
                    startLocation.setLongitude(longitude);
                    startPointTime = time;
                } else {
                    Location newLocation = new Location("");
                    newLocation.setLatitude(latitude);
                    newLocation.setLongitude(longitude);
                    double dis = (double) startLocation.distanceTo(newLocation) / 1000.0; // in km
                    disIncreased += dis;
                    distancePoints[i] = new DataPoint(timeDuration, disIncreased);
                    velocityPoints[i] =
                            new DataPoint(timeDuration, dis / ((time - startPointTime) / (1000.0 * 60 * 60)));
                    startLocation = newLocation;
                    startPointTime = time;
                    i++;
                }
                cursor.moveToNext();
            }
            cursor.close();
            return new DataPoint[][]{velocityPoints, distancePoints};
        }
        return null;
    }

    private void log(String str) {
        Log.i(this.getClass().getSimpleName(), "-----------------" + str);
    }
}
