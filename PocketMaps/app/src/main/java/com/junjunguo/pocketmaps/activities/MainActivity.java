package com.junjunguo.pocketmaps.activities;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.MyMap;
import com.junjunguo.pocketmaps.model.listeners.OnClickMapListener;
import com.junjunguo.pocketmaps.map.MapHandler;
import com.junjunguo.pocketmaps.fragments.MessageDialog;
import com.junjunguo.pocketmaps.fragments.MyMapAdapter;
import com.junjunguo.pocketmaps.util.IO;
import com.junjunguo.pocketmaps.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.util.Variable;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows all local-available maps on a list.
 * <br/>Allows to load a map.
 * 
 * <p/>This file is part of PocketMaps
 * <br/>Created by GuoJunjun <junjunguo.com> on July 04, 2015.
 */
public class MainActivity extends AppCompatActivity implements OnClickMapListener {
    public final static int ITEM_TOUCH_HELPER_LEFT = 4;
    public final static int ITEM_TOUCH_HELPER_RIGHT = 8;
    private MyMapAdapter mapAdapter;
    private boolean changeMap;
    private RecyclerView mapsRV;
    private boolean activityLoaded = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        continueActivity();
    }
    
    boolean continueActivity()
    {
      if (activityLoaded) { return true; }
      String sPermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
      if (!Permission.checkPermission(sPermission, this))
      {
        String sPermission2 = android.Manifest.permission.ACCESS_FINE_LOCATION;
        Permission.startRequest(new String[]{sPermission, sPermission2}, true, this);
        return false;
      }
        Variable.getVariable().setContext(getApplicationContext());
        // set status bar
        new SetStatusBarColor().setStatusBarColor(findViewById(R.id.statusBarBackgroundMain),
                getResources().getColor(R.color.my_primary_dark), this);

        File defMapsDir = getDefaultBaseDirectory(this);
        if (defMapsDir==null) { return false; }
        Variable.getVariable().setBaseFolder(defMapsDir.getPath());

        if (!Variable.getVariable().getMapsFolder().exists())
        {
          Variable.getVariable().getMapsFolder().mkdirs();
        }
        boolean loadSuccess = Variable.getVariable().loadVariables();
        activateAddBtn();
        activateRecyclerView(new ArrayList<MyMap>());
        generateList();
        //        vh = null;
        changeMap = getIntent().getBooleanExtra("com.junjunguo.pocketmaps.activities.MapActivity.SELECTNEWMAP", true);
        // start map activity if load succeed
        if (loadSuccess && !changeMap) {
            startMapActivity();
        }
        activityLoaded = true;
        return true;
    }

    public static File getDefaultBaseDirectory(Context context)
    {
      // greater or equal to Kitkat
      if (Build.VERSION.SDK_INT >= 19)
      {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
          Toast.makeText(context, "Pocket Maps is not usable without an external storage!", Toast.LENGTH_SHORT).show();
          return null;
        }
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
      }
      else
      {
        return Environment.getExternalStorageDirectory();
      }
    }

    /**
     * add button will move user to download activity
     */
    private void activateAddBtn() {
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
        if (files==null)
        {
          // Array 'files' was null on a test device.
          log("Warning: mapsFolder does not exist!");
          files = new String[0];
        }
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
    private void activateRecyclerView(List<MyMap> myMaps) {
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
      OnItemClickListener l = new OnItemClickListener()
      {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
          MyMap mm = mapAdapter.remove(position);
          Variable.getVariable().removeLocalMap(mm);
          File mapsFolder = MyMap.getMapFile(mm, MyMap.MapFileType.MapFolder);
          recursiveDelete(mapsFolder);
          mm.setStatus(MyMap.DlStatus.On_server);
          log("RecursiveDelete: " + mm.getMapName());
        }
      };
      addDeleteItemHandler(this, mapsRV, l);
    }
    
    public static void addDeleteItemHandler(final Context context, final RecyclerView recView, final OnItemClickListener l) {
        // swipe left or right to remove an item
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ITEM_TOUCH_HELPER_LEFT | ITEM_TOUCH_HELPER_RIGHT) {
                    @Override public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                        builder1.setMessage(R.string.delete_msg);
                        builder1.setCancelable(true);
  
                        builder1.setPositiveButton(
                            R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    //Remove swiped item from list and notify the RecyclerView
                                    l.onItemClick(null, viewHolder.itemView, viewHolder.getAdapterPosition(), viewHolder.getItemId());
                                }
                            });
  
                        builder1.setNegativeButton(
                            R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    int vhPos = viewHolder.getAdapterPosition();
                                    recView.getAdapter().notifyItemRemoved(vhPos);
                                    recView.getAdapter().notifyItemInserted(vhPos);
                                }
                            });
  
                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    }
                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        itemTouchHelper.attachToRecyclerView(recView);
    }

    @Override public void onClickMap(View view, int position, TextView tv) {
        try {
            // load map
            //            log(mapAdapter.getItem(position).getMapName() + " - " + "chosen");
            MyMap myMap = mapAdapter.getItem(position);
            if (MyMap.isVersionCompatible(myMap.getMapName()))
            {
              Variable.getVariable().setPrepareInProgress(true);
              Variable.getVariable().setCountry(myMap.getMapName());
              if (changeMap)
              {
                Variable.getVariable().setLastLocation(null);
                //                log("last location " + Variable.getVariable().getLastLocation());
                MapHandler.reset();
                System.gc();
              }
              startMapActivity();
            }
            else
            {
              logUser("Map is not compatible with this version!\nPlease update map!");
            }
            myMap.checkUpdateAvailableMsg(this);
        } catch (Exception e) {e.printStackTrace();}
    }

    /**
     * delete a recursively delete a folder or file
     *
     * @param fileOrDirectory
     */
    public void recursiveDelete(File fileOrDirectory)
    {
      if (fileOrDirectory.isDirectory())
      {
        for (File child : fileOrDirectory.listFiles())
        {
          recursiveDelete(child);
        }
      }
      try
      {
        fileOrDirectory.delete();
      }
      catch (Exception e)
      {
        e.printStackTrace();
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
            case R.id.menu_home_page:
                //                got to home page;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://junjunguo.com/PocketMaps/")));
                return true;
            case R.id.menu_rate_pocket_maps:
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }
                return true;

            case R.id.menu_about_pocket_maps:
                //                got to about view;
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.menu_switch_maps_dir:
                final File oldFile = Variable.getVariable().getMapsFolder();
                IO.showRootfolderSelector(this, false, new Runnable()
                {
                  @Override public void run()
                  {

                    if (!oldFile.equals(Variable.getVariable().getMapsFolder()))
                    {
                      int icount = mapAdapter.getItemCount();
                      if (icount > 0)
                      {
                        mapAdapter.removeAll();
                        mapAdapter.notifyItemRangeRemoved(0, icount);
                      }
                      generateList();
                      icount = mapAdapter.getItemCount();
                      mapAdapter.notifyItemRangeInserted(0, icount);
                    }
                  }
                });
                return true;
            case R.id.menu_quit:
                quitApp();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (continueActivity())
        {
          addRecentDownloadedFiles();
          MessageDialog.showMsg(this, "mapDeleteMsg", R.string.swipe_out, true);
        }
    }

    @Override protected void onPause() {
        super.onPause();
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
        addRecentDownloadedFiles();
    }

    public void progressUpdate(Integer value) {
    }

    /**
     * add recent downloaded files from Download activity(if any)
     */
    private void addRecentDownloadedFiles() {
        try {
            for (MyMap curMap : Variable.getVariable().getRecentDownloadedMaps())
            {
                mapAdapter.insert(curMap);
                Variable.getVariable().addLocalMap(curMap);
                log("add recent downloaded files: " + curMap.getMapName());
            }
            Variable.getVariable().getRecentDownloadedMaps().clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * send message to logcat
     *
     * @param str
     */
    private static void log(String str) {
        Log.i(MainActivity.class.getName(), str);
    }

    private void logUser(String str) {
      Log.i(MainActivity.class.getName(), str);
      try
      {
        Toast.makeText(getBaseContext(), str, Toast.LENGTH_SHORT).show();
      }
      catch (Exception e) { e.printStackTrace(); }
    }
    
}