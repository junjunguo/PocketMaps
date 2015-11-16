package com.junjunguo.pocketmaps.model.util;

import android.database.Cursor;
import android.util.Log;

import com.junjunguo.pocketmaps.model.database.DBhelper;
import com.junjunguo.pocketmaps.model.database.DBtrackingPoints;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on August 17, 2015.
 */
public class GenerateGPX {

    /**
     * XML header.
     */
    private final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

    /**
     * GPX opening tag
     */
    private final String TAG_GPX =
            "<gpx" + " version=\"1.1\"" + " creator=\"PocketMaps by JunjunGuo.com - http://junjunguo.com/PocketMaps/\"" +
                    " xmlns=\"http://www.topografix.com/GPX/1/1\"" +
                    " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                    " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx" +
                    ".xsd \">";

    /**
     * Date format for a point timestamp.
     */
    private final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Writes the GPX file
     *
     * @param trackName Name of the GPX track (metadata)
     * @param db        database.
     * @param gpxFile   Target GPX file
     * @throws IOException
     */
    public void writeGpxFile(String trackName, DBtrackingPoints db, File gpxFile) throws IOException {

        String METADATA = "  <metadata>\n" +
                "    <link href=\"http://JunjunGuo.com/PocketMaps\">\n" +
                "      <text>Pocket Maps: Free offline maps with routing functions and more</text>\n" +
                "    </link>\n" +
                "    <time>" + DF.format(System.currentTimeMillis()) + "</time>\n" +
                "  </metadata>";
        if (!gpxFile.exists()) {
            gpxFile.createNewFile();
        }
        FileWriter fw = new FileWriter(gpxFile);

        fw.write(XML_HEADER + "\n");
        fw.write(TAG_GPX + "\n");
        fw.write(METADATA + "\n");
        writeTrackPoints(trackName, fw, db);
        fw.write("</gpx>");
        fw.close();
    }

    /**
     * Iterates on track points and write them.
     *
     * @param trackName Name of the track (metadata).
     * @param fw        Writer to the target file.
     * @param db        database
     * @throws IOException
     */
    public void writeTrackPoints(String trackName, FileWriter fw, DBtrackingPoints db) throws IOException {
        db.open();
        Cursor c = db.getCursor();
        DBhelper dBhelper = db.getDbHelper();
        fw.write("\t" + "<trk>" + "\n");
        fw.write("\t\t" + "<name>" + "PocketMaps GPS track log" + "</name>" + "\n");
        fw.write("\t\t" + "<trkseg>" + "\n");
        while (!c.isAfterLast()) {
            StringBuffer out = new StringBuffer();
            out.append("\t\t\t" + "<trkpt lat=\"" + c.getDouble(c.getColumnIndex(dBhelper.COLUMN_LATITUDE)) + "\" " +
                    "lon=\"" + c.getDouble(c.getColumnIndex(dBhelper.COLUMN_LONGITUDE)) + "\">");
            out.append("<ele>" + c.getDouble(c.getColumnIndex(dBhelper.COLUMN_ALTITUDE)) + "</ele>");
            out.append("<time>" +
                    DF.format(new Date(c.getLong(c.getColumnIndex(dBhelper.COLUMN_DATETIME)))) +
                    "</time>");

            out.append("</trkpt>" + "\n");
            fw.write(out.toString());

            c.moveToNext();
        }
        db.close();
        fw.write("\t\t" + "</trkseg>" + "\n");
        fw.write("\t" + "</trk>" + "\n");
    }

    /**
     * send message to logcat
     *
     * @param str
     */
    private void log(String str) {
        Log.i(this.getClass().getSimpleName(), "---GPX--- " + str);
    }

}
