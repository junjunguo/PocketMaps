package com.junjunguo.pocketmaps.model;

import com.junjunguo.pocketmaps.activities.DownloadMapActivity;
import com.junjunguo.pocketmaps.util.Constant;
import com.junjunguo.pocketmaps.util.IO;
import com.junjunguo.pocketmaps.util.Variable;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.List;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 02, 2015.
 */
public class MyMap implements Comparable<MyMap> {
    public static final String TIME_OLD="1970-01";
    public static final String MAP_VERSION = "0.9.0_1";
    private String country = "";
    private String size = "";
    private String url = "";
    private String continent = "";
    private String mapName = "";
    private String timeRemote = "";
    private String timeLocal = "";
    private int resId = 0;
    private int status;
    private boolean updateAvailable = false;
    private long lastCheckTime = 0;

    /**
     * generate MyMap for local select list
     *
     * @param mapName map name
     */
    public MyMap(String mapName) {
        this.status = Constant.COMPLETE;
        int index = mapName.indexOf("-gh");
        if (index > 0) {
            mapName = mapName.substring(0, index);
        }
        this.mapName = mapName;
        generateContinentName(mapName);
        File file = new File(Variable.getVariable().getMapsFolder().getAbsolutePath(), mapName + "-gh");
        setUrl(file.getAbsolutePath());
        setSize(dirSize(file) + "M");
    }
    
    private static File getVersionFile(String mapName)
    {
      char sep = File.separatorChar;
      String mapsFolder = Variable.getVariable().getMapsFolder().getAbsolutePath();
      File versFile = new File(mapsFolder, mapName + "-gh" + sep + "version.txt");
      return versFile;
    }
    
    public static boolean isVersionCompatible(String mapName)
    {
      File versFile = getVersionFile(mapName);
      String mapVers = IO.readFromFile(versFile, "\n");
      if (mapVers == null) { mapVers = "0"; }
      return mapVers.startsWith(MAP_VERSION + "\n");
    }
    
    public static boolean setVersionCompatible(String mapName, MyMap myMap)
    {
      File versFile = getVersionFile(mapName);
      myMap.updateAvailable = false;
      return IO.writeToFile(MAP_VERSION + "\n" + myMap.getTime(true), versFile, false);
    }
    
    private long getLastCheckTimeSpan()
    {
      return System.currentTimeMillis() - lastCheckTime;
    }
    /**
     * generate MyMap for download list
     *
     * @param mapName map name
     * @param size    map size
     */
    public MyMap(String mapName, String size, String timeRemote, String mapUrl) {
        this.mapName = mapName;
        this.size = size;
        this.timeRemote = timeRemote;
        initStatus();
        setUrl(mapUrl + mapName + ".ghz");
        generateContinentName(mapName);
    }

    private void initStatus() {
        if (Variable.getVariable().getLocalMapNameList().contains(mapName)) {
            status = Constant.COMPLETE;
        } else if (Variable.getVariable().getPausedMapName().equalsIgnoreCase(mapName)) {
            //            log("map name: " + mapName + "; " + Variable.getVariable().getPausedMapName());
            status = Constant.PAUSE;
            //            Variable.getVariable().setDownloadStatus(Constant.PAUSE);
        } else {
            status = Constant.ON_SERVER;
        }
    }

    /**
     * split map name to continent name and name and upper case first letter
     *
     * @param mapName map name
     */
    private void generateContinentName(String mapName) {
        String[] s = mapName.split("_");
        setContinent(Character.toString(s[0].charAt(0)).toUpperCase() + s[0].substring(1));
        String country = "";
        for (int i = 1; i < s.length; i++) {
            country += Character.toString(s[i].charAt(0)).toUpperCase() + s[i].substring(1) + " ";
        }
        setCountry(country.substring(0, country.length() - 1));
    }
    
    /** Compare mapTimeLocal and mapTimeRemote.
     *  <br/>Reads mapTimeLocal on first time. **/
    public boolean isUpdateAvailable()
    {
      if (updateAvailable) { return true; }
      log("GH Compare maps Local=" + getTime(false) + " Remote=" + getTime(true));
      updateAvailable = (getTime(true).compareTo(getTime(false))>0);
      return updateAvailable;
    }
    
    /** Check async for updates and inform user.
     *  <br/> Fails on missing network connection?!
     *  @param reloadServer True to get mapDate from server, false to compare this map with local mapTime. **/
    public void checkUpdateAvailableMsg(final Activity activity)
    {
      log("GH Checking for update available ...");
      if (getLastCheckTimeSpan()<(1000*60*60))
      {
        log("GH Do not check again until one hour.");
        return;
      }
      final String msgTxt = "Update for map is available: " + getMapName() + "\nPlease update map!";
      if (updateAvailable)
      {
        log("GH Do not check again, update is available!");
        logUserLong(msgTxt, activity);
        return;
      }
      lastCheckTime = System.currentTimeMillis();
      new AsyncTask<Void, Void, MyMap>() {

        @Override protected void onPostExecute(MyMap myMapRemote) {
            super.onPostExecute(myMapRemote);
            if (myMapRemote==null) { return; } // No map on server.
            if (myMapRemote.isUpdateAvailable())
            {
              MyMap.this.updateAvailable = true;
              logUserLong(msgTxt, activity);
            }
        }

        @Override
        protected MyMap doInBackground(Void... params)
        {
          List<MyMap> myMaps = DownloadMapActivity.getMapsFromJSsources(MyMap.this.getMapName());
          for (MyMap curMap : myMaps)
          {
            if (curMap.getMapName().equals(MyMap.this.getMapName()))
            {
              return curMap;
            }
          }
          return null;
        }
      }.execute();
    }
    
    private static String readTimeLocal(String mapName)
    {
      File versFile = getVersionFile(mapName);
      String mapVers = IO.readFromFile(versFile, "\n");
      String timeLocal = "1970-01";
      if (mapVers != null)
      {
        String fields[] = mapVers.split("\n");
        if (fields.length>1) { timeLocal = fields[1]; }
      }
      return timeLocal;
    }

    /**
     * Return the size of a directory in megabytes
     */
    public long dirSize(File dir) {
        if (!dir.exists()) return 0;

        long result = 0;
        File[] fileList = dir.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            // Recursive call if it's a directory
            if (fileList[i].isDirectory()) {
                result += dirSize(fileList[i]);
            } else {
                result += fileList[i].length();
            }
        }
        return result / (1024 * 1024);
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSize() {
        if (size == "") {
            return "Map size: < 1M";
        }
        return "Map size: " + size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
  public String getTime(boolean ofRemote)
  {
    if (ofRemote)
    {
      return timeRemote;
    }
    if (timeLocal.isEmpty()) { timeLocal = readTimeLocal(getMapName()); }
    return timeLocal;
  }

  public void setTime(boolean ofRemote, String time)
  {
    if (ofRemote)
    {
      this.timeRemote = time;
    }
    this.timeLocal = time;
  }
  
    public String getContinent() {
        return continent;
    }

    private void setContinent(String continent) {
        this.continent = continent;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int compareTo(MyMap o) {
        if (getStatus() != Constant.ON_SERVER && o.getStatus() == Constant.ON_SERVER) {
            return -1;
        }
        if (getStatus() == Constant.ON_SERVER && o.getStatus() != Constant.ON_SERVER) {
            return 1;
        }
        return ((getContinent() + getCountry()).compareToIgnoreCase(o.getContinent() + o.getCountry()));
    }

    public String toString() {
        return "MyMap{" +
                "country='" + country + '\'' +
                ", size='" + size + '\'' +
                ", url='" + url + '\'' +
                ", continent='" + continent + '\'' +
                ", mapName='" + mapName + '\'' +
                ", resId=" + resId +
                ", timeRemote=" + timeRemote +
                ", timeLocal=" + timeLocal +
                ", status=" + getStatusStr() +
                '}';
    }

    /**
     * @return status as a String
     */
    public String getStatusStr() {
        return Constant.statuses[getStatus()];
    }

    private static void log(String str) {
        Log.i(MyMap.class.getName(), str);
    }
    
    private void logUserLong(String str, Activity activity) {
      Log.i(MyMap.class.getName(), str);
      Toast.makeText(activity.getBaseContext(), str, Toast.LENGTH_LONG).show();
    }
}
