package com.junjunguo.pocketmaps.util;

import android.content.Context;
import android.util.Log;

import com.junjunguo.pocketmaps.activities.Analytics;
import com.junjunguo.pocketmaps.model.MyMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.oscim.core.GeoPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * variable data might need to be saved to file
 * <p/>
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 27, 2015.
 */
public class Variable {
    public enum TravelMode{Foot, Bike, Car};

    private TravelMode travelMode;
    /**
     * fastest, shortest (route)
     */
    private String weighting;
    /**
     * Bidirectional Dijkstra:      DIJKSTRA_BI             = "dijkstrabi"
     * <p/>
     * Unidirectional Dijkstra:     DIJKSTRA                = "dijkstra"
     * <p/>
     * one to many Dijkstra:        DIJKSTRA_ONE_TO_MANY    = "dijkstraOneToMany"
     * <p/>
     * Unidirectional A* :          ASTAR                   = "astar"
     * <p/>
     * Bidirectional A* :           ASTAR_BI                = "astarbi"
     */
    private String routingAlgorithms;
    /**
     * advanced setting on or off
     */
    private boolean advancedSetting;
    /**
     * instructions on or off; default true (on)
     */
    private boolean directionsON;
    /**
     * maximum zoom level on map
     */
    private int zoomLevelMax;
    /**
     * minimum zoom level on map
     */
    private int zoomLevelMin;
    /**
     * users current / last used zoom level
     */
    private int lastZoomLevel;

    /**
     * users last browsed screen center location
     */
    private GeoPoint lastLocation;
    /**
     * map directory name: pocketmaps/maps/
     */
    private String mapDirectory;
    /**
     * download directory name: pocketmaps/downloads/
     */
    private String dlDirectory;
    /**
     * map directory name: pocketmaps/tracking/
     */
    private String trackingDirectory;

    /**
     * area or country name (need to be loaded)
     * <p/>
     * example: /storage/emulated/0/Download/(mapDirectory)/(country)-gh
     */
    private String country;
    /**
     * a File where all Areas or counties are in
     * <p/>
     * example:
     * <p/>
     * <li>mapsFolder.getAbsolutePath() = /storage/emulated/0/Download/pocketmaps/maps </li>
     * <p/>
     * <li> mapsFolder   =   new File("/storage/emulated/0/Download/pocketmaps/maps")</li>
     */
    private File mapsFolder;

    /**
     * Server for download JSON list of maps
     */
    private String mapUrlJSON;
    /**
     * prepare to load the map
     */
    private volatile boolean prepareInProgress;

    /**
     * list of downloaded maps in local storage; check and init when app started; used to avoid recheck local files
     */
    private List<MyMap> localMaps;
    /**
     * temporary memorialize recent downloaded maps from DownloadMapActivity
     */
    private List<MyMap> recentDownloadedMaps;

    /**
     * temporary memorialize download list of cloud maps from DownloadMapActivity
     */
    private List<MyMap> cloudMaps;
    
    /**
     * sport category spinner index at {@link Analytics#spinner}
     */
    private int sportCategoryIndex;

    private boolean lightSensorON;
    private boolean voiceON;

    /**
     * application context
     */
    private Context context;

    private static Variable variable;

    private Variable() {
        this.travelMode = TravelMode.Foot;
        this.weighting = "fastest";
        this.routingAlgorithms = "astarbi";
        this.zoomLevelMax = 22;
        this.zoomLevelMin = 1;
        this.lastZoomLevel = 8;
        this.lastLocation = null;
        this.country = null;
        this.mapsFolder = null;
        this.context = null;
        this.advancedSetting = false;
        this.directionsON = true;
        this.voiceON = true;
        this.lightSensorON = true;
        this.mapDirectory = "pocketmaps/maps/";
        this.dlDirectory = "pocketmaps/downloads/";
        this.trackingDirectory = "pocketmaps/tracking/";
        this.mapUrlJSON = "http://vsrv15044.customer.xenway.de/maps";
        this.localMaps = new ArrayList<>();
        this.recentDownloadedMaps = new ArrayList<>();
        this.cloudMaps = new ArrayList<>();
        this.sportCategoryIndex = 0;
    }

    public static Variable getVariable() {
        if (variable == null) {
            variable = new Variable();
        }
        return variable;
    }

    public String getMapUrlJSON() {
        return mapUrlJSON;
    }

    public TravelMode getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(TravelMode travelMode) {
        this.travelMode = travelMode;
    }

    public String getWeighting() {
        return weighting;
    }

    public void setWeighting(String weighting) {
        this.weighting = weighting;
    }

    public String getRoutingAlgorithms() {
        return routingAlgorithms;
    }

    public void setRoutingAlgorithms(String routingAlgorithms) {
        this.routingAlgorithms = routingAlgorithms;
    }

    public boolean isAdvancedSetting() {
        return advancedSetting;
    }

    public void setAdvancedSetting(boolean advancedSetting) {
        this.advancedSetting = advancedSetting;
    }

    public boolean isDirectionsON() {
        return directionsON;
    }

    public void setDirectionsON(boolean directionsON) {
        this.directionsON = directionsON;
    }
    
    public boolean isVoiceON()
    {
      return voiceON;
    }
    
    public void setVoiceON(boolean voiceON)
    {
      this.voiceON = voiceON;
    }
    
    public boolean isLightSensorON()
    {
      return lightSensorON;
    }
    
    public void setLightSensorON(boolean lightSensorON)
    {
      this.lightSensorON = lightSensorON;
    }

    /**
     * @return is DirectionsON as string : "true or false"
     */
    public String getDirectionsON() {
        return isDirectionsON() ? "true" : "false";
    }

    public int getZoomLevelMax() {
        return zoomLevelMax;
    }

    public void setZoomLevelMax(int zoomLevelMax) {
        this.zoomLevelMax = zoomLevelMax;
    }

    public int getZoomLevelMin() {
        return zoomLevelMin;
    }

    public void setZoomLevelMin(int zoomLevelMin) {
        this.zoomLevelMin = zoomLevelMin;
    }

    /**
     * @param zoomLevelMax max zoom level
     * @param zoomLevelMin min zoom level
     */
    public void setZoomLevels(int zoomLevelMax, int zoomLevelMin) {
        setZoomLevelMax(zoomLevelMax);
        setZoomLevelMin(zoomLevelMin);
    }

    public int getLastZoomLevel() {
        return lastZoomLevel;
    }

    public void setLastZoomLevel(int lastZoomLevel) {
        this.lastZoomLevel = lastZoomLevel;
    }

    public GeoPoint getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(GeoPoint lastLocation) {
        this.lastLocation = lastLocation;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public File getMapsFolder() {
        return mapsFolder;
    }
    
    public File getDownloadsFolder() {
      File dlFolder = new File(mapsFolder.getParentFile().getParent(), dlDirectory);
      if (!dlFolder.exists()) { dlFolder.mkdirs(); }
      return new File(mapsFolder.getParentFile().getParent(), dlDirectory);
    }

    public void setBaseFolder(String baseFolder) {
        this.mapsFolder = new File(baseFolder, mapDirectory);
    }

    public File getTrackingFolder() {
      return new File(mapsFolder.getParentFile().getParent(), trackingDirectory);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isPrepareInProgress() {
        return prepareInProgress;
    }

    public void setPrepareInProgress(boolean prepareInProgress) {
        this.prepareInProgress = prepareInProgress;
    }

    public List<MyMap> getLocalMaps() {
        return localMaps;
    }

    /**
     * add a list of maps to localMaps
     *
     * @param localMaps list of maps
     */
    public void addLocalMaps(List<MyMap> localMaps) {
        this.localMaps.addAll(localMaps);
    }

    /**
     * add a map to local map list
     *
     * @param localMap MyMap
     */
    public void addLocalMap(MyMap localMap) {
        if (!getLocalMapNameList().contains(localMap.getMapName())) {
            this.localMaps.add(localMap);
        }
    }

    public void removeLocalMap(MyMap localMap) {
        this.localMaps.remove(localMap);
    }

    public void setLocalMaps(List<MyMap> localMaps) {
        this.localMaps = localMaps;
    }

    /**
     * @return a string list of local map names (continent_country)
     */
    public List<String> getLocalMapNameList() {
        ArrayList<String> al = new ArrayList<String>();
        for (MyMap mm : getLocalMaps()) {
            al.add(mm.getMapName());
        }
        return al;
    }

    public List<MyMap> getRecentDownloadedMaps() {
        return recentDownloadedMaps;
    }

    public List<MyMap> getCloudMaps() {
        return cloudMaps;
    }

    public void updateCloudMaps(List<MyMap> cloudMaps) {
      ArrayList<MyMap> newList = new ArrayList<MyMap>();
      for (MyMap oldMap : this.cloudMaps)
      {
        for (MyMap newMap : cloudMaps)
        {
          if (newMap.getUrl().equals(oldMap.getUrl()))
          {
            newMap.setStatus(oldMap.getStatus());
            break;
          }
        }
      }
      // Find same Map from CloudMaps
      for (MyMap newMap : cloudMaps)
      {
        int myIndex = Variable.getVariable().getCloudMaps().indexOf(newMap);
        if (myIndex < 0) { newList.add(newMap); continue; }
        MyMap sameMap = Variable.getVariable().getCloudMaps().get(myIndex);
        sameMap.set(newMap);
        newList.add(sameMap);
      }
      this.cloudMaps = newList;
    }

    /** Similar as getTravelMode() but used for spinner. **/
    public int getSportCategoryIndex() {
        return sportCategoryIndex;
    }

    /** Similar as setTravelMode() but used for spinner. **/
    public void setSportCategoryIndex(int sportCategoryIndex) {
        this.sportCategoryIndex = sportCategoryIndex;
    }

    /**
     * run when app open at run time
     * <p/>
     * load variables from saved file
     *
     * @return true if load succeed, false if nothing to load or load fail
     */
    public boolean loadVariables() {
        String file = readFile();
        if (file == null) {
            return false;
        }
        JSONObject jo;
        try {
            jo = new JSONObject(file);
            setTravelMode(TravelMode.valueOf(toUpperFirst(jo.getString("travelMode"))));
            setWeighting(jo.getString("weighting"));
            setRoutingAlgorithms(jo.getString("routingAlgorithms"));
            setDirectionsON(jo.getBoolean("directionsON"));
            setAdvancedSetting(jo.getBoolean("advancedSetting"));
            setZoomLevelMax(jo.getInt("zoomLevelMax"));
            setZoomLevelMin(jo.getInt("zoomLevelMin"));
            setLastZoomLevel(jo.getInt("lastZoomLevel"));
            double la = jo.getDouble("latitude");
            double lo = jo.getDouble("longitude");
            if (la != 0 && lo != 0) {
                setLastLocation(new GeoPoint(la, lo));
            }
            String coun = jo.getString("country");
            if (coun != "") {
                setCountry(jo.getString("country"));
            }
            File mapsFolderAbsPath = new File(jo.getString("mapsFolderAbsPath"));
            if (mapsFolderAbsPath.exists())
            {
              setBaseFolder(mapsFolderAbsPath.getParentFile().getParent());
            }
            setSportCategoryIndex(jo.getInt("sportCategoryIndex"));
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static String toUpperFirst(String string)
    {
      // TODO This is just a workaround, because of incompatiblity from older versions.
      // This workaround will ensure to override the fault data.
      // Can be deleted later, maybe in Version 3.0, or in year 2022
      if (string == null) { return "Car"; }
      String first = string.substring(0,1).toUpperCase();
      String rest = string.substring(1);
      return first + rest;
    }

    /**
     * run before app destroyed at run time
     * <p/>
     * save variables to local file (json)   @return true is succeed, false otherwise
     */
    public boolean saveVariables() {
        JSONObject jo = new JSONObject();
        try {
            jo.put("travelMode", getTravelMode().toString());
            jo.put("weighting", getWeighting());
            jo.put("routingAlgorithms", getRoutingAlgorithms());
            jo.put("advancedSetting", isAdvancedSetting());
            jo.put("directionsON", isDirectionsON());
            jo.put("zoomLevelMax", getZoomLevelMax());
            jo.put("zoomLevelMin", getZoomLevelMin());
            jo.put("lastZoomLevel", getLastZoomLevel());
            if (getLastLocation() != null) {
                jo.put("latitude", getLastLocation().getLatitude());
                jo.put("longitude", getLastLocation().getLongitude());
            } else {
                jo.put("latitude", 0);
                jo.put("longitude", 0);
            }
            if (getCountry() == null) {
                jo.put("country", "");

            } else {
                jo.put("country", getCountry());
            }
            jo.put("mapsFolderAbsPath", getMapsFolder().getAbsolutePath());
            jo.put("sportCategoryIndex", getSportCategoryIndex());
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return saveStringToFile(jo.toString());
    }

    /**
     * @return read saved file and return it as a string
     */
    public String readFile() {
        try(FileInputStream fis = context.openFileInput("pocketmapssavedfile.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr))
        {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            log("Cant load savingfile, maybe the first time since app installed.");
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param file a string need to be saved
     * @return
     */
    public boolean saveStringToFile(String file) {
        try(FileOutputStream fos = context.openFileOutput("pocketmapssavedfile.txt", Context.MODE_PRIVATE))
        {
            fos.write(file.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    private void log(String s) {
      Log.i(Variable.class.getName(), s);
    }

}