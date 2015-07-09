package com.junjunguo.pocketmaps.controller;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.map.AndroidDownloader;
import com.junjunguo.pocketmaps.model.map.DownloadFiles;
import com.junjunguo.pocketmaps.model.util.MapDownloadListener;
import com.junjunguo.pocketmaps.model.util.MyApp;
import com.junjunguo.pocketmaps.model.util.MyDownloadAdapter;
import com.junjunguo.pocketmaps.model.util.MyMap;
import com.junjunguo.pocketmaps.model.util.OnDownloading;
import com.junjunguo.pocketmaps.model.util.OnDownloadingListener;
import com.junjunguo.pocketmaps.model.util.RVItemTouchListener;
import com.junjunguo.pocketmaps.model.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.model.util.Variable;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadMapActivity extends AppCompatActivity implements MapDownloadListener, OnDownloadingListener {
    private MyDownloadAdapter myDownloadAdapter;

    /**
     * used to show or hide item download action
     */
    private View vh;
    private int itemPosition;
    private ProgressBar progressBar;
    private TextView downloadStatus;
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
        //        String currentDownloadingMapName = Variable.getVariable().getCurrentDownloadingMapName();
        //        log("@@: saved: " + currentDownloadingMapName);
        //        if (Variable.getVariable().isDownloading() && Variable.getVariable().getView() != null) {
        OnDownloading.getOnDownloading().setListener(this);
        List cloudMaps = Variable.getVariable().getCloudMaps();
        if (Variable.getVariable().isDownloading() && cloudMaps != null && !cloudMaps.isEmpty()) {
            try {
                // Restore value of members from saved state
                //                itemPosition = Variable.getVariable().getItemPosition();
                //            vh = Variable.getVariable().getView();
                //            progressBar = Variable.getVariable().getProgressBar();
                //                log("$progress bar on create (old)" + Variable.getVariable().getProgressBar() +
                // "id: " +
                //                        Variable.getVariable().getProgressBar().getId());
                activeRecyclerView(cloudMaps);
                //                itemPosition = myDownloadAdapter.getPosition(currentDownloadingMapName);
                //                log("$item position: " + itemPosition);

                //                vh = mapsRV.getChildAt(itemPosition);
                //                vh = (View) myDownloadAdapter.getDownloadingHolder();
                //                mapsRV.getChildViewHolder();
                //                mapsRV.getChildAt();
                //                log("mapsRV child count " + mapsRV.getChildCount());
                //                log("mapsRV.getLayoutManager().getChild count " + mapsRV.getLayoutManager()
                // .getChildCount());
                //                log("maps rv to string: " + mapsRV.toString());
                //                vh = mapsRV.getChildAt(itemPosition);
                //                log("$vh: " + vh);

                //                progressBar = Variable.getVariable().getDownloadingProgressBar();
                //                log("$$ progress bar: " + progressBar);
                //                vh = (View) progressBar.getParent();
                //                itemPosition = mapsRV.getChildAdapterPosition(vh);

                //                initProgressBar();
            } catch (Exception e) {e.getStackTrace();}
        } else {
            //  initialize members with default values for a new instance
            vh = null;
            itemPosition = 0;
            downloadList();
            activeRecyclerView(new ArrayList());
        }
        DownloadFiles.getDownloader().addListener(this);
        Variable.getVariable().setCloudMaps(new ArrayList<MyMap>());
        //        Variable.getVariable().setView(null);
        //        Variable.getVariable().setDownloadAdapter(null);
        //        Variable.getVariable().setDownloadingProgressBar(null);
    }

    //    public void test() {
    //        activeRecyclerView(Variable.getVariable().getCloudMaps());
    //        List<MyMap> cloudMaps = Variable.getVariable().getCloudMaps();
    //        MyMap downloadingMap = null;
    //        for (MyMap mm : cloudMaps) {
    //            if (mm.isDownloading()) {
    //                downloadingMap = mm;
    //                break;
    //            }
    //        }
    //        for (int i = 0; i < mapsRV.getChildCount(); i++) {
    //            mapsRV.getChildAt(i).getId();
    //            myDownloadAdapter.getItemId(i);
    //        }
    ////        mapsRV.getLayoutManager().getChildAt();
    //    }

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
                String[] lines = new String[0];
                try {
                    lines = new AndroidDownloader().downloadAsString(Variable.getVariable().getFileListURL())
                            .split("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                    MyApp.tracker().send(new HitBuilders.ExceptionBuilder().setDescription(
                            new StandardExceptionParser(getApplicationContext(), null)
                                    .getDescription(Thread.currentThread().getName(), e)).setFatal(false).build());
                }
                List<MyMap> myMaps = new ArrayList<>();
                for (String str : lines) {
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
                }
                return myMaps;
            }

            @Override protected void onPostExecute(List<MyMap> myMaps) {
                super.onPostExecute(myMaps);
                listReady(myMaps);
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
        myDownloadAdapter = new MyDownloadAdapter(myMaps);
        mapsRV.setAdapter(myDownloadAdapter);
        onItemTouchHandler(mapsRV);
        log("active recycler view");
        log("mapsRV child count " + mapsRV.getChildCount());
        log("mapsRV.getLayoutManager().getChild count " + mapsRV.getLayoutManager().getChildCount());
        log("maps rv to string: " + mapsRV.toString());

    }

    /**
     * perform actions when item touched
     *
     * @param mapsRV
     */
    private void onItemTouchHandler(RecyclerView mapsRV) {
        mapsRV.addOnItemTouchListener(new RVItemTouchListener(new RVItemTouchListener.OnItemTouchListener() {
            public boolean onItemTouch(View view, int position, MotionEvent e) {
                itemPosition = position;
                if (vh != view) {
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            view.setBackgroundColor(getResources().getColor(R.color.my_primary_light));
                            return true;
                        case MotionEvent.ACTION_UP:
                            log("onitem touch view: " + view + "  position: " + position);
                            view.setBackgroundColor(getResources().getColor(R.color.my_icons));
                            activeDownload(view, position);
                            return true;
                    }
                }
                return false;
            }
        }));
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
                //                setProgressBar((ProgressBar) vh.findViewById(R.id.my_download_item_progress_bar));
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
        this.progressBar = progressBar;
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.VISIBLE);
        log("progress bar on init (new)" + progressBar + "id: " + progressBar.getId());
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
            Variable.getVariable().addRecentDownloadedMap(mm);
            log("download finish handled");
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public void progressBarOnUpdate(Integer value) {
        try {
            if (progressBar != null) {
                progressBar.setProgress(value);
                downloadStatus.setText("Downloading file " + String.format("%1$" + 3 + "s", value) + "%");
                //                log("##get progress :" + progressBar.getProgress());
            }
        } catch (Exception e) {
            e.getStackTrace();
        }

    }

    protected void onStart() {
        super.onStart();
        log("on start");
        //        log("$$ progress bar: " + progressBar);

    }

    protected void onResume() {
        super.onResume();
        log("on resume");
        //        log("$$ progress bar: " + progressBar);
    }

    protected void onPause() {
        super.onPause();
        log("on pause");
        //        log("$$ progress bar: " + progressBar);
    }

    public void onStop() {
        log("on Stop !!");
        super.onStop();
        //        if (Variable.getVariable().isDownloading()) {
        try {
            //            Variable.getVariable().setView(vh);
            Variable.getVariable().setCloudMaps(myDownloadAdapter.getMaps());
            //            Variable.getVariable().setItemPosition(itemPosition);
            //            Variable.getVariable().setProgressBar(progressBar);
            //            Variable.getVariable().setCurrentDownloadingMapName(myDownloadAdapter.getItem(itemPosition)
            // .getMapName());

            vh = null;
            log("on save instance state");
            finish();
        } catch (Exception e) {
            e.getStackTrace();
        }
        //        }
        DownloadFiles.getDownloader().removeListener(this);
    }

    private void log(String s) {
        System.out.println(this.getClass().getSimpleName() + "-------------------" + s);
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
}