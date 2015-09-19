package com.junjunguo.pocketmaps.model.util;

/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on September 19, 2015.
 */
public class Constant {
    // CONSTANT
    public static final int DOWNLOADING = 0;
    public static final int COMPLETE = 1;
    public static final int PAUSE = 2;
    public static final int ON_SERVER = 3;
    public static final int ERROR = 4;
    public static final String[] statuses = {"Downloading", "Complete", "Pause", "On server", "Error"};

    public static final int BUFFER_SIZE = 8 * 1024;

}
