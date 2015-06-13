package com.junjunguo.offlinemap.controller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.graphhopper.util.Helper;
import com.graphhopper.util.ProgressListener;
import com.junjunguo.offlinemap.R;
import com.junjunguo.offlinemap.model.map.AndroidDownloader;
import com.junjunguo.offlinemap.model.map.AndroidHelper;
import com.junjunguo.offlinemap.model.map.GHAsyncTask;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends ActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private String mapDirectory = "/offlinemap/maps/";
    private String currentArea = "";
    private String fileListURL = "http://folk.ntnu.no/junjung/osm/v1/";
    private String prefixURL = fileListURL;

    private String downloadURL;
    private File mapsFolder;

    private volatile boolean prepareInProgress = false;

    private Spinner localMapsSpinner;
    private Button btnSelectLocalMap;
    private Spinner remoteMapsSpinner;
    private Button btnDownloadMap;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;


    @Override public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_main);
        buildGoogleApiClient();


        boolean greaterOrEqKitkat = Build.VERSION.SDK_INT >= 19;
        if (greaterOrEqKitkat) {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                logToast("Offline Map is not usable without an external storage!");
                return;
            }
            mapsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    mapDirectory);
        } else mapsFolder = new File(Environment.getExternalStorageDirectory(), mapDirectory);

        if (!mapsFolder.exists()) mapsFolder.mkdirs();

        TextView welcome = (TextView) findViewById(R.id.title_text);
        //        welcome.setText("Welcome to Offline Map " + Constants.VERSION + "!");
        welcome.setPadding(6, 3, 3, 3);
        localMapsSpinner = (Spinner) findViewById(R.id.locale_area_spinner);
        btnSelectLocalMap = (Button) findViewById(R.id.btn_select_local_map);
        remoteMapsSpinner = (Spinner) findViewById(R.id.remote_area_spinner);
        btnDownloadMap = (Button) findViewById(R.id.btn_download_map);
        // TODO get user confirmation to download
        // if (AndroidHelper.isFastDownload(this))
        if (isOnline()) {
            chooseAreaFromRemote();
        }
        chooseAreaFromLocal();
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
    private void chooseAreaFromLocal() {
        List<String> nameList = new ArrayList<String>();
        String[] files = mapsFolder.list(new FilenameFilter() {
            @Override public boolean accept(File dir, String filename) {
                return filename != null && (filename.endsWith(".ghz") || filename.endsWith("-gh"));
            }
        });
        for (String file : files) {
            nameList.add(file);
        }

        if (nameList.isEmpty()) return;

        chooseArea(btnSelectLocalMap, localMapsSpinner, nameList, new MySpinnerListener() {
            @Override public void onSelect(String selectedArea, String selectedFile) {
                initFiles(selectedArea);
            }
        });
    }

    /**
     * inner interface
     */
    public interface MySpinnerListener {
        void onSelect(String selectedArea, String selectedFile);
    }

    /**
     * list of area (countries) from server
     */
    private void chooseAreaFromRemote() {
        new GHAsyncTask<Void, Void, List<String>>() {
            protected List<String> saveDoInBackground(Void... params) throws Exception {
                String[] lines = new AndroidDownloader().downloadAsString(fileListURL).split("\n");
                List<String> res = new ArrayList<String>();
                for (String str : lines) {
                    int index = str.indexOf("href=\"");
                    if (index >= 0) {
                        index += 6;
                        int lastIndex = str.indexOf(".ghz", index);
                        if (lastIndex >= 0) res.add(prefixURL + str.substring(index, lastIndex) + ".ghz");
                    }
                }

                return res;
            }

            @Override protected void onPostExecute(List<String> nameList) {
                if (nameList.isEmpty()) {
                    logToast("No maps created for your version!? " + fileListURL);
                    return;
                } else if (hasError()) {
                    getError().printStackTrace();
                    logToast("Are you connected to the internet? Problem while fetching remote area list: " +
                            getErrorMessage());
                    return;
                }
                MySpinnerListener spinnerListener = new MySpinnerListener() {
                    @Override public void onSelect(String selectedArea, String selectedFile) {
                        if (selectedFile == null || new File(mapsFolder, selectedArea + ".ghz").exists() ||
                                new File(mapsFolder, selectedArea + "-gh").exists()) {
                            downloadURL = null;
                        } else {
                            downloadURL = selectedFile;
                        }
                        initFiles(selectedArea);
                    }
                };
                chooseArea(btnDownloadMap, remoteMapsSpinner, nameList, spinnerListener);
            }
        }.execute();
    }

    /**
     * download files
     *
     * @param area
     */
    private void initFiles(String area) {
        prepareInProgress = true;
        currentArea = area;
        downloadingFiles();
    }

    public void downloadingFiles() {
        final File areaFolder = new File(mapsFolder, currentArea + "-gh");
        // do not run downloading
        if (downloadURL == null || areaFolder.exists()) {
            //            loadMap(areaFolder);
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Downloading and uncompressing " + downloadURL);
        dialog.setIndeterminate(false);
        dialog.setMax(100);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();

        new GHAsyncTask<Void, Integer, Object>() {
            protected Object saveDoInBackground(Void... _ignore) throws Exception {
                String localFolder = Helper.pruneFileEnd(AndroidHelper.getFileName(downloadURL));
                localFolder = new File(mapsFolder, localFolder + "-gh").getAbsolutePath();
                log("downloading & unzipping " + downloadURL + " to " + localFolder);
                AndroidDownloader downloader = new AndroidDownloader();
                downloader.setTimeout(30000);
                downloader.downloadAndUnzip(downloadURL, localFolder, new ProgressListener() {
                    @Override public void update(long val) {
                        publishProgress((int) val);
                    }
                });
                return null;
            }

            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                dialog.setProgress(values[0]);
            }

            protected void onPostExecute(Object _ignore) {
                dialog.hide();
                if (hasError()) {
                    String str = "An error happend while retrieving maps:" + getErrorMessage();
                    log(str, getError());
                    logToast(str);
                } else {
                    // load map when finish downloading
                    //                    loadMap(areaFolder);
                }
            }
        }.execute();
    }

    /**
     * when an Area (country) is chosen
     *
     * @param button     btn download map
     * @param spinner    list of countries
     * @param nameList   list of names
     * @param mylistener MySpinnerListener
     */
    private void chooseArea(Button button, final Spinner spinner, List<String> nameList,
            final MySpinnerListener mylistener) {
        final Map<String, String> nameToFullName = new TreeMap<String, String>();
        for (String fullName : nameList) {
            String tmp = Helper.pruneFileEnd(fullName);
            if (tmp.endsWith("-gh")) tmp = tmp.substring(0, tmp.length() - 3);

            tmp = AndroidHelper.getFileName(tmp);
            nameToFullName.put(tmp, fullName);
        }
        nameList.clear();
        nameList.addAll(nameToFullName.keySet());
        ArrayAdapter<String> spinnerArrayAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, nameList);
        spinner.setAdapter(spinnerArrayAdapter);
        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Object o = spinner.getSelectedItem();
                if (o != null && o.toString().length() > 0 && !nameToFullName.isEmpty()) {
                    String area = o.toString();
                    mylistener.onSelect(area, nameToFullName.get(area));
                    startMapActivity();
                } else {
                    mylistener.onSelect(null, null);
                }
            }
        });
    }

    /**
     * move to map screen
     */
    private void startMapActivity() {
        logToast("start MapActivity");
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("prepareInProgressExtra", prepareInProgress);
        intent.putExtra("currentAreaExtra", currentArea);
        //        intent.putExtra("mapDirectoryExtra",mapDirectory);
        intent.putExtra("mapsFolderAbsolutePathExtra", mapsFolder.getAbsolutePath());
        logToast(mapsFolder.getAbsolutePath());
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

    /**
     * @return true is there is a network connection
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * send message to logcat
     *
     * @param str
     */
    private void log(String str) {
        Log.i(this.getClass().getSimpleName(), str);
    }

    private void log(String str, Throwable t) {
        Log.i(this.getClass().getSimpleName(), str, t);
    }

    /**
     * send message to logcat and Toast it on screen
     *
     * @param str: message
     */
    private void logToast(String str) {
        log(str);
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }
}