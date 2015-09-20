package com.junjunguo.pocketmaps.model.listeners;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on July 06, 2015.
 */
public interface MapDownloadListener {
    /**
     * a download is started
     */
    void downloadStart();

    /**
     * a download activity is finished
     */
    void downloadFinished(String mapName);

    void progressUpdate(Integer value);
}
