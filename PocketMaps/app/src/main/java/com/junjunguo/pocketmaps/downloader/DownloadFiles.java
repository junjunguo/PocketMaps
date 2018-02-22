package com.junjunguo.pocketmaps.downloader;

import android.os.AsyncTask;
import android.util.Log;

import com.junjunguo.pocketmaps.model.MyMap;
import com.junjunguo.pocketmaps.model.listeners.MapDownloadListener;
import com.junjunguo.pocketmaps.util.Constant;
import com.junjunguo.pocketmaps.util.Variable;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of Pockets Maps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 14, 2015.
 */
public class DownloadFiles {
    private List<MapDownloadListener> mapDownloadListeners;
    private static DownloadFiles downloadFiles;
    private boolean asytaskFinished;
    private AsyncTask<URL, Integer, MapDownloader> asyncTask;
    private MapDownloader mapDownloader;

    private DownloadFiles() {
        this.mapDownloadListeners = new ArrayList<>();
        asytaskFinished = true;
    }

    public static DownloadFiles getDownloader() {
        if (downloadFiles == null) {
            downloadFiles = new DownloadFiles();
        }
        return downloadFiles;
    }
    
  /**
   * @param mapUrl
   * @return json string
   */
  public String downloadTextfile(String textFileUrl)
  {
    StringBuilder json = new StringBuilder();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(textFileUrl).openStream())))
    {
      String lineUrl;
      while ((lineUrl = in.readLine()) != null)
      {
        json.append(lineUrl);
      }
      in.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return json.toString();
  }

    /**
     * download and unzip map files (in background) and save it in  mapsFolder/currentArea-gh/
     *
     * @param mapsFolder maps folder for maps
     * @param mapName    area (country) to download
     * @param urlStr     download link
     */
    public void startDownloadAsync(final File mapsFolder, MyMap myMap) {
        final String mapName = myMap.getMapName();
        final String urlStr = myMap.getUrl();
        MyMap.setVersionIncompatible(myMap.getMapName(), myMap);
        mapDownloader = new MapDownloader();
        asytaskFinished = false;
        asyncTask = new AsyncTask<URL, Integer, MapDownloader>() {
            protected MapDownloader doInBackground(URL... params) {
                if (!mapsFolder.exists()) { mapsFolder.mkdirs();}
                mapDownloader.downloadFile(urlStr,
                        (new File(mapsFolder.getAbsolutePath(), urlStr.substring(urlStr.lastIndexOf("/") + 1)))
                                .getAbsolutePath(), mapName, new MapDownloadListener() {
                            public void downloadStart() {
                            }

                            public void downloadFinished(String mapName) {
                            }

                            public void progressUpdate(Integer value) {
                                publishProgress(value);
                            }

                            @Override
                            public void onStartUnpacking()
                            {
                              publishProgress(150);
                            }
                        });
                return mapDownloader;
            }

            protected void onCancelled() {
                super.onCancelled();
                asytaskFinished = true;
            }

            protected void onPreExecute() {
                super.onPreExecute();
                asytaskFinished = true;
                broadcastStart();
                Variable.getVariable().setDownloadStatus(Constant.DOWNLOADING);
            }

            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                broadcastOnUpdate(values[0]);
            }

            protected void onPostExecute(MapDownloader mapDownloader) {
                super.onPostExecute(mapDownloader);
                if (mapDownloader.isDownloadStatusOk())
                {
                  broadcastFinished(mapName);
                }
                else
                {
                  Variable.getVariable().setDownloadStatus(Constant.ERROR);
                }
            }
        }.execute();
    }

    /**
     * add to broadcast list
     *
     * @param listener MapDownloadListener
     */
    public void addListener(MapDownloadListener listener) {
        //        log("add listener before- " + mapDownloadListeners.toString());
        if (!mapDownloadListeners.contains(listener)) this.mapDownloadListeners.add(listener);
        //        log("add listener after+ " + mapDownloadListeners.toString());
    }

    /**
     * remove listener from broadcast list
     *
     * @param listener MapDownloadListener
     */
    public void removeListener(MapDownloadListener listener) {
        this.mapDownloadListeners.remove(listener);
    }

    /**
     * broadcast download finished
     *
     * @param mapName String map name
     */
    private void broadcastFinished(String mapName) {
        try {
            Variable.getVariable().setDownloadStatus(Constant.COMPLETE);
            for (MapDownloadListener listener : mapDownloadListeners) {
                listener.downloadFinished(mapName);
            }
            Variable.getVariable().addRecentDownloadedMap(new MyMap(mapName));
            // load map to local select list when finish downloading ?
        } catch (Exception e) {
            e.printStackTrace();
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
     * @param value int progressUpdate
     */
    private void broadcastOnUpdate(Integer value) {
      if (value == 150)
      {
        for (MapDownloadListener listener : mapDownloadListeners) {
          listener.onStartUnpacking();
        }
      }
      else
      {
        for (MapDownloadListener listener : mapDownloadListeners) {
            listener.progressUpdate(value);
        }
      }
    }

    public boolean isAsytaskFinished() {
        return asytaskFinished;
    }

    public void cancelAsyncTask() { asyncTask.cancel(true);}

    /**
     * send message to logcat
     *
     * @param str String
     */
    private void log(String str) {
        Log.i(this.getClass().getName(), str);
    }

}
