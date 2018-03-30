package com.junjunguo.pocketmaps.activities;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.downloader.DownloadFiles;
import com.junjunguo.pocketmaps.downloader.MapUnzip;
import com.junjunguo.pocketmaps.fragments.MyDownloadAdapter;
import com.junjunguo.pocketmaps.model.MyMap;
import com.junjunguo.pocketmaps.model.listeners.OnClickMapListener;
import com.junjunguo.pocketmaps.model.listeners.OnProgressListener;
import com.junjunguo.pocketmaps.util.IO;
import com.junjunguo.pocketmaps.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.util.Variable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shows all server-side-available maps on a list.
 * Allows to download or update a map.
 * 
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 04, 2015.
 */
public class DownloadMapActivity extends AppCompatActivity
        implements OnClickMapListener {
    private MyDownloadAdapter myDownloadAdapter;

//    private static DownloadFinishListener downloadFinishListener;
//    private ProgressBar itemDownloadPB;
    private ProgressBar listDownloadPB;
//    private TextView downloadStatusTV;
    private TextView listDownloadTV;
    private RecyclerView mapsRV;
    private long cloudMapsTime;
    private ArrayList<BroadcastReceiver> receiverList = new ArrayList<BroadcastReceiver>();


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // return up one level
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // set status bar
        new SetStatusBarColor().setStatusBarColor(findViewById(R.id.statusBarBackgroundDownload),
                getResources().getColor(R.color.my_primary_dark), this);
        List<MyMap> cloudMaps = Variable.getVariable().getCloudMaps();
        try
        {
          String dlFiles[] = Variable.getVariable().getDownloadsFolder().list();
          if (cloudMaps == null || cloudMaps.isEmpty() || isCloudMapsUpdateRecent())
          {
            cloudMaps = null;
          }
          if (cloudMaps!=null && dlFiles.length==0)
          {
            log("Skip downloading existing cloud-map-list");
            Collections.sort(cloudMaps);
            activateRecyclerView(cloudMaps);
          }
          else
          {
            listDownloadPB = (ProgressBar) findViewById(R.id.my_maps_download_load_list_pb);
            listDownloadTV = (TextView) findViewById(R.id.my_maps_download_load_list_tv);
            listDownloadTV.bringToFront();
            listDownloadPB.setProgress(0);
            listDownloadPB.setMax(100);
            listDownloadPB.setIndeterminate(false);
            listDownloadPB.setVisibility(View.VISIBLE);
            listDownloadPB.bringToFront();
            activateRecyclerView(new ArrayList<MyMap>());
            downloadList(cloudMaps, dlFiles);
          }
        } catch (Exception e) {e.printStackTrace();}
    }

    private boolean isCloudMapsUpdateRecent()
    {
      long now = System.currentTimeMillis();
      long hours = 1000 * 60 * 60 * 3;
      if ((cloudMapsTime + hours) > now) { return true; }
      return false;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * download and generate a list of countries from server and add them to the list view
     */
    private void downloadList(final List<MyMap> cloudMaps, final String dlFiles[]) {
        new AsyncTask<URL, Integer, List<MyMap>>() {
            @Override protected List<MyMap> doInBackground(URL... params) {
                OnProgressListener procListener = new OnProgressListener()
                {
                  @Override
                  public void onProgress(int progress)
                  {
                    publishProgress(progress);
                  }
                };
                for(String dlFile : dlFiles)
                {
                  if (dlFile.endsWith(".ghz"))
                  {
                    String tmpMapName = dlFile.substring(0, dlFile.length()-4);
                    MyMap tmpMap = new MyMap(tmpMapName);
                    for (MyMap curMap : Variable.getVariable().getCloudMaps())
                    {
                      if (curMap.getMapName().equals(tmpMapName)) { tmpMap = curMap; break; }
                    }
                    File idFile = MyMap.getMapFile(tmpMap, MyMap.MapFileType.DlIdFile);
                    if (!idFile.exists())
                    {
                      unzipBg(tmpMap, myDownloadAdapter);
                      continue;
                    }
                    String idFileContent = IO.readFromFile(idFile, "\n");
                    if (idFileContent.startsWith("" + MyMap.DlStatus.Error + ": "))
                    {
                      logUser(idFileContent);
                      clearDlFile(tmpMap);
                    }
                    else
                    {
                      int id = Integer.parseInt(idFileContent.replace("\n", ""));
                      broadcastReceiverCheck(DownloadMapActivity.this, tmpMap, id);
                    }
                  }
                }
                if (cloudMaps == null)
                {
                  List<MyMap> myMaps = getMapsFromJSsources(null, procListener);
                  Collections.sort(myMaps);
                  return myMaps;
                }
                return cloudMaps;
            }

            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                listDownloadPB.setProgress(values[0]);
            }

            @Override protected void onPostExecute(List<MyMap> myMaps) {
                super.onPostExecute(myMaps);
                listReady(myMaps);
                listDownloadPB.setVisibility(View.GONE);
                listDownloadTV.setVisibility(View.GONE);
            }
        }.execute();
    }

    /**
     * Read all maps data from server.
     * @param mapNameFilter MapName or null for all.
     * @return list of MyMap
     */
    public static List<MyMap> getMapsFromJSsources(String mapNameFilter, OnProgressListener task)
    {
      ArrayList<MyMap> maps = new ArrayList<>();
      try
      {
        String jsonDirUrl = Variable.getVariable().getMapUrlJSON();
        String jsonFileUrl = jsonDirUrl + "/map_url_json";
        String jsonContent = DownloadFiles.getDownloader().downloadTextfile(jsonFileUrl);
        task.onProgress(50);
        
        JSONObject jsonObj = new JSONObject(jsonContent);
        if (jsonObj.has("maps-" + MyMap.MAP_VERSION) && jsonObj.has("maps-" + MyMap.MAP_VERSION + "-path"))
        {
          String mapsPath = jsonObj.getString("maps-" + MyMap.MAP_VERSION + "-path");
          JSONArray jsonList = jsonObj.getJSONArray("maps-" + MyMap.MAP_VERSION);
          for (int i = 0; i < jsonList.length(); i++)
          {
            JSONObject o = jsonList.getJSONObject(i);
            String name = o.getString("name");
            if (mapNameFilter!=null && !mapNameFilter.equals(name)) { continue; }
            String size = o.getString("size");
            String time = o.getString("time");
            MyMap curMap = new MyMap(name,size,time,jsonDirUrl + "/" + mapsPath + "/" + time + "/");
            maps.add(curMap);
            float progress = i;
            progress = i / jsonList.length();
            progress = progress * 50.0f;
            task.onProgress(50 + (int)progress);
          }
        }
      }
      catch (JSONException e)
      {
        e.printStackTrace();
      }
      return maps;
    }

    /**
     * list of countries are ready
     *
     * @param myMaps MyMap
     */
    private void listReady(List<MyMap> myMaps) {
        if (myMaps.isEmpty()) {
            logUser("No connection to server!");
        } else {
            Variable.getVariable().updateCloudMaps(myMaps);
            cloudMapsTime = System.currentTimeMillis();
            myDownloadAdapter.clearList();
            myDownloadAdapter.addAll(myMaps);
        }
    }

    /**
     * active directions, and directions view
     */
    private void activateRecyclerView(List<MyMap> myMaps) {
        mapsRV = (RecyclerView) findViewById(R.id.my_maps_download_recycler_view);
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(600);
        animator.setRemoveDuration(600);
        mapsRV.setItemAnimator(animator);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mapsRV.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mapsRV.setLayoutManager(layoutManager);
        myDownloadAdapter = new MyDownloadAdapter(myMaps, this);
        mapsRV.setAdapter(myDownloadAdapter);
    }

    @Override public void onClickMap(View view, int iPos, TextView tv) {
        try {
            // download map
          onClickMapNow(view, iPos, tv);
        } catch (Exception e) {e.printStackTrace();}
    }

    /**
     * download map
     *
     * @param view     View
     * @param position item position
     */
    private void onClickMapNow(View view, int position, TextView tv)
    {
      MyMap myMap = myDownloadAdapter.getItem(position);
      if (myMap.getStatus() == MyMap.DlStatus.Downloading || myMap.getStatus() == MyMap.DlStatus.Unzipping)
      {
        logUser("Already downloading!");
        return;
      }
      else if (myMap.getStatus() == MyMap.DlStatus.Complete)
      {
        logUser("Already downloaded!");
        return;
      }
      tv.setText("downloading...");
      refreshMapEntry(myMap, myDownloadAdapter);
      myMap.setStatus(MyMap.DlStatus.Downloading);
      DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
      Request request = new Request(Uri.parse(myMap.getUrl()));
      File destFile = MyMap.getMapFile(myMap, MyMap.MapFileType.DlMapFile);
      request.setDestinationUri(Uri.fromFile(destFile));
      long enqueueId = dm.enqueue(request);
      File idFile = MyMap.getMapFile(myMap, MyMap.MapFileType.DlIdFile);
      IO.writeToFile("" + enqueueId, idFile, false);
      BroadcastReceiver br = createBroadcastReceiver(myDownloadAdapter, myMap, enqueueId);
      registerReceiver(br, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
      receiverList.add(br);
    }
    
    /** Check first, if map is finished, or on pending status register receiver. **/
    private static void broadcastReceiverCheck(DownloadMapActivity activity, final MyMap myMap, final long enqueueId)
    {
      int preStatus = getDownloadStatus(activity, enqueueId);
      if (preStatus == DownloadManager.STATUS_SUCCESSFUL)
      {
        unzipBg(myMap, activity.myDownloadAdapter);
        return;
      }
      else if (preStatus == DownloadManager.STATUS_FAILED)
      {
        clearDlFile(myMap);
        activity.logUser("Error post-downloading map: " + myMap.getMapName());
      }
      else
      {
        BroadcastReceiver br = createBroadcastReceiver(activity.myDownloadAdapter, myMap, enqueueId);
        activity.registerReceiver(br, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        activity.receiverList.add(br);
      }
    }
    
    private static BroadcastReceiver createBroadcastReceiver(final MyDownloadAdapter dlAdapter, final MyMap myMap, final long enqueueId)
    {
      log("Register receiver for map: " + myMap.getMapName());
      BroadcastReceiver receiver = new BroadcastReceiver() {
        boolean isActive = true;
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isActive) { return; }
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                int dlStatus = getDownloadStatus(context, enqueueId);
                if (dlStatus == DownloadManager.STATUS_SUCCESSFUL)
                {
                  unzipBg(myMap, dlAdapter);
                  isActive = false;
                }
                else if (dlStatus == -1)
                { // Aborted
                  log("Break downloading map: " + myMap.getMapName());
                  myMap.setStatus(MyMap.DlStatus.On_server);
                  clearDlFile(myMap);
                  isActive = false;
                }
                else if (dlStatus == DownloadManager.STATUS_FAILED)
                { // Error
                  log("Error downloading map: " + myMap.getMapName());
                  myMap.setStatus(MyMap.DlStatus.Error);
                  clearDlFile(myMap);
                  isActive = false;
                }
                refreshMapEntry(myMap, dlAdapter);
            }
        }

      };

      return receiver;
    }
    
    private static void refreshMapEntry(MyMap myMap, MyDownloadAdapter dlAdapter)
    {
      int rvIndex = Variable.getVariable().getCloudMaps().indexOf(myMap);
      if (rvIndex >= 0)
      {
        log("Refreshing map-entry at " + rvIndex);
        dlAdapter.notifyItemRemoved(rvIndex);
        dlAdapter.notifyItemInserted(rvIndex);
      }
      else { log("No map-entry for refreshing found"); }
    }
    
    private static int getDownloadStatus(Context context, long enqueueId)
    {
      Query query = new Query();
      DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
      query.setFilterById(enqueueId);
      Cursor c = dm.query(query);
      if (c.moveToFirst())
      {
        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
        return c.getInt(columnIndex);
      }
      return -1; // Aborted.
    }

    private static void clearDlFile(MyMap myMap)
    {
      log("Clearing dl file for map: " + myMap.getMapName());
      File destFile = MyMap.getMapFile(myMap, MyMap.MapFileType.DlMapFile);
      if (destFile.exists())
      {
        destFile.delete();
      }
      destFile = MyMap.getMapFile(myMap, MyMap.MapFileType.DlIdFile);
      if (destFile.exists())
      {
        destFile.delete();
      }
    }
    
    private static void unzipBg(final MyMap myMap, final MyDownloadAdapter dlAdapter)
    {
      log("Unzipping map: " + myMap.getMapName());
      myMap.setStatus(MyMap.DlStatus.Unzipping);
      refreshMapEntry(myMap, dlAdapter);
      new AsyncTask<URL, Integer, MyMap>() {
        String errMsg = null;
        @Override protected MyMap doInBackground(URL... params) {
            File ghzFile = MyMap.getMapFile(myMap, MyMap.MapFileType.DlMapFile);
            if (ghzFile.exists())
            {
              try
              {
                new MapUnzip().unzip(ghzFile.getPath(),
                    new File(Variable.getVariable().getMapsFolder(), myMap.getMapName() + "-gh").getAbsolutePath());
              }
              catch (IOException e)
              {
                errMsg = "Error unpacking map: " + myMap.getMapName();
              }
            }
            else
            {
              errMsg = "Error, missing downloaded file: " + ghzFile.getPath();
            }
            clearDlFile(myMap);
            return myMap;
        }

        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        @Override protected void onPostExecute(MyMap myMaps) {
            super.onPostExecute(myMaps);
            refreshMapEntry(myMap, dlAdapter);
            if (errMsg!=null)
            {
              File idFile = MyMap.getMapFile(myMap, MyMap.MapFileType.DlIdFile);
              IO.writeToFile("" + MyMap.DlStatus.Error + ": " + errMsg, idFile, false);
              myMap.setStatus(MyMap.DlStatus.Error);
              return;
            }
            Variable.getVariable().getRecentDownloadedMaps().add(myMap);
            MyMap.setVersionCompatible(myMap.getMapName(), myMap);
            myMap.setStatus(MyMap.DlStatus.Complete);
        }
      }.execute();
    }

    @Override
    public void onStop() {
        super.onStop();
        Variable.getVariable().saveVariables();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        for (BroadcastReceiver b : receiverList)
        {
          unregisterReceiver(b);
        }
        receiverList.clear();
    }

    private static void log(String s) {
      Log.i(DownloadMapActivity.class.getName(), s);
    }

    private void logUser(String str) {
      log(str);
      try
      {
        Toast.makeText(getBaseContext(), str, Toast.LENGTH_SHORT).show();
      }
      catch (Exception e) { e.printStackTrace(); }
    }
    
}