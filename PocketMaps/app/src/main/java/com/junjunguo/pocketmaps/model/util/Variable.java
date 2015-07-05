package com.junjunguo.pocketmaps.model.util;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;
import org.mapsforge.core.model.LatLong;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    /**
     * foot, bike, car
     */
    private String travelMode;
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
     * users last location: saved from current, use as last when next time open
     */
    private LatLong lastLocation;
    /**
     * map directory name: pocketmaps/maps/
     */
    private String mapDirectory;

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
     * a list of url address for each country's map
     */
    private String fileListURL;
    /**
     * prepare to load the map
     */
    private volatile boolean prepareInProgress;

    /**
     * list of downloaded maps in local storage; check and init when app started
     */
    private List<MyMap> localMaps;
    private boolean aNewMapDownloaded;

    /**
     * @return file list url address default  = "http://folk.ntnu.no/junjung/pocketmaps/maps/" (can not reset)
     */
    public String getFileListURL() {
        return fileListURL;
    }

    /**
     * application context
     */
    private Context context;

    private static Variable variable;

    private Variable() {
        this.travelMode = "foot";
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
        this.mapDirectory = "/pocketmaps/maps/";
        this.fileListURL = "http://folk.ntnu.no/junjung/pocketmaps/maps/";
        this.localMaps = new ArrayList<>();
        this.aNewMapDownloaded = false;
    }

    public static Variable getVariable() {
        if (variable == null) {
            variable = new Variable();
        }
        return variable;
    }

    public String getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(String travelMode) {
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
     * @param zoomLevelMax
     * @param zoomLevelMin
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

    public LatLong getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(LatLong lastLocation) {
        this.lastLocation = lastLocation;
    }

    public String getMapDirectory() {
        return mapDirectory;
    }

    public void setMapDirectory(String mapDirectory) {
        this.mapDirectory = mapDirectory;
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

    public void setMapsFolder(File mapsFolder) {
        this.mapsFolder = mapsFolder;
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

    public boolean isaNewMapDownloaded() {
        return aNewMapDownloaded;
    }

    public void setaNewMapDownloaded(boolean aNewMapDownloaded) {
        this.aNewMapDownloaded = aNewMapDownloaded;
    }

    /**
     * add a list of maps to localMaps
     *
     * @param localMaps
     */
    public void addLocalMaps(List<MyMap> localMaps) {
        this.localMaps.addAll(localMaps);
    }

    /**
     * add a map to local map list
     *
     * @param localMap
     */
    public void addLocalMap(MyMap localMap) {
        this.localMaps.add(localMap);
    }

    /**
     * @return a string list of local map names (continent_country)
     */
    public List getLocalMapNameList() {
        ArrayList<String> mn = new ArrayList();
        for (MyMap m : getLocalMaps()) {
            mn.add(m.getMapName());
        }
        return mn;
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
            setTravelMode(jo.getString("travelMode"));
            setWeighting(jo.getString("weighting"));
            setRoutingAlgorithms(jo.getString("routingAlgorithms"));
            setDirectionsON(jo.getBoolean("directionsON"));
            setAdvancedSetting(jo.getBoolean("advancedSetting"));
            setZoomLevelMax(jo.getInt("zoomLevelMax"));
            setZoomLevelMin(jo.getInt("zoomLevelMin"));
            setLastZoomLevel(jo.getInt("lastZoomLevel"));
            setLastLocation(new LatLong(jo.getDouble("latitude"), jo.getDouble("longitude")));
            setMapDirectory(jo.getString("mapDirectory"));
            setCountry(jo.getString("country"));
            setMapsFolder(new File(jo.getString("mapsFolderAbsPath")));
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * run before app destroyed at run time
     * <p/>
     * save variables to local file (json)   @return true is succeed, false otherwise
     */
    public boolean saveVariables() {
        JSONObject jo = new JSONObject();
        try {
            jo.put("travelMode", getTravelMode());
            jo.put("weighting", getWeighting());
            jo.put("routingAlgorithms", getRoutingAlgorithms());
            jo.put("advancedSetting", isAdvancedSetting());
            jo.put("directionsON", isDirectionsON());
            jo.put("zoomLevelMax", getZoomLevelMax());
            jo.put("zoomLevelMin", getZoomLevelMin());
            jo.put("lastZoomLevel", getLastZoomLevel());
            jo.put("latitude", getLastLocation().latitude);
            jo.put("longitude", getLastLocation().longitude);
            jo.put("mapDirectory", getMapDirectory());
            jo.put("country", getCountry());
            jo.put("mapsFolderAbsPath", getMapsFolder().getAbsolutePath());
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
        FileInputStream fis = null;
        try {
            fis = context.openFileInput("pocketmapssavedfile.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.getStackTrace();
            return null;
        }
    }


    /**
     * @param file a string need to be saved
     * @return
     */
    public boolean saveStringToFile(String file) {
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput("pocketmapssavedfile.txt", Context.MODE_PRIVATE);
            outputStream.write(file.getBytes());
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}