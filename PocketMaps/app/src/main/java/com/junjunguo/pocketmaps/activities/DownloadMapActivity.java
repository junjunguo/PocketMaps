package com.junjunguo.pocketmaps.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.junjunguo.pocketmaps.fragments.MyDownloadAdapter;
import com.junjunguo.pocketmaps.model.MyMap;
import com.junjunguo.pocketmaps.model.listeners.MapDownloadListener;
import com.junjunguo.pocketmaps.model.listeners.MapFABonClickListener;
import com.junjunguo.pocketmaps.model.listeners.OnDownloadingListener;
import com.junjunguo.pocketmaps.util.Constant;
import com.junjunguo.pocketmaps.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.util.Variable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        implements MapDownloadListener, OnDownloadingListener, MapFABonClickListener {
    private MyDownloadAdapter myDownloadAdapter;

    private static DownloadFinishListener downloadFinishListener;
    private int itemPosition;
    private ProgressBar itemDownloadPB;
    private ProgressBar listDownloadPB;
    private TextView downloadStatusTV;
    private TextView listDownloadTV;
    private RecyclerView mapsRV;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // return up one level
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //         set status bar
        new SetStatusBarColor().setStatusBarColor(findViewById(R.id.statusBarBackgroundDownload),
                getResources().getColor(R.color.my_primary_dark), this);
        List<MyMap> cloudMaps = Variable.getVariable().getCloudMaps();
        if (Variable.getVariable().getDownloadStatus() == Constant.DOWNLOADING && cloudMaps != null &&
                !cloudMaps.isEmpty()) {
            try {
                activeRecyclerView(cloudMaps);
            } catch (Exception e) {e.printStackTrace();}
        } else {
            itemPosition = -1;
            listDownloadPB = (ProgressBar) findViewById(R.id.my_maps_download_load_list_pb);
            listDownloadTV = (TextView) findViewById(R.id.my_maps_download_load_list_tv);
            listDownloadTV.bringToFront();
            listDownloadPB.setProgress(0);
            listDownloadPB.setMax(100);
            listDownloadPB.setIndeterminate(false);
            listDownloadPB.setVisibility(View.VISIBLE);
            listDownloadPB.bringToFront();
            downloadList();
            activeRecyclerView(new ArrayList<MyMap>());
        }
        DownloadFiles.getDownloader().addListener(this);
        if (downloadFinishListener == null)
        {
          downloadFinishListener = new DownloadFinishListener();
          DownloadFiles.getDownloader().addListener(downloadFinishListener);
        }
        downloadFinishListener.setAdapter(myDownloadAdapter);
        Variable.getVariable().setCloudMaps(new ArrayList<MyMap>());
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
    private void downloadList() {
        new AsyncTask<URL, Integer, List<MyMap>>() {
            @Override protected List<MyMap> doInBackground(URL... params) {
                List<MyMap> myMaps = getMapsFromJSsources(null);
//                ArrayList<String> mapUrlList = downloadMapUrlList(Variable.getVariable().getMapUrlList());
//                int i = 0;
//                for (String mapUrl : mapUrlList) {
//                    try {
//                        publishProgress(0, 0);
//                        URL url = new URL(mapUrl);
//                        publishProgress(80, 0);
//                        // Read all the text returned by the server
//                        BufferedReader l = new BufferedReader(new InputStreamReader(url.openStream()));
//                        int lines = 0;
//                        while (l.readLine() != null) lines++;
//                        l.close();
//                        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
//                        publishProgress(100, 0);
//                        String str;
//                        while ((str = in.readLine()) != null) {
//                            int index = str.indexOf("href=\"");
//                            if (index >= 0) {
//                                index += 6;
//                                int lastIndex = str.indexOf(".ghz", index);
//                                if (lastIndex >= 0) {
//                                    int sindex = str.indexOf("right\">", str.length() - 52);
//                                    int slindex = str.indexOf("M", sindex);
//                                    String mapName = str.substring(index, lastIndex);
//                                    String size = "";
//                                    if (sindex >= 0 && slindex >= 0) {
//                                        size = str.substring(sindex + 7, slindex + 1);
//                                    }
//log("map url: +++ " + mapUrl);
//                                    MyMap mm = new MyMap(mapName, size, mapUrl);
//                                    myMaps.add(mm);
//                                }
//                            }
//                            i++;
//                            publishProgress(100, (int) (((float) i / lines) * 100));
//                        }
//                        in.close();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
// new map sources
                Collections.sort(myMaps);
                //                printMapsList(myMaps);
                return myMaps;
            }

            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                listDownloadPB.setProgress(values[1]);
                listDownloadPB.setSecondaryProgress(values[0]);
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
    public static List<MyMap> getMapsFromJSsources(String mapNameFilter)
    {
      ArrayList<MyMap> maps = new ArrayList<>();
      try
      {
        String jsonDirUrl = Variable.getVariable().getMapUrlJSON();
        String jsonFileUrl = jsonDirUrl + "/map_url_json";
        String jsonContent = DownloadFiles.getDownloader().downloadTextfile(jsonFileUrl);
        
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
            myDownloadAdapter.clearList();
            myDownloadAdapter.addAll(myMaps);
        }
    }

    /**
     * active directions, and directions view
     */
    private void activeRecyclerView(List<MyMap> myMaps) {
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
        myDownloadAdapter = new MyDownloadAdapter(myMaps, this, this);
        mapsRV.setAdapter(myDownloadAdapter);
        //        onItemTouchHandler(mapsRV);

        //        int status = Variable.getVariable().getDownloadStatus();
        //        if (status == Constant.DOWNLOADING || status == Constant.PAUSE) {
        //            refreshItemPosition();
        //        }
    }

    @Override public void mapFABonClick(View view, int iPos) {
        try {
            // load map
            activeDownload(view, iPos);
        } catch (Exception e) {e.printStackTrace();}
    }

    /**
     * download map
     *
     * @param view     View
     * @param position item position
     */
    private void activeDownload(View view, int position) {
        int status = Variable.getVariable().getDownloadStatus();
        MyMap myMap = myDownloadAdapter.getItem(position);
        // pause/ resume
        int pausedMapPosition = myDownloadAdapter.getPosition(Variable.getVariable().getPausedMapName());
        if (position == pausedMapPosition) {
            FloatingActionButton itemIcon = (FloatingActionButton) view.findViewById(R.id.my_download_item_flag);
            if (status == Constant.DOWNLOADING) {
                Variable.getVariable().setDownloadStatus(Constant.PAUSE);
                itemIcon.setImageResource(R.drawable.ic_play_arrow_light_green_a700_24dp);
                DownloadFiles.getDownloader().cancelAsyncTask();
                downloadStatusTV.setText("Paused ..." +
                        String.format("%1$" + 3 + "s", Variable.getVariable().getMapFinishedPercentage()) + "%");
            } else if (status == Constant.PAUSE && DownloadFiles.getDownloader().isAsytaskFinished()) {
                Variable.getVariable().setDownloadStatus(Constant.DOWNLOADING);
                itemIcon.setImageResource(R.drawable.ic_pause_orange_24dp);
                downloadStatusTV.setText("Downloading ..." +
                        String.format("%1$" + 3 + "s", Variable.getVariable().getMapFinishedPercentage()) + "%");
                DownloadFiles.getDownloader()
                        .startDownloadAsync(Variable.getVariable().getMapsFolder(), myMap);
            }
        } else if (itemPosition != position) {
            if (status != Constant.DOWNLOADING && status != Constant.PAUSE) {
                itemPosition = position;
                boolean hasUpdate = myMap.isUpdateAvailable();
                log("Map=" + myMap.getMapName() + " updateAvailable=" + hasUpdate);
                if (myMap.getStatus() == Constant.ON_SERVER || hasUpdate) {
                    FloatingActionButton itemIcon =
                            (FloatingActionButton) view.findViewById(R.id.my_download_item_flag);
                    itemIcon.setImageResource(R.drawable.ic_pause_orange_24dp);
                    this.downloadStatusTV = (TextView) view.findViewById(R.id.my_download_item_download_status);
                    downloadStatusTV.setText("Downloading ..." + String.format("%1$" + 3 + "s", 0 + "%"));
                    myDownloadAdapter.getItem(itemPosition).setStatus(Constant.DOWNLOADING);
                    initProgressBar((ProgressBar) view.findViewById(R.id.my_download_item_progress_bar));
                    DownloadFiles.getDownloader()
                            .startDownloadAsync(Variable.getVariable().getMapsFolder(), myMap);
                }
                else
                {
                  logUser("No update available, map is up to date.");
                }
            }
            else
            {
              logUser("Wait for an other map download.");
            }
        }
    }

    /**
     * init progress bar and set visible
     */
    private void initProgressBar(ProgressBar progressBar) {
        this.itemDownloadPB = progressBar;
        progressBar.setProgress(Variable.getVariable().getMapFinishedPercentage());
        progressBar.setMax(100);
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.VISIBLE);
    }


    @Override
    public void downloadStart() {
    }

    @Override
    public void downloadFinished(String mapName) {
        try {
            itemPosition = -1;

            //            myDownloadAdapter.insert(mm);
            if (downloadStatusTV != null) {
              downloadStatusTV.setText("Download finished!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void progressUpdate(Integer value) {
        try {
            if (itemDownloadPB != null) {
                itemDownloadPB.setProgress(value);
                downloadStatusTV.setText("Downloading " + String.format("%1$" + 3 + "s", value) + "%");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        int status = Variable.getVariable().getDownloadStatus();
        if (status == Constant.DOWNLOADING || status == Constant.PAUSE) {
            try {
                Variable.getVariable().setMapFinishedPercentage(itemDownloadPB.getProgress());
                Variable.getVariable().setCloudMaps(myDownloadAdapter.getMaps());
                itemPosition = -1;
                //                log("on save instance state");
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        DownloadFiles.getDownloader().removeListener(this);
        Variable.getVariable().saveVariables();
    }
    
    /**
     * Recycler view is ready to use
     *
     * @param downloadStatus TextView
     * @param progressBar    ProgressBar
     */
    @Override
    public void progressbarReady(TextView downloadStatus, ProgressBar progressBar, int adapterPosition) {
        int status = Variable.getVariable().getDownloadStatus();
        // do it only when downloading not yet finished
        if (status == Constant.DOWNLOADING || status == Constant.PAUSE) {
            try {
                this.downloadStatusTV = downloadStatus;
                initProgressBar(progressBar);
                itemPosition = adapterPosition;
            } catch (Exception e) {e.printStackTrace();}
        }
    }

    private void log(String s) {
      Log.i(DownloadMapActivity.class.getName(), s);
    }

    private void logUser(String str) {
      log(str);
      Toast.makeText(getBaseContext(), str, Toast.LENGTH_SHORT).show();
    }
    
    /** A listener that everytime listens to download-finish-action, also in background. **/
    static class DownloadFinishListener implements MapDownloadListener
    {
      MyDownloadAdapter adapter;
      
      public void setAdapter(MyDownloadAdapter adapter)
      {
        this.adapter = adapter;
      }

      @Override public void downloadStart() {}
      @Override public void progressUpdate(Integer value) {}
      @Override public void downloadFinished(String mapName)
      {
        MyMap mm = adapter.getMaps().get(adapter.getPosition(mapName));
        mm.setStatus(Constant.COMPLETE);
        MyMap.setVersionCompatible(mapName, mm);
        Variable.getVariable().setDownloadStatus(Constant.COMPLETE);
      }
    }
}