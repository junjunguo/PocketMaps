package com.junjunguo.pocketmaps.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.util.MyMap;
import com.junjunguo.pocketmaps.model.util.MyMapAdapter;
import com.junjunguo.pocketmaps.model.util.SetStatusBarColor;
import com.junjunguo.pocketmaps.model.util.Variable;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Variable.getVariable().setContext(getApplicationContext());
        // start map activity if load succeed
        if (Variable.getVariable().loadVariables()) {
            //            startMapActivity();
        }
        // set status bar
        new SetStatusBarColor().setSystemBarColor(findViewById(R.id.statusBarBackgroundMain),
                getResources().getColor(R.color.my_primary_dark), this);
        buildGoogleApiClient();

        //         greater Or Equal to Kitkat
        if (Build.VERSION.SDK_INT >= 19) {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(this, "Offline Map is not usable without an external storage!", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            Variable.getVariable().setMapsFolder(
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            Variable.getVariable().getMapDirectory()));
        } else Variable.getVariable().setMapsFolder(
                new File(Environment.getExternalStorageDirectory(), Variable.getVariable().getMapDirectory()));

        if (!Variable.getVariable().getMapsFolder().exists()) Variable.getVariable().getMapsFolder().mkdirs();
        generateList();
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
        List<MyMap> myMaps = new ArrayList<>();
        String[] files = Variable.getVariable().getMapsFolder().list(new FilenameFilter() {
            @Override public boolean accept(File dir, String filename) {
                return (filename != null && (filename.endsWith(".ghz") || filename.endsWith("-gh")));
            }
        });
        for (String file : files) {
            myMaps.add(new MyMap(file));
        }
        if (!myMaps.isEmpty()) {
            activeRecyclerView(myMaps);
        }
    }

    /**
     * active directions, and directions view
     */
    private void activeRecyclerView(List myMaps) {
        RecyclerView mapsRV;
        RecyclerView.Adapter adapter;
        RecyclerView.LayoutManager layoutManager;

        mapsRV = (RecyclerView) findViewById(R.id.my_maps_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mapsRV.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        mapsRV.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        adapter = new MyMapAdapter(myMaps);
        mapsRV.setAdapter(adapter);
    }


    //    /**
    //     * when an Area (country) is chosen
    //     *
    //     * @param button     btn select local map
    //     * @param spinner    list of countries
    //     * @param nameList   list of names
    //     * @param mylistener MySpinnerListener
    //     */
    //    private void chooseLocalArea(Button button, final Spinner spinner, List<String> nameList,
    //            final MySpinnerListener mylistener) {
    //
    //        final Map<String, String> nameToFullName = new TreeMap<String, String>();
    //        for (String fullName : nameList) {
    //            String tmp = Helper.pruneFileEnd(fullName);
    //            if (tmp.endsWith("-gh")) tmp = tmp.substring(0, tmp.length() - 3);
    //
    //            tmp = AndroidHelper.getFileName(tmp);
    //            nameToFullName.put(tmp, fullName);
    //        }
    //        nameList.clear();
    //        nameList.addAll(nameToFullName.keySet());
    //        ArrayAdapter<String> spinnerArrayAdapter =
    //                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, nameList);
    //        spinner.setAdapter(spinnerArrayAdapter);
    //        button.setOnClickListener(new View.OnClickListener() {
    //            @Override public void onClick(View v) {
    //                Object o = spinner.getSelectedItem();
    //                if (o != null && o.toString().length() > 0 && !nameToFullName.isEmpty()) {
    //                    String area = o.toString();
    //                    mylistener.onSelect(area, nameToFullName.get(area));
    //                    startMapActivity();
    //                } else {
    //                    mylistener.onSelect(null, null);
    //                }
    //            }
    //        });
    //    }


    /**
     * move to map screen
     */
    private void startMapActivity() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
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
                return true;
            case R.id.menu_quit:
                quitApp();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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


    //
    //    /**
    //     * send message to logcat
    //     *
    //     * @param str
    //     */
    //    private void log(String str) {
    //        Log.i(this.getClass().getSimpleName(), str);
    //    }
    //
    //    private void log(String str, Throwable t) {
    //        Log.i(this.getClass().getSimpleName(), str, t);
    //    }
    //
    //    /**
    //     * send message to logcat and Toast it on screen
    //     *
    //     * @param str: message
    //     */
    //    private void logToast(String str) {
    //        log(str);
    //        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    //    }

    /**
     //     * list of area (countries) from server
     //     */
    //    private void chooseAreaFromRemote() {
    //        new GHAsyncTask<Void, Void, List<String>>() {
    //            protected List<String> saveDoInBackground(Void... params) throws Exception {
    //                String[] lines =
    //                        new AndroidDownloader().downloadAsString(Variable.getVariable().getFileListURL()).split
    // ("\n");
    //                List<String> res = new ArrayList<String>();
    //                List<MyMap> myMaps = new ArrayList<>();
    //                for (String str : lines) {
    //                    int index = str.indexOf("href=\"");
    //                    if (index >= 0) {
    //                        index += 6;
    //                        int lastIndex = str.indexOf(".ghz", index);
    //                        if (lastIndex >= 0) {
    //                            res.add(prefixURL + str.substring(index, lastIndex) + ".ghz");
    //                            int sindex = str.indexOf("right\">", str.length() - 52);
    //                            int slindex = str.indexOf("M", sindex);
    //                            myMaps.add(
    //                                    new MyMap(str.substring(index, lastIndex), str.substring(sindex + 7,
    // slindex + 1)));
    //                        }
    //                    }
    //                }
    //                System.out.println(">>>>>>>>>>>" + myMaps.get(0).getSize());
    //                System.out.println(">>>>>>>>>>>" + myMaps.get(0).getMapName());
    //                System.out.println(">>>>>>>>>>>" + myMaps.get(0).getName());
    //                System.out.println(">>>>>>>>>>>" + myMaps.get(0).getContinent());
    //
    //                return res;
    //            }
    //
    //            @Override protected void onPostExecute(List<String> nameList) {
    //                if (nameList.isEmpty()) {
    //                    logToast("No maps created for your version!? " + Variable.getVariable().getFileListURL());
    //                    return;
    //                } else if (hasError()) {
    //                    getError().printStackTrace();
    //                    logToast("Are you connected to the internet? Problem while fetching remote area list: " +
    //                            getErrorMessage());
    //                    return;
    //                }
    //                MySpinnerListener spinnerListener = new MySpinnerListener() {
    //                    @Override public void onSelect(String selectedArea, String selectedFile) {
    //                        if (selectedFile == null ||
    //                                new File(Variable.getVariable().getMapsFolder(), selectedArea + ".ghz").exists
    // () ||
    //                                new File(Variable.getVariable().getMapsFolder(), selectedArea + "-gh").exists()) {
    //                            downloadURL = null;
    //                        } else {
    //                            downloadURL = selectedFile;
    //                        }
    //                        initFiles(selectedArea);
    //                    }
    //                };
    //                chooseRemoteArea(btnDownloadMap, remoteMapsSpinner, nameList, spinnerListener);
    //            }
    //        }.execute();
    //
    //    }
}