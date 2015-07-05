package com.junjunguo.pocketmaps.model.map;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.graphhopper.util.Helper;
import com.graphhopper.util.ProgressListener;
import com.junjunguo.pocketmaps.model.util.Variable;

import java.io.File;

/**
 * This file is part of Pockets Maps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 14, 2015.
 */
public class DownloadFiles {

    private File mapsFolder;
    private String currentArea;
    private String downloadURL;
    private Context context;

    /**
     * download and unzip map files and save it in  mapsFolder/currentArea-gh/
     *
     * @param mapsFolder  File maps folder
     * @param currentArea area (country) to download
     * @param downloadURL download link
     * @param context     calling activity
     */
    public DownloadFiles(File mapsFolder, String currentArea, String downloadURL, Context context) {
        this.mapsFolder = mapsFolder;
        this.currentArea = currentArea;
        this.downloadURL = downloadURL;
        this.context = context;
        downloadingFiles();
    }

    public void downloadingFiles() {
        final File areaFolder = new File(mapsFolder, currentArea + "-gh");
        // do not run download
        if (downloadURL == null || areaFolder.exists()) {
            //            loadMap();
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Downloading and uncompressing " + downloadURL);
        dialog.setIndeterminate(false);
        dialog.setMax(100);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();

        new GHAsyncTask<Void, Integer, Object>() {
            protected Object saveDoInBackground(Void... _ignore) throws Exception {
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
                dialog.setProgress(values[0]);
            }

            protected void onPostExecute(Object _ignore) {
                dialog.hide();
                if (hasError()) {
                    String str = "An error happend while retrieving maps:" + getErrorMessage();
                    log(str, getError());
                    logToast(str);
                } else {
                    // load map to local select list when finish downloading ?

                    // tell variable that a new map has been downloaded
                    Variable.getVariable().setaNewMapDownloaded(true);
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
