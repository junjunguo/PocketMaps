package com.junjunguo.pocketmaps.controller;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.dataType.MyMap;
import com.junjunguo.pocketmaps.model.listeners.MapDownloadListener;
import com.junjunguo.pocketmaps.model.listeners.MapFABonClickListener;
import com.junjunguo.pocketmaps.model.map.DownloadFiles;
import com.junjunguo.pocketmaps.model.util.MyApp;
import com.junjunguo.pocketmaps.model.util.MyMapAdapter;
import com.junjunguo.pocketmaps.model.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.model.util.Variable;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MapDownloadListener,
                   MapFABonClickListener {
    private GoogleApiClient mGoogleApiClient;
    private MyMapAdapter mapAdapter;
    private Location mLastLocation;
    private boolean selectNewMap;
    private RecyclerView mapsRV;
    protected Context context = this;

//    /**
//     * used to show or hide item action
//     */
//    private View vh;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Variable.getVariable().setContext(getApplicationContext());
        // set status bar
        new SetStatusBarColor().setStatusBarColor(findViewById(R.id.statusBarBackgroundMain),
                getResources().getColor(R.color.my_primary_dark), this);
        buildGoogleApiClient();

        //         greater Or Equal to Kitkat
        if (Build.VERSION.SDK_INT >= 19) {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(this, "Pocket Maps is not usable without an external storage!", Toast.LENGTH_SHORT)
                        .show();
                MyApp.tracker().send(new HitBuilders.ExceptionBuilder()
                        .setDescription("Pocket Maps is not usable without an external storage!").setFatal(false)
                        .build());
                return;
            }
            Variable.getVariable().setMapsFolder(
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            Variable.getVariable().getMapDirectory()));
        } else Variable.getVariable().setMapsFolder(
                new File(Environment.getExternalStorageDirectory(), Variable.getVariable().getMapDirectory()));

        if (!Variable.getVariable().getMapsFolder().exists()) Variable.getVariable().getMapsFolder().mkdirs();
        activeAddBtn();
        activeRecyclerView(new ArrayList());
        generateList();
        DownloadFiles.getDownloader().addListener(this);
//        vh = null;
        selectNewMap = getIntent().getBooleanExtra("SELECTNEWMAP", false);
        // start map activity if load succeed
        if (Variable.getVariable().loadVariables() && !selectNewMap) {
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


    @Override public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override public void onConnectionSuspended(int i) {
    }

    @Override public void onConnectionFailed(ConnectionResult connectionResult) {
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
                return (filename != null && (filename.endsWith(".ghz") || filename.endsWith("-gh")));
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
                        //                        // 1. Instantiate an AlertDialog.Builder with its constructor
                        //                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        //
                        //                        // 2. Chain together various setter methods to set the dialog
                        // characteristics
                        //                        builder.setMessage(R.string.delete_map_msg)
                        //                                .setPositiveButton(R.string.ok, new DialogInterface
                        // .OnClickListener() {
                        //                                    public void onClick(DialogInterface dialog, int id) {
                        //                                        // delete map
                        //                                        MyMap mm = mapAdapter
                        //                                                .remove(mapsRV.getChildAdapterPosition
                        // (viewHolder.itemView));  //
                        //                                        // remove from adapter
                        //                                        Variable.getVariable().removeLocalMap(mm);
                        //                                        recursiveDelete(new File(mm.getUrl()));
                        //                                        dialog.dismiss();
                        //                                    }
                        //                                }).setNegativeButton(R.string.cancel, new DialogInterface
                        // .OnClickListener() {
                        //                            public void onClick(DialogInterface dialog, int id) {
                        //                                // User cancelled the dialog
                        //
                        //                                dialog.dismiss();
                        //                            }
                        //                        });
                        //                        // Create the AlertDialog object and return it
                        //
                        //                        // 3. Get the AlertDialog from create()
                        //                        AlertDialog dialog = builder.create();
                        //                        dialog.show();
                    }
                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        itemTouchHelper.attachToRecyclerView(mapsRV);
    }

    //    /**
    //     * perform actions when item touched
    //     *
    //     * @param mapsRV
    //     */
    //    private void onItemTouchHandler(RecyclerView mapsRV) {
    //        mapsRV.addOnItemTouchListener(new RVItemTouchListener(new RVItemTouchListener.OnItemTouchListener() {
    //            public boolean onItemTouch(View view, int position, MotionEvent e) {
    //                if (view != vh) {
    //                    switch (e.getAction()) {
    //                        case MotionEvent.ACTION_POINTER_DOWN:
    //                            view.setBackgroundColor(getResources().getColor(R.color.my_primary_light));
    //                            return true;
    //                        case MotionEvent.ACTION_POINTER_UP:
    //                            view.setBackgroundColor(getResources().getColor(R.color.my_icons));
    //                            itemActions(view, position);
    //                            return true;
    //                    }
    //                }
    //                return false;
    //            }
    //        }));
    //    }

    @Override public void mapFABonClick(View view) {
        try {
            log("on fab click!");
            // load map
//            vh = view;
            Variable.getVariable().setPrepareInProgress(true);

            int position = mapsRV.getChildAdapterPosition(view);
            log(mapAdapter.getItem(position).getMapName() + " - " +
                    "chosen");
            Variable.getVariable().setCountry(mapAdapter.getItem(position).getMapName());
            if (selectNewMap) {
                Variable.getVariable().setLastLocation(null);
                log("last location " + Variable.getVariable().getLastLocation());
            }
            startMapActivity();
        } catch (Exception e) {e.getStackTrace();}
    }

//    /**
//     * item is clicked actions : a new item layout remove, cancel, OK
//     *
//     * @param view
//     * @param position
//     */
//    private void itemActions(View view, final int position) {
//        if (vh == view) {
//
//        } else {
//            if (vh != null) {
//                ViewGroup item = (ViewGroup) vh.findViewById(R.id.my_maps_item_rl);
//                ViewGroup action = (ViewGroup) vh.findViewById(R.id.my_maps_item_action_rl);
//                item.setVisibility(View.INVISIBLE);
//                action.setVisibility(View.VISIBLE);
//            }
//            vh = view;
//            view.setClickable(false);
//            final ViewGroup item = (ViewGroup) view.findViewById(R.id.my_maps_item_rl);
//            final ViewGroup action = (ViewGroup) view.findViewById(R.id.my_maps_item_action_rl);
//            item.setVisibility(View.INVISIBLE);
//            action.setVisibility(View.VISIBLE);
//            Button remove = (Button) view.findViewById(R.id.my_maps_item_action_remove_btn);
//            Button cancel = (Button) view.findViewById(R.id.my_maps_item_action_cancel_btn);
//            Button ok = (Button) view.findViewById(R.id.my_maps_item_action_ok_btn);
//
//            remove.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    // delete map
//                    MyMap mm = mapAdapter.remove(position);  // remove from adapter
//                    Variable.getVariable().removeLocalMap(mm);
//                    recursiveDelete(new File(mm.getUrl()));
//                    //                    resetVH(item, action);
//                }
//            });
//            cancel.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    //                    resetVH(item, action);
//                }
//            });
//            ok.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    //                    resetVH(item, action);
//                    // load map
//                    Variable.getVariable().setPrepareInProgress(true);
//                    log(mapAdapter.getItem(position).getMapName() + " - chosen");
//                    Variable.getVariable().setCountry(mapAdapter.getItem(position).getMapName());
//                    if (selectNewMap) {
//                        Variable.getVariable().setLastLocation(null);
//                        log("last location " + Variable.getVariable().getLastLocation());
//                    }
//                    startMapActivity();
//                }
//            });
//        }
//    }

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

    //
    //    /**
    //     * reset: item visible; action invisible; vh = null;
    //     *
    //     * @param item
    //     * @param action
    //     */
    //    private void resetVH(ViewGroup item, ViewGroup action) {
    //        vh = null;
    //        item.setVisibility(View.VISIBLE);
    //        action.setVisibility(View.INVISIBLE);
    //    }

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
        startActivity(intent);
        finish();
    }

    /**
     * accessing google play services
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient =
                new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API).build();
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
            case R.id.menu_quit:
                quitApp();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onResume() {
        super.onResume();
        addRecentDownloadedFiles();
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

    public void downloadFinished() {
        log("add recent downloaded files called from Main implement download finished!");
        addRecentDownloadedFiles();
    }

    public void progressBarOnUpdate(Integer value) {

    }

    /**
     * add recent downloaded files from Download activity(if any)
     */
    private void addRecentDownloadedFiles() {
        try {
            log("add recent downloaded files!");
            for (int i = Variable.getVariable().getRecentDownloadedMaps().size() - 1; i >= 0; i--) {
                MyMap mm = Variable.getVariable().removeRecentDownloadedMap(i);
                mapAdapter.insert(mm);
                Variable.getVariable().addLocalMap(mm);
                log("add recent downloaded files!" + mm);
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
        Log.i(this.getClass().getSimpleName(), "---------- main activity ----------" + str);
    }

    /**
     * send message to logcat and Toast it on screen
     *
     * @param str: message
     */
    private void logToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        log(str);
    }

}