package com.junjunguo.pocketmaps.model.map;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.graphhopper.util.Helper;
import com.graphhopper.util.ProgressListener;
import com.junjunguo.pocketmaps.model.util.MapDownloadListener;
import com.junjunguo.pocketmaps.model.util.MyApp;
import com.junjunguo.pocketmaps.model.util.MyDownloadAdapter;
import com.junjunguo.pocketmaps.model.util.MyMap;
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
     * @param mapsFolder        File maps folder
     * @param currentArea       area (country) to download
     * @param downloadURL       download link
     * @param context           calling activity
     * @param pb
     * @param itemPosition
     * @param myDownloadAdapter
     */
    public void downloadMap(final File mapsFolder, final String currentArea, final String downloadURL, Context context,
            final ProgressBar pb, final int itemPosition, final MyDownloadAdapter myDownloadAdapter) {
        this.context = context;
        final File areaFolder = new File(mapsFolder, currentArea + "-gh");
        // do not run download
        if (downloadURL == null || areaFolder.exists()) {
            //            loadMap();
            return;
        }

        pb.setProgress(0);
        pb.setMax(100);
        pb.setVisibility(View.VISIBLE);
        pb.setIndeterminate(false);

        //        final ProgressDialog dialog = new ProgressDialog(context);
        //        dialog.setMessage("Downloading and uncompressing " + downloadURL);
        //        dialog.setMax(100);
        //        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //        dialog.show();

        new GHAsyncTask<Void, Integer, Object>() {
            protected Object saveDoInBackground(Void... _ignore) throws Exception {
                broadcastStart();
                Variable.getVariable().setDownloading(true);
                String localFolder = Helper.pruneFileEnd(AndroidHelper.getFileName(downloadURL));
                localFolder = new File(mapsFolder, localFolder + "-gh").getAbsolutePath();
                log("downloading & unzipping " + downloadURL + " to " + localFolder);
                AndroidDownloader downloader = new AndroidDownloader();
                downloader.setTimeout(30000);
                downloader.downloadAndUnzip(downloadURL, localFolder, new ProgressListener() {
                    @Override public void update(long val) {
                        publishProgress((int) val);
                    }
                });
                return null;
            }

            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                pb.setProgress(values[0]);
            }

            protected void onPostExecute(Object _ignore) {
                if (hasError()) {
                    String str = "An error happend while retrieving maps:" + getErrorMessage();
                    log(str, getError());
                    logToast(str);

                    MyApp.tracker().send(new HitBuilders.ExceptionBuilder().setDescription("DownloadFiles-Download " +
                            "map " + getErrorMessage()).setFatal(false).build());
                } else {
                    // load map to local select list when finish downloading ?

                    log("download finished");
                    try {
                        MyMap mm = myDownloadAdapter.remove(itemPosition);
                        mm.setDownloaded(true);
                        myDownloadAdapter.insert(mm);
                        Variable.getVariable().addRecentDownloadedMap(mm);
                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                }
                Variable.getVariable().setDownloading(false);
                broadcastFinished();
                
                pb.setVisibility(View.INVISIBLE);

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
    }

    /**
     * broadcast download finished
     */
    private void broadcastFinished() {
        for (MapDownloadListener listener : mapDownloadListeners) {
            log("download file finished - " + listener.getClass().getSimpleName());
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
     */
    private void broadcastOnUpdate() {
        for (MapDownloadListener listener : mapDownloadListeners) {
            listener.progressBarOnupdate();
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
