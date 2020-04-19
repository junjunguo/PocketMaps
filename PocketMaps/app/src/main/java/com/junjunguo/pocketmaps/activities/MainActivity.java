package com.junjunguo.pocketmaps.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.downloader.MapDownloadUnzip;
import com.junjunguo.pocketmaps.downloader.MapDownloadUnzip.StatusUpdate;
import com.junjunguo.pocketmaps.model.MyMap;
import com.junjunguo.pocketmaps.model.MyMap.DlStatus;
import com.junjunguo.pocketmaps.model.MyMap.MapFileType;
import com.junjunguo.pocketmaps.model.listeners.OnClickMapListener;
import com.junjunguo.pocketmaps.navigator.NaviEngine;
import com.junjunguo.pocketmaps.map.MapHandler;
import com.junjunguo.pocketmaps.fragments.Dialog;
import com.junjunguo.pocketmaps.fragments.MessageDialog;
import com.junjunguo.pocketmaps.fragments.MyMapAdapter;
import com.junjunguo.pocketmaps.fragments.VoiceDialog;
import com.junjunguo.pocketmaps.navigator.NaviText;
import com.junjunguo.pocketmaps.navigator.NaviVoice;
import com.junjunguo.pocketmaps.util.IO;
import com.junjunguo.pocketmaps.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.util.Variable;
import com.junjunguo.pocketmaps.util.Variable.VarType;

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
        NaviText.initTextList(this);
        continueActivity();
    }
    
    private PhoneStateListener createCallListener()
    {
      return new PhoneStateListener()
      {
        @Override
        public void onCallStateChanged(int state, String incomingNumber)
        {
          boolean mute = true;
          if (state==TelephonyManager.CALL_STATE_OFFHOOK) {}
          else if (state==TelephonyManager.CALL_STATE_RINGING) {}
          else if (state==TelephonyManager.CALL_STATE_IDLE) { mute = false; }
          NaviEngine.getNaviEngine().setNaviVoiceMute(mute);
        }
      };
    }

    boolean continueActivity()
    {
      if (activityLoaded) { return true; }
      String sPermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
      String sPermission3 = android.Manifest.permission.READ_PHONE_STATE;
      if (!Permission.checkPermission(sPermission, this))
      {
        String sPermission2 = android.Manifest.permission.ACCESS_FINE_LOCATION;
        Permission.startRequest(new String[]{sPermission, sPermission2, sPermission3}, true, this);
        return false;
      }
      if (Permission.checkPermission(sPermission3, this))
      {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(createCallListener(), PhoneStateListener.LISTEN_CALL_STATE);
      }
        Variable.getVariable().setContext(getApplicationContext());
        // set status bar
        new SetStatusBarColor().setStatusBarColor(findViewById(R.id.statusBarBackgroundMain),
                getResources().getColor(R.color.my_primary_dark), this);

        boolean loadSuccess = true;
        if (!Variable.getVariable().isLoaded(VarType.Base))
        {
          loadSuccess = Variable.getVariable().loadVariables(Variable.VarType.Base);
        }
        if (!loadSuccess)
        { // First time app started, or loading-error.
          File defMapsDir = IO.getDefaultBaseDirectory(this);
          if (defMapsDir==null) { return false; }
          Variable.getVariable().setBaseFolder(defMapsDir.getPath());
        }

        if (!Variable.getVariable().getMapsFolder().exists())
        {
          Variable.getVariable().getMapsFolder().mkdirs();
        }
        if (!Variable.getVariable().isLoaded(VarType.Geocode))
        {
          Variable.getVariable().loadVariables(Variable.VarType.Geocode);
        }
        
        activateAddBtn();
        activateRecyclerView(new ArrayList<MyMap>());
        generateList();
        //        vh = null;
        boolean defSelect = Variable.getVariable().getAutoSelectMap();
        changeMap = getIntent().getBooleanExtra("com.junjunguo.pocketmaps.activities.MapActivity.SELECTNEWMAP", !defSelect);
        if (Variable.getVariable().getCountry().isEmpty())
        {
          changeMap = true;
        }
        // start map activity if load succeed
        if (loadSuccess)
        {
            if (MapActivity.isMapAlive()) { startMapActivity(); } // Continue map
            else if (!changeMap) { startMapActivity(); }
        }
        activityLoaded = true;
        return true;
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
          generateListNow();
        } else {
            mapAdapter.addAll(Variable.getVariable().getLocalMaps());
        }
    }

    /**
     * read local files and build a list then add the list to mapAdapter
     */
    private void generateListNow() {
        String[] files = Variable.getVariable().getMapsFolder().list(new FilenameFilter() {
            @Override public boolean accept(File dir, String filename) {
                return (filename != null && (filename.endsWith("-gh")));
            }
        });
        if (files==null)
        {
          // Array 'files' was null on a test device.
          log("Warning: mapsFolder does not exist: " + Variable.getVariable().getMapsFolder());
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
        if (mapAdapter == null)
        {
          mapAdapter = new MyMapAdapter(myMaps, this, false);
        }
        else
        {
          mapAdapter.clearList();
          mapAdapter.addAll(myMaps);
        }
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
          clearLocalMap(mm);
        }
      };
      addDeleteItemHandler(this, mapsRV, l);
    }
    
    public static void clearLocalMap(MyMap mm)
    {
      Variable.getVariable().removeLocalMap(mm);
      File mapsFolder = MyMap.getMapFile(mm, MyMap.MapFileType.MapFolder);
      mm.setStatus(MyMap.DlStatus.On_server);
      int index = Variable.getVariable().getCloudMaps().indexOf(mm);
      if (index >= 0)
      { // Get same MyMap from CloudList.
        mm = Variable.getVariable().getCloudMaps().get(index);
        mm.setStatus(MyMap.DlStatus.On_server);
      }
      recursiveDelete(mapsFolder);
      log("RecursiveDelete: " + mm.getMapName());
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

    @Override public void onClickMap(int position)
    {
        try
        {
            MyMap myMap = mapAdapter.getItem(position);
            if (startMapActivityCheck(myMap))
            {
              startMapActivity();
            }
        }
        catch (Exception e) {e.printStackTrace();}
    }

    /**
     * delete a recursively delete a folder or file
     *
     * @param fileOrDirectory
     */
    private static void recursiveDelete(File fileOrDirectory)
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
    
    private boolean startMapActivityCheck(MyMap myMap)
    {
      if (MyMap.isVersionCompatible(myMap.getMapName()))
      {
        Variable.getVariable().setPrepareInProgress(true);
        Variable.getVariable().setCountry(myMap.getMapName());
        if (changeMap)
        {
          Variable.getVariable().setLastLocation(null);
          MapHandler.reset();
          System.gc();
        }
        return true;
      }
      else
      {
        logUser("Map is not compatible with this version!\nPlease update map!");
      }
      myMap.checkUpdateAvailableMsg(this);
      return false;
    }

    /**
     * move to map screen
     */
    private void startMapActivity() {
        Intent intent = new Intent(this, MapActivity.class);
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
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://github.com/junjunguo/PocketMaps/")));
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
                        mapAdapter.clearList();
                        mapAdapter.notifyItemRangeRemoved(0, icount);
                      }
                      copyFavourites(oldFile);
                      generateList();
                      icount = mapAdapter.getItemCount();
                      mapAdapter.notifyItemRangeInserted(0, icount);
                    }
                  }
                });
                return true;
            case R.id.menu_voices:
                VoiceDialog.showTtsVoiceSelector(this);
                return true;
            case R.id.menu_autoselect_map:
                Dialog.showAutoSelectMapSelector(this);
                return true;
            case R.id.menu_units:
                Dialog.showUnitTypeSelector(this);
                return true;
            case R.id.menu_help:
                startActivity(new Intent(Intent.ACTION_VIEW,
                      Uri.parse("https://github.com/junjunguo/PocketMaps/blob/master/documentation/index.md")));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Ensure, that Properties are the same on all mapDirs, otherwise override is possible.
    protected void copyFavourites(File oldMapDir)
    {
      String newFolder = Variable.getVariable().getMapsFolder().getParent();
      String oldFolder = oldMapDir.getParent();
      File oldFavourites = new File(oldFolder, "Favourites.properties");
      File newFavourites = new File(newFolder, "Favourites.properties");
      if (!oldFavourites.isFile()) { return; }
      String dataFavourites = IO.readFromFile(oldFavourites, "\n");
      if (dataFavourites == null)
      {
        log("Error, cannot transfer favourites!");
        return;
      }
      if (dataFavourites.isEmpty()) { return; }
      IO.writeToFile(dataFavourites, newFavourites, false);
    }

    @Override protected void onResume() {
        super.onResume();
        if (continueActivity())
        {
          addRecentDownloadedFiles();
          checkMissingMaps();
          if (mapAdapter!=null && mapAdapter.getItemCount()>0)
          {
            MessageDialog.showMsg(this, "mapDeleteMsg", R.string.swipe_out, true);
          }
        }
    }

    private void checkMissingMaps()
    {
      boolean hasUnfinishedMaps = false;
      File[] fileList = Variable.getVariable().getDownloadsFolder().listFiles();
      if (fileList == null)
      {
        log("WARNING: Downloads-folder access-error!");
        return;
      }
      for (File file : Variable.getVariable().getDownloadsFolder().listFiles())
      {
        if (file.isFile())
        {
          if (file.getName().endsWith(".id"))
          {
            hasUnfinishedMaps = true;
            break;
          }
        }
      }
      if (hasUnfinishedMaps)
      {
        for (MyMap curMap : Variable.getVariable().getCloudMaps())
        {
          File idFile = MyMap.getMapFile(curMap, MapFileType.DlIdFile);
          if (idFile.exists())
          {
            MapDownloadUnzip.checkMap(this, curMap, createStatusUpdater());
          }
        }
      }
    }

    private StatusUpdate createStatusUpdater()
    {
      return new StatusUpdate()
      {
        @Override
        public void logUserThread(String txt)
        {
          MainActivity.this.logUserThread(txt);
        }

        @Override
        public void updateMapStatus(MyMap map)
        {
          MainActivity.this.logUserThread(map.getMapName() + ": " + map.getStatus());
          if (map.getStatus() == DlStatus.Complete)
          {
            mapAdapter.insert(map);
          }
        }

        @Override
        public void onRegisterBroadcastReceiver(Activity activity, MyMap myMap, long enqueueId)
        {
        }
      };
    }

    @Override protected void onPause() {
        super.onPause();
    }

    /**
     * @return true is there is a network connection
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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
    
    private void logUserThread(final String str) {
      runOnUiThread(new Runnable() {
        @Override public void run()
        {
          logUser(str);
        }});
    }
    
}
