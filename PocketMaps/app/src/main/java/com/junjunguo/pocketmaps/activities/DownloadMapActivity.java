package com.junjunguo.pocketmaps.activities;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnCloseListener;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.downloader.DownloadFiles;
import com.junjunguo.pocketmaps.downloader.DownloadService;
import com.junjunguo.pocketmaps.downloader.MapDownloadUnzip;
import com.junjunguo.pocketmaps.downloader.MapDownloadUnzip.StatusUpdate;
import com.junjunguo.pocketmaps.downloader.ProgressPublisher;
import com.junjunguo.pocketmaps.fragments.MyMapAdapter;
import com.junjunguo.pocketmaps.model.MyMap;
import com.junjunguo.pocketmaps.model.MyMap.DlStatus;
import com.junjunguo.pocketmaps.model.listeners.OnClickMapListener;
import com.junjunguo.pocketmaps.model.listeners.OnProgressListener;
import com.junjunguo.pocketmaps.util.IO;
import com.junjunguo.pocketmaps.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.util.Variable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
        implements OnClickMapListener, SearchView.OnQueryTextListener{
    private MyMapAdapter myDownloadAdapter;

    private ProgressBar listDownloadPB;
    private TextView listDownloadTV;
    private RecyclerView mapsRV;
    private static long cloudMapsTime;
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
          if (cloudMaps == null || cloudMaps.isEmpty() || isCloudMapsUpdateOld())
          {
            cloudMaps = null;
          }
          if (cloudMaps!=null && dlFiles.length==0)
          {
            log("Skip downloading existing cloud-map-list");
            Collections.sort(cloudMaps);
            activateRecyclerView(filterDeprecatedMaps(cloudMaps));
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
            downloadList(this, cloudMaps, dlFiles);
          }
        } catch (Exception e) {e.printStackTrace();}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_maps, menu);
      
      MenuItem searchItem = menu.findItem(R.id.menu_search_filter);
      SearchView searchView = (SearchView) searchItem.getActionView();
      searchView.setQueryHint(getResources().getString(R.string.search_hint));
      searchView.setOnQueryTextListener(this);
      searchView.setOnSearchClickListener(createHideMenuListener(menu));
      searchView.setOnCloseListener(createShowMenuListener(menu));
      return true;
    }

    private OnClickListener createHideMenuListener(final Menu menu)
    {
      return new OnClickListener()
      {
        @Override
        public void onClick(View arg0)
        {
          menu.setGroupVisible(R.id.menu_map_all_group, false);
        }
      };
    }

    private OnCloseListener createShowMenuListener(final Menu menu)
    {
      return new OnCloseListener()
      {
        @Override
        public boolean onClose()
        {
          menu.setGroupVisible(R.id.menu_map_all_group, true);
          return false;
        }
      };
    }

    private boolean isCloudMapsUpdateOld()
    {
      long now = System.currentTimeMillis();
      long hours = 1000 * 60 * 60 * 3;
      if ((cloudMapsTime + hours) > now) { return false; }
      return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        String continent = item.getTitle().toString();
        if (continent.equals("Downloaded"))
        {
          mapsRV.scrollToPosition(0);
          return true;
        }
        for (int i=0; i<myDownloadAdapter.getItemCount(); i++)
        {
          MyMap curMap = myDownloadAdapter.getItem(i);
          if (curMap.getStatus()!=MyMap.DlStatus.On_server) { continue; }
          if (curMap.getContinent().equals(continent))
          {
            mapsRV.scrollToPosition(i);
            return true;
          }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * download and generate a list of countries from server and add them to the list view
     */
    private void downloadList(final Activity activity,
                              final List<MyMap> cloudMaps,
                              final String dlFiles[]) {
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
                    StatusUpdate stUpdate = createStatusUpdater();
                    MapDownloadUnzip.checkMap(activity, tmpMap, stUpdate);
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

            @Override protected void onProgressUpdate(Integer... values) {
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

    protected StatusUpdate createStatusUpdater()
    {
      StatusUpdate s = new StatusUpdate()
      {
        @Override
        public void logUserThread(String txt)
        {
          DownloadMapActivity.this.logUserThread(txt);
        }

        @Override
        public void updateMapStatus(MyMap map)
        {
          DownloadMapActivity.this.myDownloadAdapter.refreshMapView(map);
        }

        @Override
        public void onRegisterBroadcastReceiver(Activity activity, MyMap myMap, long enqueueId)
        {
          BroadcastReceiver br = createBroadcastReceiver(activity, this, myMap, enqueueId);
          activity.registerReceiver(br, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
          DownloadMapActivity.this.receiverList.add(br);
        }
      };
      return s;
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
        String jsonFileUrl = jsonDirUrl + "/map_url-" + MyMap.MAP_VERSION + ".json";
        String jsonContent = DownloadFiles.getDownloader().downloadTextfile(jsonFileUrl);
        task.onProgress(50);
        log("Json file downloaded");
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
            if (o.has("newMap"))
            {
              curMap.setMapNameNew(o.getString("newMap"));
            }
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
            myDownloadAdapter.addAll(filterDeprecatedMaps(myMaps));
        }
    }

    private List<MyMap> filterDeprecatedMaps(List<MyMap> myMaps)
    {
      ArrayList<MyMap> newList = new ArrayList<MyMap>();
      for (MyMap curMap : myMaps)
      {
        if (!curMap.getMapNameNew().isEmpty() &&
             curMap.getStatus() != DlStatus.Complete) { continue; }
        newList.add(curMap);
      }      
      return newList;
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
        myDownloadAdapter = new MyMapAdapter(myMaps, this, true);
        mapsRV.setAdapter(myDownloadAdapter);
    }

    @Override public void onClickMap(int iPos) {
        try {
            // download map
          onClickMapNow(iPos);
        } catch (Exception e) {e.printStackTrace();}
    }

    /**
     * download map
     *
     * @param view     View
     * @param position item position
     */
    private void onClickMapNow(int position)
    {
      MyMap myMap = myDownloadAdapter.getItem(position);
      if (myMap.getStatus() == MyMap.DlStatus.Downloading || myMap.getStatus() == MyMap.DlStatus.Unzipping)
      {
        logUser("Already downloading!");
        return;
      }
      else if (myMap.isUpdateAvailable())
      {
        MyMap myMapNew = null;
        if (!myMap.getMapNameNew().isEmpty())
        {          
          int removePos = -1;
          for (int i= 0; i<myDownloadAdapter.getItemCount(); i++)
          {
            if (myDownloadAdapter.getItem(i).getMapName().equals(myMap.getMapName()))
            {
              removePos = i;
            }
            if (myDownloadAdapter.getItem(i).getMapName().equals(myMap.getMapNameNew()))
            {
              position = i;
              myMapNew = myDownloadAdapter.getItem(i);
            }
          }
          if (removePos < 0 || myMapNew == null)
          {
            logUser("OldMap or NewMap missing on json-list!");
            return;
          }
          mapsRV.scrollToPosition(position);
          myDownloadAdapter.remove(removePos);
          if (position > removePos) { position--; }
        }
        MainActivity.clearLocalMap(myMap);
        if (myMapNew != null)
        {
          myMap = myMapNew;
          if (myMap.getStatus() != DlStatus.On_server)
          {
            logUser("New map is: " + myMap.getMapName());
            return;
          }
        }
      }
      else if (myMap.getStatus() == MyMap.DlStatus.Complete)
      {
        logUser("Already downloaded!");
        return;
      }
      myMap.setStatus(MyMap.DlStatus.Downloading);
      myDownloadAdapter.refreshMapView(myMap);
      String vers = "?v=unknown";
      try
      {
        PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        vers = "?v=" + packageInfo.versionName;
      }
      catch (Exception e) {} // No problem, not important.
      
      final MyMap downloadMap = myMap;
      final String downloadUrl = myMap.getUrl() + vers;
      final StatusUpdate stUpdate = createStatusUpdater();
      final MyMapAdapter downloadAdapter = myDownloadAdapter;
      
      // Start download service
      Intent serviceIntent = new Intent(this, DownloadService.class);
      serviceIntent.setAction(DownloadService.ACTION_START);
      serviceIntent.putExtra(DownloadService.EXTRA_MAP_NAME, downloadMap.getMapName());
      serviceIntent.putExtra(DownloadService.EXTRA_URL, downloadUrl);
      serviceIntent.putExtra(DownloadService.EXTRA_FILE_LENGTH, 0L);
      startService(serviceIntent);
      
      // Also start local download for UI progress
      startLocalDownload(downloadMap, downloadUrl, stUpdate, downloadAdapter);
    }
    
    private void startLocalDownload(final MyMap downloadMap, final String downloadUrl, 
                                   final StatusUpdate stUpdate, final MyMapAdapter downloadAdapter) {
      final int notifyId = downloadMap.getMapName().hashCode();
      final ProgressPublisher progressPublisher = new ProgressPublisher(this, notifyId);
      progressPublisher.setInterval(200);
      
      AsyncTask<Void, Integer, Boolean> downloadTask = new AsyncTask<Void, Integer, Boolean>() {
        @Override
        protected Boolean doInBackground(Void... voids) {
          try {
            log("Starting download: " + downloadUrl);
            URL url = new URL(downloadUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int fileLength = conn.getContentLength();
            log("File size: " + fileLength + " bytes");
            
            File destFile = MyMap.getMapFile(downloadMap, MyMap.MapFileType.DlMapFile);
            File dlDir = new File(getFilesDir(), "downloads");
            if (!dlDir.exists()) dlDir.mkdirs();
            final File downloadFile = new File(dlDir, destFile.getName());
            
            InputStream is = conn.getInputStream();
            FileOutputStream fos = new FileOutputStream(downloadFile);
            byte[] buffer = new byte[8192];
            long downloaded = 0;
            int count;
            long lastUpdate = System.currentTimeMillis();
            while ((count = is.read(buffer)) != -1) {
              downloaded += count;
              fos.write(buffer, 0, count);
              if (System.currentTimeMillis() - lastUpdate > 500 && fileLength > 0) {
                int percent = (int) (downloaded * 100 / fileLength);
                publishProgress(percent);
                lastUpdate = System.currentTimeMillis();
              }
            }
            if (fileLength > 0) publishProgress(100);
            fos.close();
            is.close();
            conn.disconnect();
            return true;
          } catch (Exception e) {
            e.printStackTrace();
            return false;
          }
        }
        
        @Override
        protected void onProgressUpdate(Integer... values) {
          int percent = values[0];
          progressPublisher.updateText(false, "Downloading " + downloadMap.getMapName(), percent);
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
          log("Download complete");
          progressPublisher.updateTextFinal("Download finished: " + downloadMap.getMapName());
          if (success) {
            File destFile = MyMap.getMapFile(downloadMap, MyMap.MapFileType.DlMapFile);
            File dlDir = new File(getFilesDir(), "downloads");
            File downloadFile = new File(dlDir, destFile.getName());
            log("Download complete: " + downloadFile.getName());
            File idFile = MyMap.getMapFile(downloadMap, MyMap.MapFileType.DlIdFile);
            IO.writeToFile("manual_download", idFile, false);
            MapDownloadUnzip.unzipBg(DownloadMapActivity.this, downloadMap, stUpdate);
          } else {
            logUser("Download failed!");
            downloadMap.setStatus(MyMap.DlStatus.Error);
            downloadAdapter.refreshMapView(downloadMap);
          }
        }
      };
      downloadTask.execute();
    }
    
    private static BroadcastReceiver createBroadcastReceiver(final Activity activity,
                                                             final StatusUpdate stUpdate,
                                                             final MyMap myMap,
                                                             final long enqueueId)
    {
      log("Register receiver for map: " + myMap.getMapName());
      BroadcastReceiver receiver = new BroadcastReceiver() {
        boolean isActive = true;
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isActive) { return; }
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                int dlStatus = MapDownloadUnzip.getDownloadStatus(context, enqueueId);
                if (dlStatus == DownloadManager.STATUS_SUCCESSFUL)
                {
                  MapDownloadUnzip.unzipBg(activity, myMap, stUpdate);
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
                stUpdate.updateMapStatus(myMap);
            }
        }

      };

      return receiver;
    }
    
    public static void clearDlFile(MyMap myMap)
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

    @Override
    public void onStop() {
        super.onStop();
        Variable.getVariable().saveVariables(Variable.VarType.Base);
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
    
    private void logUserThread(final String str) {
      runOnUiThread(new Runnable() {
        @Override public void run()
        {
          logUser(str);
        }});
    }

    @Override
    public boolean onQueryTextChange(String filterText)
    {
      myDownloadAdapter.doFilter(filterText);
      return true;
    }

    @Override
    public boolean onQueryTextSubmit(String filterText)
    {
      InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(findViewById(R.id.menu_search_filter).getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
      return true;
    }
    
}