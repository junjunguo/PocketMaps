package com.junjunguo.pocketmaps.model.map;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.graphhopper.util.Helper;
import com.graphhopper.util.ProgressListener;
import com.junjunguo.pocketmaps.model.listeners.MapDownloadListener;
import com.junjunguo.pocketmaps.model.util.MyApp;
import com.junjunguo.pocketmaps.model.dataType.MyMap;
import com.junjunguo.pocketmaps.model.util.Variable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of Pockets Maps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 14, 2015.
 */
public class DownloadFiles {
    private List<MapDownloadListener> mapDownloadListeners;
    private Context context;
    private static DownloadFiles downloadFiles;

    private DownloadFiles() {
        this.context = null;
        this.mapDownloadListeners = new ArrayList<>();
    }

    public static DownloadFiles getDownloader() {
        if (downloadFiles == null) {
            downloadFiles = new DownloadFiles();
        }
        return downloadFiles;
    }

    /**
     * download and unzip map files and save it in  mapsFolder/currentArea-gh/
     *
     * @param mapsFolder  File maps folder
     * @param mapName     area (country) to download
     * @param downloadURL download link
     * @param context     calling activity
     */
    public void downloadMap(final File mapsFolder, final String mapName, final String downloadURL, Context context) {
        this.context = context;
        final File areaFolder = new File(mapsFolder, mapName + "-gh");
        // do not run download
        if (downloadURL == null || areaFolder.exists()) {
            //            loadMap() ?;
            return;
        }
        final long startTime = System.currentTimeMillis();
        new GHAsyncTask<Void, Integer, Object>() {
            protected Object saveDoInBackground(Void... _ignore) throws Exception {
                String localFolder = Helper.pruneFileEnd(AndroidHelper.getFileName(downloadURL));
                localFolder = new File(mapsFolder, localFolder + "-gh").getAbsolutePath();
//                log("downloading & unzipping " + downloadURL + " to " + localFolder);
                AndroidDownloader downloader = new AndroidDownloader();
                downloader.setTimeout(30000);
                downloader.downloadAndUnzip(downloadURL, localFolder, new ProgressListener() {
                    @Override public void update(long val) {
                        publishProgress((int) val);
                    }
                });
                return null;
            }

            protected void onPreExecute() {
                super.onPreExecute();
                broadcastStart();
                Variable.getVariable().setDownloading(true);
            }

            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                broadcastOnUpdate(values[0]);
            }

            protected void onPostExecute(Object _ignore) {
                if (hasError()) {
                    String str = "An error happened while retrieving maps:" + getErrorMessage();
                    log(str, getError());
                    logToast(str);

                    MyApp.tracker().send(new HitBuilders.ExceptionBuilder().setDescription("DownloadFiles-Download " +
                            "map " + getErrorMessage()).setFatal(false).build());
                } else {
                    // load map to local select list when finish downloading ?
                    long endTime = System.currentTimeMillis();
                    log("download finished - time used: " + (endTime - startTime) / 1000 + " s");
                    MyApp.tracker().send(new HitBuilders.TimingBuilder().setCategory("DownloadMap")
                            .setValue((endTime - startTime) / 1000).setVariable("s").setLabel(mapName).build());
                }
                broadcastFinished(mapName);
            }
        }.execute();
    }

    /**
     * add to broadcast list
     *
     * @param listener
     */
    public void addListener(MapDownloadListener listener) {
        this.mapDownloadListeners.add(listener);
        log(mapDownloadListeners.toString());
    }

    /**
     * remove listener from broadcast list
     *
     * @param listener
     */
    public void removeListener(MapDownloadListener listener) {
        this.mapDownloadListeners.remove(listener);
    }

    /**
     * broadcast download finished
     *
     * @param mapName
     */
    private void broadcastFinished(String mapName) {
        Variable.getVariable().addRecentDownloadedMap(new MyMap(mapName));
        Variable.getVariable().setDownloading(false);
        for (MapDownloadListener listener : mapDownloadListeners) {
            //            log("download file finished - " + listener.getClass().getSimpleName());
            listener.downloadFinished();
        }
    }

    /**
     * broadcast download start
     */
    private void broadcastStart() {
        for (MapDownloadListener listener : mapDownloadListeners) {
            listener.downloadStart();
        }
    }

    /**
     * broadcast download start
     *
     * @param value
     */
    private void broadcastOnUpdate(Integer value) {
        for (MapDownloadListener listener : mapDownloadListeners) {
            listener.progressBarOnUpdate(value);
        }
    }

    /**
     * send message to logcat
     *
     * @param str
     */
    private void log(String str) {
        Log.i(this.getClass().getSimpleName(), str);
    }

    private void log(String str, Throwable t) {
        Log.i(this.getClass().getSimpleName(), str, t);
    }

    /**
     * send message to logcat and Toast it on screen
     *
     * @param str: message
     */
    private void logToast(String str) {
        log(str);
        Toast.makeText(context, str, Toast.LENGTH_LONG).show();
    }
}
