package com.junjunguo.pocketmaps.controller;

import android.os.AsyncTask;
import android.os.Bundle;
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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.dataType.MyMap;
import com.junjunguo.pocketmaps.model.listeners.MapDownloadListener;
import com.junjunguo.pocketmaps.model.listeners.MapFABonClickListener;
import com.junjunguo.pocketmaps.model.listeners.OnDownloadingListener;
import com.junjunguo.pocketmaps.model.map.DownloadFiles;
import com.junjunguo.pocketmaps.model.util.MyApp;
import com.junjunguo.pocketmaps.model.util.MyDownloadAdapter;
import com.junjunguo.pocketmaps.model.util.OnDownloading;
import com.junjunguo.pocketmaps.model.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.model.util.Variable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
    private TextView downloadStatus;
    private TextView listDownloadTV;
    private RecyclerView mapsRV;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //         set status bar
        new SetStatusBarColor().setStatusBarColor(findViewById(R.id.statusBarBackgroundDownload),
                getResources().getColor(R.color.my_primary_dark), this);
        OnDownloading.getOnDownloading().setListener(this);
        List cloudMaps = Variable.getVariable().getCloudMaps();
        if (Variable.getVariable().isDownloading() && cloudMaps != null && !cloudMaps.isEmpty()) {
            try {
                activeRecyclerView(cloudMaps);
            } catch (Exception e) {e.getStackTrace();}
        } else {
            vh = null;
            itemPosition = 0;
            listDownloadPB = (ProgressBar) findViewById(R.id.my_maps_download_load_list_pb);
            log("list download pb : visibility" + listDownloadPB.getVisibility());
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
            @Override protected List doInBackground(URL... params) {
                List<MyMap> myMaps = new ArrayList<>();
                try {
                    publishProgress(0, 0);
                    URL url = new URL(Variable.getVariable().getFileListURL());
                    publishProgress(80, 0);
                    // Read all the text returned by the server
                    BufferedReader l = new BufferedReader(new InputStreamReader(url.openStream()));
                    int lines = 0;
                    while (l.readLine() != null) lines++;
                    l.close();
                    log("lines: " + lines);
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    publishProgress(100, 0);
                    String str;
                    int i = 0;
                    while ((str = in.readLine()) != null) {
                        int index = str.indexOf("href=\"");
                        if (index >= 0) {
                            index += 6;
                            int lastIndex = str.indexOf(".ghz", index);
                            if (lastIndex >= 0) {
                                int sindex = str.indexOf("right\">", str.length() - 52);
                                int slindex = str.indexOf("M", sindex);
                                String mapName = str.substring(index, lastIndex);
                                boolean downloaded = Variable.getVariable().getLocalMapNameList().contains(mapName);
                                //                            log("downloaded: " + downloaded);
                                String size = "";
                                if (sindex >= 0 && slindex >= 0) {
                                    size = str.substring(sindex + 7, slindex + 1);
                                }
                                MyMap mm = new MyMap(mapName, size, downloaded);
                                if (downloaded) {
                                    myMaps.add(0, mm);
                                } else {
                                    myMaps.add(mm);
                                }
                            }
                        }
                        i++;
                        publishProgress(100, (int) (((float) i / lines) * 100));
                    }
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    MyApp.tracker().send(new HitBuilders.ExceptionBuilder().setDescription(
                            new StandardExceptionParser(getApplicationContext(), null)
                                    .getDescription(Thread.currentThread().getName(), e)).setFatal(false).build());
                }
                return myMaps;
            }

            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                listDownloadPB.setProgress(values[1]);
                listDownloadPB.setSecondaryProgress(values[0]);
                //                log(" update " + values[0]);
                //                log(" update " + values[1]);
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
     * list of countries are ready
     *
     * @param myMaps
     */
    private void listReady(List<MyMap> myMaps) {
        if (myMaps.isEmpty()) {
            Toast.makeText(this, "There is a problem with the server, please report this to app developer!",
                    Toast.LENGTH_SHORT).show();
        } else {
            log(myMaps.toString());
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
    }

    @Override public void mapFABonClick(View view) {
        try {
            log("on fab click!");
            // load map
            itemPosition = mapsRV.getChildAdapterPosition(view);
            activeDownload(view, itemPosition);
        } catch (Exception e) {e.getStackTrace();}
    }

    /**
     * download map
     *
     * @param view
     * @param position
     */
    private void activeDownload(View view, int position) {
        if (vh != view && !Variable.getVariable().isDownloading()) {
            vh = view;
            MyMap myMap = myDownloadAdapter.getItem(position);
            if (!myMap.isDownloaded()) {
                this.downloadStatus = (TextView) vh.findViewById(R.id.my_download_item_download_status);
                downloadStatus.setText("Downloading file ...");
                myDownloadAdapter.getItem(itemPosition).setDownloading(true);
                initProgressBar((ProgressBar) vh.findViewById(R.id.my_download_item_progress_bar));
                DownloadFiles.getDownloader()
                        .downloadMap(Variable.getVariable().getMapsFolder(), myMap.getMapName(), myMap.getUrl(), this);
            }
        }
    }

    /**
     * init progress bar and set visible
     */
    private void initProgressBar(ProgressBar progressBar) {
        this.itemDownloadPB = progressBar;
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.VISIBLE);
    }


    public void downloadStart() {
        try {

        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public void downloadFinished() {
        try {
            vh = null;
            MyMap mm = myDownloadAdapter.remove(itemPosition);
            mm.setDownloaded(true);
            myDownloadAdapter.insert(mm);
            log("download finish handled");
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public void progressBarOnUpdate(Integer value) {
        try {
            if (itemDownloadPB != null) {
                itemDownloadPB.setProgress(value);
                downloadStatus.setText("Downloading file " + String.format("%1$" + 3 + "s", value) + "%");
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public void onStop() {
        log("on Stop !!");
        super.onStop();
        if (Variable.getVariable().isDownloading()) {
            try {
                Variable.getVariable().setCloudMaps(myDownloadAdapter.getMaps());
                vh = null;
                log("on save instance state");
                finish();
            } catch (Exception e) {
                e.getStackTrace();
            }
        }
        DownloadFiles.getDownloader().removeListener(this);
    }

    /**
     * Recycler view is ready to use
     *
     * @param downloadStatus
     * @param progressBar
     */
    public void progressbarReady(TextView downloadStatus, ProgressBar progressBar) {
        // do it only when downloading not yet finished
        if (Variable.getVariable().isDownloading()) {
            try {
                this.downloadStatus = downloadStatus;
                initProgressBar(progressBar);
                this.vh = (View) progressBar.getParent();
                itemPosition = mapsRV.getChildAdapterPosition(vh);
            } catch (Exception e) {e.getStackTrace();}
        }
    }

    private void log(String s) {
        System.out.println(this.getClass().getSimpleName() + "-------------------" + s);
    }
}