package com.junjunguo.pocketmaps.controller;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.dataType.MyMap;
import com.junjunguo.pocketmaps.model.listeners.MapDownloadListener;
import com.junjunguo.pocketmaps.model.listeners.MapFABonClickListener;
import com.junjunguo.pocketmaps.model.listeners.OnDownloadingListener;
import com.junjunguo.pocketmaps.model.map.DownloadFiles;
import com.junjunguo.pocketmaps.model.map.MyDownloadAdapter;
import com.junjunguo.pocketmaps.model.map.OnDownloading;
import com.junjunguo.pocketmaps.model.util.Constant;
import com.junjunguo.pocketmaps.model.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.model.util.Variable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on July 04, 2015.
 */
public class DownloadMapActivity extends AppCompatActivity
        implements MapDownloadListener, OnDownloadingListener, MapFABonClickListener {
    private MyDownloadAdapter myDownloadAdapter;

    /**
     * used to show or hide item download action
     */
    private View vh;
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
        OnDownloading.getOnDownloading().setListener(this);
        List cloudMaps = Variable.getVariable().getCloudMaps();
        if (Variable.getVariable().getDownloadStatus() == Constant.DOWNLOADING && cloudMaps != null &&
                !cloudMaps.isEmpty()) {
            try {
                activeRecyclerView(cloudMaps);
            } catch (Exception e) {e.getStackTrace();}
        } else {
            vh = null;
            itemPosition = 0;
            listDownloadPB = (ProgressBar) findViewById(R.id.my_maps_download_load_list_pb);
            listDownloadTV = (TextView) findViewById(R.id.my_maps_download_load_list_tv);
            listDownloadTV.bringToFront();
            listDownloadPB.setProgress(0);
            listDownloadPB.setMax(100);
            listDownloadPB.setIndeterminate(false);
            listDownloadPB.setVisibility(View.VISIBLE);
            listDownloadPB.bringToFront();
            downloadList();
            activeRecyclerView(new ArrayList());
        }
        DownloadFiles.getDownloader().addListener(this);
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
                List<MyMap> myMaps = new ArrayList<>();
                ArrayList<String> mapUrlList = downloadMapUrlList(Variable.getVariable().getMapUrlList());
                int i = 0;
                for (String mapUrl : mapUrlList) {
                    try {
                        publishProgress(0, 0);
                        URL url = new URL(mapUrl);
                        publishProgress(80, 0);
                        // Read all the text returned by the server
                        BufferedReader l = new BufferedReader(new InputStreamReader(url.openStream()));
                        int lines = 0;
                        while (l.readLine() != null) lines++;
                        l.close();
                        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                        publishProgress(100, 0);
                        String str;
                        while ((str = in.readLine()) != null) {
                            int index = str.indexOf("href=\"");
                            if (index >= 0) {
                                index += 6;
                                int lastIndex = str.indexOf(".ghz", index);
                                if (lastIndex >= 0) {
                                    int sindex = str.indexOf("right\">", str.length() - 52);
                                    int slindex = str.indexOf("M", sindex);
                                    String mapName = str.substring(index, lastIndex);
                                    String size = "";
                                    if (sindex >= 0 && slindex >= 0) {
                                        size = str.substring(sindex + 7, slindex + 1);
                                    }
                                    MyMap mm = new MyMap(mapName, size, mapUrl);
                                    myMaps.add(mm);
                                }
                            }
                            i++;
                            publishProgress(100, (int) (((float) i / lines) * 100));
                        }
                        in.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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
     * @param mapUrlList list of map url
     * @return list of url each url contains maps for each country
     */
    private ArrayList<String> downloadMapUrlList(String mapUrlList) {
        ArrayList<String> mapUrl = new ArrayList<>();
        try {
            URL url = new URL(mapUrlList);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String lineUrl;
            while ((lineUrl = in.readLine()) != null) {
                mapUrl.add(lineUrl);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapUrl;
    }


    /**
     * list of countries are ready
     *
     * @param myMaps MyMap
     */
    private void listReady(List<MyMap> myMaps) {
        if (myMaps.isEmpty()) {
            Toast.makeText(this, "There is a problem with the server, please report this to app developer!",
                    Toast.LENGTH_SHORT).show();
        } else {
            myDownloadAdapter.clearList();
            myDownloadAdapter.addAll(myMaps);
        }
    }

    /**
     * active directions, and directions view
     */
    private void activeRecyclerView(List myMaps) {
        //        RecyclerView mapsRV;
        RecyclerView.LayoutManager layoutManager;

        mapsRV = (RecyclerView) findViewById(R.id.my_maps_download_recycler_view);
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(600);
        animator.setRemoveDuration(600);
        mapsRV.setItemAnimator(animator);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mapsRV.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        mapsRV.setLayoutManager(layoutManager);
        myDownloadAdapter = new MyDownloadAdapter(myMaps, this);
        mapsRV.setAdapter(myDownloadAdapter);
        //        onItemTouchHandler(mapsRV);

        //        int status = Variable.getVariable().getDownloadStatus();
        //        if (status == Constant.DOWNLOADING || status == Constant.PAUSE) {
        //            refreshItemPosition();
        //        }
    }

    @Override public void mapFABonClick(View view) {
        try {
            // load map
            itemPosition = mapsRV.getChildAdapterPosition(view);
            activeDownload(view, itemPosition);
        } catch (Exception e) {e.getStackTrace();}
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
                        .startDownload(Variable.getVariable().getMapsFolder(), myMap.getMapName(), myMap.getUrl());
            }
        } else if (vh != view) {
            if (status != Constant.DOWNLOADING && status != Constant.PAUSE) {
                vh = view;
                if (myMap.getStatus() == Constant.ON_SERVER) {
                    FloatingActionButton itemIcon =
                            (FloatingActionButton) view.findViewById(R.id.my_download_item_flag);
                    itemIcon.setImageResource(R.drawable.ic_pause_orange_24dp);
                    this.downloadStatusTV = (TextView) vh.findViewById(R.id.my_download_item_download_status);
                    downloadStatusTV.setText("Downloading ..." + String.format("%1$" + 3 + "s", 0 + "%"));
                    myDownloadAdapter.getItem(itemPosition).setStatus(Constant.DOWNLOADING);
                    initProgressBar((ProgressBar) vh.findViewById(R.id.my_download_item_progress_bar));
                    DownloadFiles.getDownloader()
                            .startDownload(Variable.getVariable().getMapsFolder(), myMap.getMapName(), myMap.getUrl());
                }
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


    public void downloadStart() {
        //        try {
        //
        //        } catch (Exception e) {
        //            e.getStackTrace();
        //        }
    }

    public void downloadFinished(String mapName) {
        try {
            vh = null;
            MyMap mm = myDownloadAdapter.getMaps().get(myDownloadAdapter.getPosition(mapName));
            mm.setStatus(Constant.COMPLETE);
            //            myDownloadAdapter.insert(mm);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public void progressUpdate(Integer value) {
        try {
            if (itemDownloadPB != null) {
                itemDownloadPB.setProgress(value);
                downloadStatusTV.setText("Downloading " + String.format("%1$" + 3 + "s", value) + "%");
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public void onStop() {
        super.onStop();
        int status = Variable.getVariable().getDownloadStatus();
        if (status == Constant.DOWNLOADING || status == Constant.PAUSE) {
            try {
                Variable.getVariable().setMapFinishedPercentage(itemDownloadPB.getProgress());
                Variable.getVariable().setCloudMaps(myDownloadAdapter.getMaps());
                vh = null;
                //                log("on save instance state");
                finish();
            } catch (Exception e) {
                e.getStackTrace();
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
    public void progressbarReady(TextView downloadStatus, ProgressBar progressBar) {
        int status = Variable.getVariable().getDownloadStatus();
        // do it only when downloading not yet finished
        if (status == Constant.DOWNLOADING || status == Constant.PAUSE) {
            try {
                this.downloadStatusTV = downloadStatus;
                initProgressBar(progressBar);
                this.vh = (View) progressBar.getParent();
                itemPosition = mapsRV.getChildAdapterPosition(vh);
            } catch (Exception e) {e.getStackTrace();}
        }
    }

    private void printMapsList(List<MyMap> myMaps) {
        String s = "";
        for (MyMap mm : myMaps) {
            s += mm.getCountry() + ", ";
        }
        //        log(s);
    }

    private void log(String s) {
        System.out.println(this.getClass().getSimpleName() + "-------------------" + s);
    }
}