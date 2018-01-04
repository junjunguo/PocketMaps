package com.junjunguo.pocketmaps.model.listeners;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 22, 2015.
 */
public interface NavigatorListener {
    /**
     * the change on navigator: navigation is used or not
     *
     * @param on
     */
    void onStatusChanged(boolean on);
    
    void onNaviStart(boolean on);
}
