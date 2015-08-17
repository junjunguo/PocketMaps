package com.junjunguo.pocketmaps.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on August 17, 2015.
 */
public class DBhelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "pocketmaps.db";
    public static final int DB_VERSION = 1;

    public final String REAL_TYPE = " REAL";
    public final String NUMERIC_TYPE = " NUMERIC";
    public final String COMMA_SEP = ",";

    public final String TABLE_NAME = "tracking";
    public final String COLUMN_DATETIME = "datetime";
    public final String COLUMN_LATITUDE = "latitude";
    public final String COLUMN_LONGITUDE = "longitude";
    public final String COLUMN_ALTITUDE = "altitude";
    public final String PRIMARY_KEY = "PRIMARY KEY (" + COLUMN_DATETIME +
            "," + COLUMN_LONGITUDE + "," + COLUMN_LATITUDE + ")";

    private final String CREATE_TRACK_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_DATETIME + NUMERIC_TYPE + COMMA_SEP +
            COLUMN_LONGITUDE + REAL_TYPE + COMMA_SEP +
            COLUMN_LATITUDE + REAL_TYPE + COMMA_SEP +
            COLUMN_ALTITUDE + REAL_TYPE + COMMA_SEP + PRIMARY_KEY +
            " )";

    public final String DELETE_TRACK_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public DBhelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TRACK_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_TRACK_TABLE);
        onCreate(db);
    }
}