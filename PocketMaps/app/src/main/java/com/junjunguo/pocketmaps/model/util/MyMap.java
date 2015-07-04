package com.junjunguo.pocketmaps.model.util;

import java.io.File;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 02, 2015.
 */
public class MyMap {
    private String name, size, url, continent, mapName;
    private int resId;
    private boolean downloaded;

    public void init() {
        this.name = "";
        this.size = "";
        this.url = "";
        this.continent = "";
        this.mapName = "";
        this.resId = 0;
        this.downloaded = false;
    }

    /**
     * generate MyMap for local select
     *
     * @param mapName
     */
    public MyMap(String mapName) {
        init();
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

    /**
     * generate MyMap for download list
     *
     * @param mapName
     * @param size
     */
    public MyMap(String mapName, String size) {
        init();
        this.mapName = mapName;
        this.size = size;
        setUrl(Variable.getVariable().getFileListURL() + mapName + ".ghz");
        generateContinentName(mapName);
    }

    /**
     * split map name to continent name and name and upper case first letter
     *
     * @param mapName
     */
    private void generateContinentName(String mapName) {
        String[] s = mapName.split("_");
        setContinent(Character.toString(s[0].charAt(0)).toUpperCase() + s[0].substring(1));
        setName(Character.toString(s[1].charAt(0)).toUpperCase() + s[1].substring(1));
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

    /**
     * @return country name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
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

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    private void log(String s) {
        System.out.println(this.getClass().getSimpleName() + "-------------------" + s);
    }

    public String toString() {
        return "MyMap{" +
                "name='" + name + '\'' +
                ", size='" + size + '\'' +
                ", url='" + url + '\'' +
                ", continent='" + continent + '\'' +
                ", mapName='" + mapName + '\'' +
                ", resId=" + resId +
                ", downloaded=" + downloaded +
                '}';
    }
}
