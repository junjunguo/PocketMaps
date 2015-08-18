package com.junjunguo.pocketmaps.model.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

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
}
