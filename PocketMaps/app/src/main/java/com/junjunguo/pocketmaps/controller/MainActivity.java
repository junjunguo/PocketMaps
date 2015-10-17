package com.junjunguo.pocketmaps.controller;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.dataType.MyMap;
import com.junjunguo.pocketmaps.model.listeners.MapDownloadListener;
import com.junjunguo.pocketmaps.model.listeners.MapFABonClickListener;
import com.junjunguo.pocketmaps.model.map.DownloadFiles;
import com.junjunguo.pocketmaps.model.map.MapHandler;
import com.junjunguo.pocketmaps.model.map.MyMapAdapter;
import com.junjunguo.pocketmaps.model.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.model.util.Variable;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 04, 2015.
 */
public class MainActivity extends AppCompatActivity implements MapDownloadListener, MapFABonClickListener {
    private MyMapAdapter mapAdapter;
    //    private Location mLastLocation;           ?
    private boolean changeMap;
    private RecyclerView mapsRV;
    protected Context context = this;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Variable.getVariable().setContext(getApplicationContext());
        // set status bar
        new SetStatusBarColor().setStatusBarColor(findViewById(R.id.statusBarBackgroundMain),
                getResources().getColor(R.color.my_primary_dark), this);

        //         greater Or Equal to Kitkat
        if (Build.VERSION.SDK_INT >= 19) {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(this, "Pocket Maps is not usable without an external storage!", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            Variable.getVariable().setMapsFolder(
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            Variable.getVariable().getMapDirectory()));
            Variable.getVariable().setTrackingFolder(
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            Variable.getVariable().getTrackingDirectory()));
        } else Variable.getVariable().setMapsFolder(
                new File(Environment.getExternalStorageDirectory(), Variable.getVariable().getMapDirectory()));

        if (!Variable.getVariable().getMapsFolder().exists()) Variable.getVariable().getMapsFolder().mkdirs();
        activeAddBtn();
        activeRecyclerView(new ArrayList());
        generateList();
        //        vh = null;
        changeMap = getIntent().getBooleanExtra("SELECTNEWMAP", false);
        // start map activity if load succeed
        if (Variable.getVariable().loadVariables() && !changeMap) {
            startMapActivity();
        }
    }

    /**
     * add button will move user to download activity
     */
    private void activeAddBtn() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.my_maps_add_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startDownloadActivity();
            }
        });
    }

    /**
     * choose area form local files
     */
    private void generateList() {
        if (Variable.getVariable().getLocalMaps().isEmpty()) {
            refreshList();
        } else {
            mapAdapter.addAll(Variable.getVariable().getLocalMaps());
        }
    }

    /**
     * read local files and build a list then add the list to mapAdapter
     */
    private void refreshList() {
        String[] files = Variable.getVariable().getMapsFolder().list(new FilenameFilter() {
            @Override public boolean accept(File dir, String filename) {
                return (filename != null && (filename.endsWith("-gh")));
            }
        });
        for (String file : files) {
            Variable.getVariable().addLocalMap(new MyMap(file));
        }
        if (!Variable.getVariable().getLocalMaps().isEmpty()) {
            mapAdapter.addAll(Variable.getVariable().getLocalMaps());
        }
    }


    /**
     * active directions, and directions view
     */
    private void activeRecyclerView(List myMaps) {
        RecyclerView.LayoutManager layoutManager;

        mapsRV = (RecyclerView) findViewById(R.id.my_maps_recycler_view);
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(2000);
        animator.setRemoveDuration(600);
        mapsRV.setItemAnimator(animator);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mapsRV.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        mapsRV.setLayoutManager(layoutManager);
        // specify an adapter (see also next example)
        mapAdapter = new MyMapAdapter(myMaps, this);
        mapsRV.setAdapter(mapAdapter);

        deleteItemHandler();
    }

    /**
     * swipe to right or left to delete item & AlertDialog to confirm
     */
    private void deleteItemHandler() {
        // swipe left or right to remove an item
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        //Remove swiped item from list and notify the RecyclerView
                        MyMap mm = mapAdapter.remove(mapsRV.getChildAdapterPosition(viewHolder.itemView));  //
                        // remove from adapter
                        Variable.getVariable().removeLocalMap(mm);
                        recursiveDelete(new File(mm.getUrl()));
                    }
                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        itemTouchHelper.attachToRecyclerView(mapsRV);
    }

    @Override public void mapFABonClick(View view) {
        try {
            // load map
            Variable.getVariable().setPrepareInProgress(true);

            int position = mapsRV.getChildAdapterPosition(view);
            //            log(mapAdapter.getItem(position).getMapName() + " - " + "chosen");
            Variable.getVariable().setCountry(mapAdapter.getItem(position).getMapName());
            if (changeMap) {
                Variable.getVariable().setLastLocation(null);
                //                log("last location " + Variable.getVariable().getLastLocation());
                MapHandler.reset();
                System.gc();
            }
            startMapActivity();
        } catch (Exception e) {e.getStackTrace();}
    }

    /**
     * delete a recursively delete a folder or file
     *
     * @param fileOrDirectory
     */
    public void recursiveDelete(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) for (File child : fileOrDirectory.listFiles())
            recursiveDelete(child);
        try {
            fileOrDirectory.delete();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    /**
     * move to download activity
     */
    private void startDownloadActivity() {
        if (isOnline()) {
            Intent intent = new Intent(this, DownloadMapActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Add new Map need internet connection!", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * move to map screen
     */
    private void startMapActivity() {
        Intent intent = new Intent(this, MapActivity.class);
        // clear every thing before start map activity
        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                //                got to setting;
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_home_page:
                //                got to home page;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://junjunguo.com/PocketMaps/")));
                return true;
            case R.id.menu_rate_pocket_maps:
                Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
                }
                return true;
            case R.id.menu_quit:
                quitApp();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onResume() {
        super.onResume();
        //        log("on resume");
        addRecentDownloadedFiles();
        DownloadFiles.getDownloader().addListener(this);
    }

    protected void onPause() {
        super.onPause();
        //        log("on pause");
        DownloadFiles.getDownloader().removeListener(this);
    }

    /**
     * finish all activities ( quit the app )
     */
    private void quitApp() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("ACTION_QUIT");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

        finish();
        System.exit(0);
    }

    /**
     * @return true is there is a network connection
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void downloadStart() {
    }

    public void downloadFinished(String mapName) {
        //        log("add recent downloaded files called from Main implement download finished!");
        addRecentDownloadedFiles();
    }

    public void progressUpdate(Integer value) {

    }

    /**
     * add recent downloaded files from Download activity(if any)
     */
    private void addRecentDownloadedFiles() {
        try {
            //            log("add recent downloaded files!");
            for (int i = Variable.getVariable().getRecentDownloadedMaps().size() - 1; i >= 0; i--) {
                MyMap mm = Variable.getVariable().removeRecentDownloadedMap(i);
                mapAdapter.insert(mm);
                Variable.getVariable().addLocalMap(mm);
                //                log("add recent downloaded files: " + mm.toString());
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }


    /**
     * send message to logcat
     *
     * @param str
     */
    private void log(String str) {
        Log.i(this.getClass().getSimpleName(), "-------" + str);
    }
}