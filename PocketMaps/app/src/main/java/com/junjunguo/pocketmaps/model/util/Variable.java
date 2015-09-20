package com.junjunguo.pocketmaps.model.util;

import android.content.Context;
import android.util.Log;

import com.junjunguo.pocketmaps.model.dataType.MyMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.mapsforge.core.model.LatLong;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
     * users last browsed screen center location
     */
    private LatLong lastLocation;
    /**
     * map directory name: pocketmaps/maps/
     */
    private String mapDirectory;
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
     * a folder to save tracking files
     */
    private File trackingFolder;

    /**
     * a list of url address for each sit: each sit has a list of country's map
     */
    private String mapUrlList;
    //    private String mapUrlList;
    /**
     * prepare to load the map
     */
    private volatile boolean prepareInProgress;

    /**
     * list of downloaded maps in local storage; check and init when app started; used to avoid recheck local files
     */
    private List<MyMap> localMaps;

    /**
     *
     */
    //    private String unFinishedMapURL;
    private String pausedMapName;

    /**
     * int DOWNLOADING = 0; int COMPLETE = 1; int PAUSE = 2; int ON_SERVER = 3;
     */
    private int downloadStatus;

    /**
     * map file last modified time
     */
    private String mapLastModified;
    /**
     * map download finished = -1;
     * <p/>
     * map download unfinished: downloaded percentage 0--100
     */
    private int mapFinishedPercentage;

    /**
     * temporary memorialize recent downloaded maps from DownloadMapActivity
     */
    private List<MyMap> recentDownloadedMaps;

    /**
     * temporary memorialize download list of cloud maps from DownloadMapActivity
     */
    private List<MyMap> cloudMaps;
    /**
     * default true for auto load;
     * <p>
     * when load: app open auto load = true, when load a new map from main activity we need to set auto load = false
     */
    //    private boolean autoLoad;
    /**
     * sport category spinner index at {@link com.junjunguo.pocketmaps.controller.Analytics#spinner}
     */
    private int sportCategoryIndex;


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
        this.trackingDirectory = "/pocketmaps/tracking/";
        this.mapUrlList = "http://folk.ntnu.no/junjung/pocketmaps/map_url_list";
        this.localMaps = new ArrayList<>();
        this.recentDownloadedMaps = new ArrayList<>();
        this.cloudMaps = new ArrayList<>();
        this.sportCategoryIndex = 0;
        resetDownloadMapVariables();
    }

    public void resetDownloadMapVariables() {
        this.downloadStatus = Constant.ON_SERVER;
        this.pausedMapName = "";
        this.mapLastModified = "";
        this.mapFinishedPercentage = -1;
        //        this.unFinishedMapURL = "";
    }

    public static Variable getVariable() {
        if (variable == null) {
            variable = new Variable();
        }
        return variable;
    }

    /**
     * @return file list url address default  = "http://folk.ntnu.no/junjung/pocketmaps/maps/" (can not reset)
     */
    public String getMapUrlList() {
        return mapUrlList;
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

    public LatLong getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(LatLong lastLocation) {
        this.lastLocation = lastLocation;
    }

    public String getMapDirectory() {
        return mapDirectory;
    }

    public String getTrackingDirectory() {
        return trackingDirectory;
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

    public File getTrackingFolder() {
        return trackingFolder;
    }

    public void setTrackingFolder(File trackingFolder) {
        this.trackingFolder = trackingFolder;
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

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public int getMapFinishedPercentage() {
        return mapFinishedPercentage;
    }

    public void setMapFinishedPercentage(int mapFinishedPercentage) {
        this.mapFinishedPercentage = mapFinishedPercentage;
    }

    public String getMapLastModified() {
        return mapLastModified;
    }

    public void setMapLastModified(String mapLastModified) {
        this.mapLastModified = mapLastModified;
    }

    //    public String getUnFinishedMapURL() {
    //        return unFinishedMapURL;
    //    }

    //    public void setUnFinishedMapURL(String unFinishedMapURL) {
    //        this.unFinishedMapURL = unFinishedMapURL;
    //    }

    public String getPausedMapName() {
        return pausedMapName;
    }

    public void setPausedMapName(String pausedMapName) {
        this.pausedMapName = pausedMapName;
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
    public List getLocalMapNameList() {
        ArrayList<String> al = new ArrayList();
        for (MyMap mm : getLocalMaps()) {
            al.add(mm.getMapName());
        }
        return al;
    }

    public List<MyMap> getRecentDownloadedMaps() {
        return recentDownloadedMaps;
    }

    public void addRecentDownloadedMap(MyMap myMap) {
        recentDownloadedMaps.add(myMap);
    }

    public MyMap removeRecentDownloadedMap(int index) throws Exception {
        //        if (index >= 0 && index < getRecentDownloadedMaps().size()) {
        return recentDownloadedMaps.remove(index);
        //        }
    }


    public void setRecentDownloadedMaps(List<MyMap> recentDownloadedMaps) {
        this.recentDownloadedMaps = recentDownloadedMaps;
    }

    public List<MyMap> getCloudMaps() {
        return cloudMaps;
    }

    public void setCloudMaps(List<MyMap> cloudMaps) {
        this.cloudMaps = cloudMaps;
    }

    //    public void setAutoLoad(boolean autoLoad) {
    //        this.autoLoad = autoLoad;
    //    }

    public int getSportCategoryIndex() {
        return sportCategoryIndex;
    }

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
        //        if (!autoLoad) {return false;}
        String file = readFile();
        if (file == null) {
            return false;
        }
        JSONObject jo;
        try {
            boolean loadMap = false;
            jo = new JSONObject(file);
            setTravelMode(jo.getString("travelMode"));
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
                setLastLocation(new LatLong(la, lo));
            }
            String coun = jo.getString("country");
            if (coun != "") {
                setCountry(jo.getString("country"));
                loadMap = true;
            }
            setMapDirectory(jo.getString("mapDirectory"));
            setMapsFolder(new File(jo.getString("mapsFolderAbsPath")));
            setSportCategoryIndex(jo.getInt("sportCategoryIndex"));
            setDownloadStatus(jo.getInt("mapDownloadStatus"));
            setMapLastModified(jo.getString("mapLastModified"));
            setMapFinishedPercentage(jo.getInt("mapFinishedPercentage"));
            //            setUnFinishedMapURL(jo.getString("mapUnfinishedDownlURL"));
            setPausedMapName(jo.getString("pausedMapName"));
            if (getPausedMapName() != "") {
                loadMap = false;
            }
            if (!hasUnfinishedDownload()) {
                //                log("reset download map variables");
                resetDownloadMapVariables();
            }
            return loadMap;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean hasUnfinishedDownload() {
        String[] filesGHZ = getMapsFolder().list(new FilenameFilter() {
            @Override public boolean accept(File dir, String filename) {
                return (filename != null && (filename.endsWith(".ghz")));
            }
        });
        String[] files_gh = getMapsFolder().list(new FilenameFilter() {
            @Override public boolean accept(File dir, String filename) {
                return (filename != null && (filename.endsWith("-gh")));
            }
        });

        for (String file : filesGHZ) {
            for (String f : files_gh) {
               if( f.contains(file.replace(".ghz", ""))){
                   (new File(getMapsFolder(), file)).delete();
               }
            }
            Variable.getVariable().addLocalMap(new MyMap(file));
            if (file.contains(getPausedMapName())) {
                return true;
            }
            //            boolean del =
            (new File(getMapsFolder(), file)).delete();
            //                        log("delete file " + file + " ? -" + del);
        }
        return false;
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
            if (getLastLocation() != null) {
                jo.put("latitude", getLastLocation().latitude);
                jo.put("longitude", getLastLocation().longitude);
            } else {
                jo.put("latitude", 0);
                jo.put("longitude", 0);
            }
            if (getCountry() == null) {
                jo.put("country", "");

            } else {
                jo.put("country", getCountry());
            }
            jo.put("mapDirectory", getMapDirectory());
            jo.put("mapsFolderAbsPath", getMapsFolder().getAbsolutePath());
            jo.put("sportCategoryIndex", getSportCategoryIndex());
            jo.put("mapDownloadStatus", getDownloadStatus());
            jo.put("mapLastModified", getMapLastModified());
            jo.put("mapFinishedPercentage", getMapFinishedPercentage());
            //            jo.put("mapUnfinishedDownlURL", getUnFinishedMapURL());
            jo.put("pausedMapName", getPausedMapName());
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

    private void log(String str) {
        Log.i(this.getClass().getSimpleName(), "-------" + str);
    }

}